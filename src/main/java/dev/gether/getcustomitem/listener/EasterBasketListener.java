package dev.gether.getcustomitem.listener;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.EasterBasketItem;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EasterBasketListener extends AbstractCustomItemListener<EasterBasketItem>{

    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();

    public EasterBasketListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if (!(event.getCustomItem() instanceof EasterBasketItem easterBasketItem)) return;
        if (!easterBasketItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, easterBasketItem, event.getItemStack(), event.getEquipmentSlot())) return;
        if (WorldGuardUtil.isDeniedFlag(victim.getLocation(), victim, Flags.PVP)) return;

        easterBasketItem.playSound(damager.getLocation());
        easterBasketItem.notifyYourself(damager);

        boolean foundTotem = false;
        if (easterBasketItem.isRemoveTotem()) {
            if (removeTotems(damager, victim, easterBasketItem)) {
                foundTotem = true;
            }
        } else {
            if (hasTotemInInventory(victim)) {
                foundTotem = true;
                startEffectTask(damager, victim, easterBasketItem, false);
            }
        }

        if (!foundTotem) {
            MessageUtil.sendMessage(damager, easterBasketItem.getNoTotemsMessage());
        }

    }


    private void startEffectTask(Player source, Player target, EasterBasketItem item, boolean shouldRemoveTotem) {
        EasterBasketTask task = new EasterBasketTask(source, target, item, this, shouldRemoveTotem);
        task.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
        activeEffects.put(target.getUniqueId(), task);
    }

    private boolean hasTotemInInventory(Player player) {
        if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) return true;
        if (player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) return true;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                return true;
            }
        }

        return false;
    }

    boolean removeTotems(Player source, Player target, EasterBasketItem item) {
        boolean totemRemoved = false;

        ItemStack mainHand = target.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING) {
            ItemStack totemClone = mainHand.clone();
            target.getInventory().setItemInMainHand(null);

            HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(totemClone);
            if (!leftover.isEmpty()) {
                target.getWorld().dropItemNaturally(target.getLocation(), totemClone);
            }
            totemRemoved = true;
        }

        if (!totemRemoved) {
            ItemStack offHand = target.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
                ItemStack totemClone = offHand.clone();
                target.getInventory().setItemInOffHand(null);

                HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(totemClone);
                if (!leftover.isEmpty()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), totemClone);
                }
                totemRemoved = true;
            }
        }

        if (!totemRemoved) {
            for (int i = 0; i < target.getInventory().getSize(); i++) {
                ItemStack xitem = target.getInventory().getItem(i);
                if (xitem != null && xitem.getType() == Material.TOTEM_OF_UNDYING) {
                    target.getInventory().setItem(i, null);

                    HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(xitem);
                    if (!leftover.isEmpty()) {
                        target.getWorld().dropItemNaturally(target.getLocation(), leftover.get(0));
                    }

                    totemRemoved = true;
                    break;
                }
            }
        }

        if (totemRemoved) {
            String targetName = target.getName();
            String sourceName = source.getName();

            MessageUtil.sendMessage(source, item.getTotemRemovedMessage().replace("{player}", targetName));
            MessageUtil.sendMessage(target, item.getTotemLostMessage().replace("{player}", sourceName));
        }

        return totemRemoved;
    }

    Map<UUID, BukkitRunnable> getActiveEffects() {
        return activeEffects;
    }
}

class EasterBasketTask extends BukkitRunnable {
    private final Player source;
    private final Player target;
    private final EasterBasketItem item;
    private final EasterBasketListener listener;
    private int ticks = 0;
    private final int maxTicks;
    private final boolean shouldRemoveTotem;

    public EasterBasketTask(Player source, Player target, EasterBasketItem item, EasterBasketListener listener, boolean shouldRemoveTotem) {
        this.source = source;
        this.target = target;
        this.item = item;
        this.listener = listener;
        this.maxTicks = item.getEffectDuration() * 20;
        this.shouldRemoveTotem = shouldRemoveTotem;
    }

    @Override
    public void run() {
        if (!isValid()) {
            cleanup();
            return;
        }

        drawParticleLine();
        applyEffects();

        if (shouldRemoveTotem && ticks == 0) {
            listener.removeTotems(source, target, item);
        }

        ticks++;
    }

    private boolean isValid() {
        return source.isOnline() && target.isOnline() && ticks < maxTicks;
    }

    private void cleanup() {
        cancel();
        listener.getActiveEffects().remove(target.getUniqueId());
    }

    private void drawParticleLine() {
        org.bukkit.Location start = source.getLocation().add(0, 1, 0);
        org.bukkit.Location end = target.getLocation().add(0, 1, 0);
        org.bukkit.util.Vector direction = end.toVector().subtract(start.toVector());
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
            org.bukkit.Location particleLoc = start.clone().add(direction.clone().multiply(i));
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
}