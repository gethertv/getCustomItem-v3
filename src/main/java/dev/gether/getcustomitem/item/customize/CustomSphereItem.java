package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("custom_sphere")
@SuperBuilder
@NoArgsConstructor
public class CustomSphereItem extends CustomItem {
    private int duration;
    private double radius;
    private ParticleConfig particleConfig;
    private List<PotionEffectConfig> effects;
    private boolean damageInSphere;
    private double damagePerSecond;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{duration}", String.valueOf(duration),
                "{radius}", String.valueOf(radius),
                "{damage}", String.valueOf(damagePerSecond)
        );
    }

    @JsonIgnore
    private ParticleConfig getDefaultParticleConfig() {
        return ParticleConfig.builder()
                .enable(true)
                .particle(Particle.REDSTONE)
                .count(100)
                .offSetX(0)
                .offSetY(0)
                .offSetZ(0)
                .extra(0)
                .dustOptions(new DustOptions(0, 170, 255, 1))
                .build();
    }

    @JsonIgnore
    public ParticleConfig getParticleConfig() {
        return particleConfig != null ? particleConfig : getDefaultParticleConfig();
    }

    @JsonIgnore
    public static CustomSphereItem createDefaultItem() {
        return CustomSphereItem.builder()
                .enabled(true)
                .itemID("frost_sphere")
                .categoryName("sphere_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLUE_ICE)
                        .name("#00aaffFrost Sphere")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#00aaffDuration: &f{duration}s",
                                        "#00aaffRadius: &f{radius} blocks",
                                        "&7",
                                        "&7• Usage: #00aaff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.CUSTOM_SPHERE)
                .cooldown(15)
                .permissionBypass("getcustomitem.frostsphere.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .duration(5)
                .radius(4.0)
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.REDSTONE)
                        .count(100)
                        .offSetX(0)
                        .offSetY(0)
                        .offSetZ(0)
                        .extra(0)
                        .dustOptions(new DustOptions(0, 170, 255, 1))
                        .build())
                .effects(List.of(
                        new PotionEffectConfig("SLOW", 3*20, 1)
                ))
                .damageInSphere(false)
                .damagePerSecond(0.0)
                .build();
    }
}