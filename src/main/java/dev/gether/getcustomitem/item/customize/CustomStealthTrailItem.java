package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("custom_stealth_trail")
@SuperBuilder
@NoArgsConstructor
public class CustomStealthTrailItem extends CustomItem {
    private int duration;
    private double trailLength;
    private ParticleConfig particleConfig;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{duration}", String.valueOf(duration),
                "{trail_length}", String.valueOf(trailLength)
        );
    }

    public BukkitTask runTrailParticles(LinkedList<Location> trail) {
        if (!particleConfig.isEnable())
            return null;

        return new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> trailCopy;
                synchronized (trail) {
                    trailCopy = new ArrayList<>(trail);
                }
                for (Location loc : trailCopy) {
                    ParticlesUtil.spawnParticles(loc, particleConfig);
                }
            }
        }.runTaskTimerAsynchronously(GetCustomItem.getInstance(), 0L, 2L);
    }

    @JsonIgnore
    public static CustomStealthTrailItem createDefaultItem() {
        return CustomStealthTrailItem.builder()
                .enabled(true)
                .itemID("stealth_trail")
                .categoryName("stealth_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.ENDER_EYE)
                        .name("#8a2be2Stealth Trail")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#8a2be2Duration: &f{duration}s",
                                        "#8a2be2Trail Length: &f{trail_length} blocks",
                                        "&7",
                                        "&7• Usage: #8a2be2{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.CUSTOM_STEALTH_TRAIL)
                .cooldown(30)
                .permissionBypass("getcustomitem.stealthtrail.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .duration(10)
                .trailLength(5.0)
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.REDSTONE)
                        .count(1)
                        .offSetX(0)
                        .offSetY(0)
                        .offSetZ(0)
                        .extra(1)
                        .dustOptions(new DustOptions(128, 0, 128, 1))
                        .build())
                .build();
    }
}