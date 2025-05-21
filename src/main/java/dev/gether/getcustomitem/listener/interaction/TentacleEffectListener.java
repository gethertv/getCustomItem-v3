package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.TentacleEffectItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class TentacleEffectListener extends AbstractCustomItemListener<TentacleEffectItem> {
    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();
    private final Map<UUID, ItemStack> removedElytras = new HashMap<>();

    public TentacleEffectListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!isValidTentacleEffect(event)) return;

        TentacleEffectItem item = (TentacleEffectItem) event.getCustomItem();
        Player player = event.getPlayer();

        if (!canUseItem(player, item, event.getItemStack(), event.getEquipmentSlot())) return;

        item.notifyYourself(player);
        item.playSound(player.getLocation());
        List<Player> targets = findValidTargets(player, item.getSearchRadius());
        targets.forEach(target -> startEffectTask(player, target, item));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        if (activeEffects.containsKey(playerUuid)) {
            ItemStack item = event.getItem();

            if (item != null && item.getType() == Material.ELYTRA) {
                event.setCancelled(true);
                MessageUtil.sendMessage(player, fileManager.getLangConfig().getCannotUseElytraWhileTentacleEffect());
            }
        }
    }

    private boolean isValidTentacleEffect(CustomItemInteractEvent event) {
        if (!(event.getCustomItem() instanceof TentacleEffectItem item)) return false;
        return item.isEnabled();
    }

    private List<Player> findValidTargets(Player source, double radius) {
        return EntityUtil.findNearbyEntities(source.getLocation(), radius, Player.class).stream()
                .filter(player -> player != source)
                .filter(player -> !player.hasMetadata("NPC"))
                .filter(player -> !WorldGuardUtil.isDeniedFlag(player.getLocation(), player, Flags.PVP))
                .toList();
    }

    private void startEffectTask(Player source, Player target, TentacleEffectItem item) {
        TentacleEffectTask task = new TentacleEffectTask(source, target, item, this);
        task.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
        activeEffects.put(target.getUniqueId(), task);
        item.notifyOpponents(target);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (activeEffects.containsKey(uuid)) {
            activeEffects.remove(uuid).cancel();
        }

        if (removedElytras.containsKey(uuid)) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(),
                    removedElytras.remove(uuid)
            );
        }
    }

    void handleElytra(Player target, TentacleEffectItem item, boolean remove) {
        if (remove) {
            removeElytra(target, item);
        } else {
            returnElytra(target, item);
        }
    }

    private void removeElytra(Player target, TentacleEffectItem item) {
        if (!shouldRemoveElytra(target, item)) return;

        ItemStack elytra = target.getInventory().getChestplate();
        target.getInventory().setChestplate(null);
        removedElytras.put(target.getUniqueId(), elytra);
    }

    private boolean shouldRemoveElytra(Player target, TentacleEffectItem item) {
        return item.isRemoveElytra() &&
                target.getInventory().getChestplate() != null &&
                target.getInventory().getChestplate().getType() == Material.ELYTRA;
    }

    private void returnElytra(Player target, TentacleEffectItem item) {
        if (!removedElytras.containsKey(target.getUniqueId())) return;

        ItemStack elytra = removedElytras.get(target.getUniqueId());
        if (returnElytraToInventory(target, elytra, item)) {
            removedElytras.remove(target.getUniqueId());
        }
    }

    private boolean returnElytraToInventory(Player target, ItemStack elytra, TentacleEffectItem item) {
        if (target.getInventory().getChestplate() == null) {
            target.getInventory().setChestplate(elytra);
            return true;
        }

        if (target.getInventory().firstEmpty() != -1) {
            target.getInventory().addItem(elytra);
            return true;
        }

        target.getWorld().dropItemNaturally(target.getLocation(), elytra);
        MessageUtil.sendMessage(target, item.getDropElytraGround());
        return true;
    }
}

class TentacleEffectTask extends BukkitRunnable {
    private final Player source;
    private final Player target;
    private final TentacleEffectItem item;
    private final TentacleEffectListener listener;
    private int ticks = 0;
    private final int maxTicks;
    private boolean elytraRemoved = false;

    public TentacleEffectTask(Player source, Player target, TentacleEffectItem item, TentacleEffectListener listener) {
        this.source = source;
        this.target = target;
        this.item = item;
        this.listener = listener;
        this.maxTicks = item.getGlowDuration() * 20;
    }

    @Override
    public void run() {
        if (!isValid()) {
            cleanup();
            return;
        }

        drawParticleLine();
        applyEffects();
        handleElytra();
        ticks++;
    }

    private boolean isValid() {
        return source.isOnline() && target.isOnline() && ticks < maxTicks;
    }

    private void cleanup() {
        cancel();
        listener.getActiveEffects().remove(target.getUniqueId());
        if (elytraRemoved) {
            listener.handleElytra(target, item, false);
        }
    }

    private void drawParticleLine() {
        Location start = source.getLocation().add(0, 1, 0);
        Location end = target.getLocation().add(0, 1, 0);
        Vector direction = end.toVector().subtract(start.toVector());
        double length = direction.length();
        direction.normalize();

        ParticleConfig particleConfig = item.getParticleConfig();

        org.bukkit.Particle.DustOptions bukkitDustOptions = null;
        if (particleConfig.getDustOptions() != null) {
            bukkitDustOptions = new org.bukkit.Particle.DustOptions(
                    org.bukkit.Color.fromRGB(
                            particleConfig.getDustOptions().getRed(),
                            particleConfig.getDustOptions().getGreen(),
                            particleConfig.getDustOptions().getBlue()
                    ),
                    (float) particleConfig.getDustOptions().getSize()
            );
        }

        for (double i = 0; i < length; i += 0.5) {
            Location particleLoc = start.clone().add(direction.clone().multiply(i));
            target.getWorld().spawnParticle(
                    particleConfig.getParticle(),
                    particleLoc,
                    particleConfig.getCount(),
                    particleConfig.getOffSetX(),
                    particleConfig.getOffSetY(),
                    particleConfig.getOffSetZ(),
                    particleConfig.getExtra(),
                    bukkitDustOptions
            );
        }
    }

    private void applyEffects() {
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 40, 0));
    }

    private void handleElytra() {
        if (!elytraRemoved) {
            listener.handleElytra(target, item, true);
            elytraRemoved = true;
        }
    }
}