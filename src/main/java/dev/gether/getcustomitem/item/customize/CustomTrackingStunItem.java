package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.ParticlesUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("tracking_stun")
@SuperBuilder
@NoArgsConstructor
public class CustomTrackingStunItem extends CustomItem {
    private int searchRadius;
    private int particleDuration;
    private int stunDuration;
    private double particleSpeed;
    private ParticleConfig particleConfig;

    public void shootTrackingParticles(Player source, Player target) {
        if (!particleConfig.isEnable()) return;

        Location particleLoc = source.getLocation().clone().add(0, 1, 0);
        Location targetLoc = target.getLocation().clone().add(0, 1, 0);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = particleDuration * 20;
            boolean hit = false;

            @Override
            public void run() {
                if (!source.isOnline() || !target.isOnline() || ticks >= maxTicks || hit) {
                    cancel();
                    return;
                }

                // Sprawdź kolizję
                if (particleLoc.distance(targetLoc) < 1.0) {
                    hit = true;
                    applyStun(target);
                    cancel();
                    return;
                }

                // Aktualizuj pozycję celu
                targetLoc.setX(target.getLocation().getX());
                targetLoc.setY(target.getLocation().getY() + 1);
                targetLoc.setZ(target.getLocation().getZ());

                // Oblicz kierunek do celu
                Vector direction = targetLoc.toVector().subtract(particleLoc.toVector()).normalize().multiply(particleSpeed);
                particleLoc.add(direction);

                // Wyświetl cząsteczki
                ParticlesUtil.spawnParticles(particleLoc, particleConfig);

                ticks++;
            }
        }.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
    }

    private void applyStun(Player target) {
        // Dodaj metadatę
        target.setMetadata(MetadataStorage.STUNNED,
                new FixedMetadataValue(GetCustomItem.getInstance(), stunDuration));

        // Zagraj dźwięk i powiadom
        playSound(target.getLocation());
        notifyOpponents(target);

        // Zaplanuj usunięcie stuna
        new BukkitRunnable() {
            @Override
            public void run() {
                removeStun(target);
            }
        }.runTaskLater(GetCustomItem.getInstance(), stunDuration * 20L);
    }

    public void removeStun(Player player) {
        if (player.isOnline()) {
            player.removeMetadata(MetadataStorage.STUNNED, GetCustomItem.getInstance());
            player.setCustomNameVisible(false);
        }
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{search_radius}", String.valueOf(searchRadius),
                "{particle_duration}", String.valueOf(particleDuration),
                "{stun_duration}", String.valueOf(stunDuration)
        );
    }

    @JsonIgnore
    public static CustomTrackingStunItem createDefaultItem() {
        return CustomTrackingStunItem.builder()
                .enabled(true)
                .itemID("tracking_stun")
                .categoryName("stun_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.SHULKER_SHELL)
                        .name("#aa00ffTracking Stun")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#aa00ffSearch Radius: &f{search_radius} blocks",
                                        "#aa00ffParticle Duration: &f{particle_duration}s",
                                        "#aa00ffStun Duration: &f{stun_duration}s",
                                        "&7",
                                        "&7• Usage: #aa00ff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.TRACKING_STUN)
                .cooldown(15)
                .permissionBypass("getcustomitem.trackingstun.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .searchRadius(5)
                .particleDuration(5)
                .stunDuration(3)
                .particleSpeed(0.2)
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.REDSTONE)
                        .count(1)
                        .offSetX(0)
                        .offSetY(0)
                        .offSetZ(0)
                        .extra(1)
                        .dustOptions(new DustOptions(170, 0, 255, 1))
                        .build())
                .build();
    }
}