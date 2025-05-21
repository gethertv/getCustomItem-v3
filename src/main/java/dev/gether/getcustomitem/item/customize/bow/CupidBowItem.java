package dev.gether.getcustomitem.item.customize.bow;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("cupid_bow")
@SuperBuilder
@NoArgsConstructor
public class CupidBowItem extends CustomItem {
    private ParticleConfig particleConfig;
    private String ignorePermission;
    private List<PotionEffectConfig> potionEffectConfigs;
    private double chance;
    private double maxRange;
    private Material projectileMaterial;
    private boolean autoReload = true;
    private boolean disableDamage = true;


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
                "{chance}", String.valueOf(chance)
        );
    }

    @JsonIgnore
    public static CupidBowItem createDefaultItem() {
        return CupidBowItem.builder()
                .enabled(true)
                .itemID("cupid_bow")
                .categoryName("cupid_bow_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BOW)
                        .name("#40ffe9Cupid's Bow")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#85fff1× Hit player and",
                                        "#85fff1× give victim blindness",
                                        "#85fff1× Chance: #c2fff8{chance}%",
                                        "&7",
                                        "&7&7• Usage: #85fff1{usage}",
                                        "&7"
                                )
                        ))
                        .enchantments(new HashMap<>(Map.of(
                                Enchantment.ARROW_FIRE, 1,
                                Enchantment.ARROW_INFINITE, 1,
                                Enchantment.ARROW_DAMAGE, 6,
                                Enchantment.DURABILITY, 3
                        )))
                        .unbreakable(true)
                        .glow(false)
                        .build())
                .itemType(ItemType.CUPIDS_BOW)
                .cooldown(10)
                .permissionBypass("getcustomitem.cupidbow.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.HEART)
                        .build())
                .ignorePermission("getcustomitem.cupidbow.ignore")
                .chance(50)
                .maxRange(25)
                .projectileMaterial(Material.ARROW)
                .cooldownMessage(true)
                .potionEffectConfigs(new ArrayList<>(List.of(
                        new PotionEffectConfig("BLINDNESS", 5, 1)
                )))
                .build();
    }
}
