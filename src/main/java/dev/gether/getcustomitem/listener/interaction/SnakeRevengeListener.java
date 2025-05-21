package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.SnakeRevengeItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SnakeRevengeListener extends AbstractCustomItemListener<SnakeRevengeItem> {

    private final CustomStealthTrailListener stealthTrailListener;

    public SnakeRevengeListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager, CustomStealthTrailListener stealthTrailListener) {
        super(itemManager, cooldownManager, fileManager);
        this.stealthTrailListener = stealthTrailListener;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!isValidSnakeRevenge(event)) return;

        SnakeRevengeItem item = (SnakeRevengeItem) event.getCustomItem();
        Player player = event.getPlayer();

        if (!canUseItem(player, item, event.getItemStack(), event.getEquipmentSlot())) return;

        item.playSound(player.getLocation());
        List<Player> targets = findValidTargets(player, item.getSearchRadius());

        int revealCount = 0;
        for (Player target : targets) {
            if (isHiddenByStealthEffect(target)) {
                removeStealthEffect(target);
                item.notifyOpponents(target);
                spawnParticlesBetween(player, target, item);
                revealCount++;
            }
        }

        if (revealCount > 0) {
            MessageUtil.sendMessage(player, item.getSnakeEffectRemovedMessage().replace("{count}", String.valueOf(revealCount)));
        } else {
            MessageUtil.sendMessage(player, item.getNoSnakeEffectMessage());
        }
    }

    private boolean isValidSnakeRevenge(CustomItemInteractEvent event) {
        if (!(event.getCustomItem() instanceof SnakeRevengeItem item)) return false;
        return item.isEnabled();
    }

    private List<Player> findValidTargets(Player source, double radius) {
        return EntityUtil.findNearbyEntities(source.getLocation(), radius, Player.class).stream()
                .filter(player -> player != source)
                .filter(player -> !player.hasMetadata("NPC"))
                .filter(player -> !WorldGuardUtil.isDeniedFlag(player.getLocation(), player, Flags.PVP))
                .toList();
    }

    private boolean isHiddenByStealthEffect(Player player) {
        return player.hasMetadata(MetadataStorage.CUSTOM_ITEM);
    }

    private void removeStealthEffect(Player target) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != target) {
                onlinePlayer.showPlayer(GetCustomItem.getInstance(), target);
            }
        }

        if (target.hasMetadata(MetadataStorage.CUSTOM_ITEM)) {
            target.removeMetadata(MetadataStorage.CUSTOM_ITEM, GetCustomItem.getInstance());
        }

        if (stealthTrailListener != null) {
            try {
                java.lang.reflect.Method method = CustomStealthTrailListener.class.getDeclaredMethod("removeStealthEffect", Player.class);
                method.setAccessible(true);
                method.invoke(stealthTrailListener, target);
            } catch (Exception ignored) {
            }
        }

        target.getWorld().spawnParticle(
                Particle.SMOKE_LARGE,
                target.getLocation().add(0, 1, 0),
                30,
                0.5,
                1.0,
                0.5,
                0.05
        );
    }

    private void spawnParticlesBetween(Player source, Player target, SnakeRevengeItem item) {
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
}