package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
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
@JsonTypeName("tentacle_effect")
@SuperBuilder
@NoArgsConstructor
public class TentacleEffectItem extends CustomItem {
    // Core settings
    private int searchRadius;
    private int glowDuration;
    private boolean removeElytra;

    private String dropElytraGround = "&cYour inventory was full - elytra dropped on ground!";

    private ParticleConfig particleConfig;

    @JsonIgnore
    private ParticleConfig getDefaultParticleConfig() {
        return ParticleConfig.builder()
                .enable(true)
                .particle(Particle.REDSTONE)
                .count(15)
                .offSetX(0.1)
                .offSetY(0.1)
                .offSetZ(0.1)
                .extra(0.01)
                .dustOptions(new DustOptions(164, 53, 240, 1))
                .build();
    }

    @JsonIgnore
    public ParticleConfig getParticleConfig() {
        return particleConfig != null ? particleConfig : getDefaultParticleConfig();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius}", String.valueOf(searchRadius),
                "{duration}", String.valueOf(glowDuration)
        );
    }

    @JsonIgnore
    public static TentacleEffectItem createDefaultItem() {
        return TentacleEffectItem.builder()
                .enabled(true)
                .itemID("tentacle_effect")
                .categoryName("tentacle_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.ENDER_EYE)
                        .name("#a435f0Tentacle Eye")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#a435f0Search Radius: &f{radius} blocks",
                                        "#a435f0Glow Duration: &f{duration}s",
                                        "&7",
                                        "&7• Usage: #a435f0{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.TENTACLE_EFFECT)
                .cooldown(10)
                .permissionBypass("getcustomitem.tentacle.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .searchRadius(5)
                .glowDuration(3)
                .removeElytra(true)
                .build();
    }
}