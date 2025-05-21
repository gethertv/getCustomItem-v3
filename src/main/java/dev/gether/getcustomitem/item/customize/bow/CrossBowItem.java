package dev.gether.getcustomitem.item.customize.bow;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.ParticlesUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CrossBowItem extends CustomItem {
    private ParticleConfig particleConfig;
    private String ignorePermission;
    private double chance;
    private double maxRange;
    private Material projectileMaterial;
    private boolean autoReload = true;
    private boolean safeTeleport;
    private boolean disableDamage = false;

    public void runParticles(Entity arrow) {

        // check the particles is enabled
        if(!particleConfig.isEnable())
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                // check if the arrow has landed or is removed
                if (arrow.isOnGround() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }

                ParticlesUtil.spawnParticles(arrow, particleConfig);
            }
        }.runTaskTimerAsynchronously(GetCustomItem.getInstance(), 0L, 0L);
    }
    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance),
                "{distance}", String.valueOf(maxRange)
        );
    }

    @JsonIgnore
    public static CrossBowItem createDefaultItem() {
        return CrossBowItem.builder()
                .enabled(true)
                .itemID("crossbow")
                .categoryName("crossbow_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.CROSSBOW)
                        .name("#40ffe9Teleporting crossbow")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#85fff1× Hit a player and move him to you",
                                        "#85fff1× Chance: #c2fff8{chance}%",
                                        "&7",
                                        "&7&7• Usage: #85fff1{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.CROSSBOW)
                .projectileMaterial(Material.ARROW)
                .cooldown(10)
                .permissionBypass("getcustomitem.crossbow.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
//                .defaultNotifications()
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.HEART)
                        .build())
                .ignorePermission("getcustomitem.crossbow.effect.bypass")
                .chance(50)
                .maxRange(25)
                .cooldownMessage(true)
                .safeTeleport(true)
                .build();
    }
}
