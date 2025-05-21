package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.CustomSphereItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.PotionConverUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class CustomSphereListener extends AbstractCustomItemListener<CustomSphereItem> {
    private final Set<Player> activeSpheres = new HashSet<>();

    public CustomSphereListener(ItemManager itemManager,
                                CooldownManager cooldownManager,
                                FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CustomSphereItem sphereItem)) return;
        if (!sphereItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, sphereItem, event.getItemStack(), event.getEquipmentSlot())) return;

        Location targetLoc = player.getLocation().add(player.getLocation().getDirection().multiply(sphereItem.getRadius()));
        if (WorldGuardUtil.isDeniedFlag(targetLoc, player, Flags.PVP)) return;

        if (activeSpheres.contains(player)) return;

        startSphereEffect(player, sphereItem);
    }

    private void startSphereEffect(Player player, CustomSphereItem sphereItem) {
        activeSpheres.add(player);
        sphereItem.playSound(player.getLocation());
        sphereItem.notifyYourself(player);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = sphereItem.getDuration() * 20;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    activeSpheres.remove(player);
                    cancel();
                    return;
                }

                Location loc = player.getLocation();
                renderSphere(loc, sphereItem);

                if (ticks % 20 == 0) {
                    applyEffects(loc, player, sphereItem);
                }

                ticks++;
            }
        }.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
    }

    private void renderSphere(Location center, CustomSphereItem sphereItem) {
        double radius = sphereItem.getRadius();
        ParticleConfig config = sphereItem.getParticleConfig();

        if (!config.isEnable()) return;

        org.bukkit.Particle.DustOptions bukkitDustOptions = null;
        if (config.getDustOptions() != null) {
            bukkitDustOptions = new org.bukkit.Particle.DustOptions(
                    org.bukkit.Color.fromRGB(
                            config.getDustOptions().getRed(),
                            config.getDustOptions().getGreen(),
                            config.getDustOptions().getBlue()
                    ),
                    (float) config.getDustOptions().getSize()
            );
        }

        for (int i = 0; i < config.getCount(); i++) {
            double u = Math.random();
            double v = Math.random();

            double theta = 2 * Math.PI * u;
            double phi = Math.acos(2 * v - 1);

            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Vector vector = new Vector(x, y, z);
            Location particleLoc = center.clone().add(vector);

            center.getWorld().spawnParticle(
                    config.getParticle(),
                    particleLoc,
                    1,
                    config.getOffSetX(),
                    config.getOffSetY(),
                    config.getOffSetZ(),
                    config.getExtra(),
                    bukkitDustOptions
            );
        }
    }

    private void applyEffects(Location center, Player source, CustomSphereItem sphereItem) {
        EntityUtil.findNearbyEntities(center, sphereItem.getRadius(), Player.class)
                .stream()
                .filter(e -> e != source)
                .filter(e -> !e.hasMetadata("NPC"))
                .filter(e -> !WorldGuardUtil.isDeniedFlag(e.getLocation(), e, Flags.PVP))
                .forEach(target -> {
                    PotionConverUtil.getPotionEffectFromConfig(sphereItem.getEffects())
                            .forEach(target::addPotionEffect);

                    if (sphereItem.isDamageInSphere()) {
                        target.damage(sphereItem.getDamagePerSecond());
                    }

                    sphereItem.notifyOpponents(target);
                });
    }
}