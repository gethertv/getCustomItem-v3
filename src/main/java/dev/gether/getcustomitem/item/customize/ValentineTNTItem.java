package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("valentine_tnt")
@SuperBuilder
@NoArgsConstructor
public class ValentineTNTItem extends CustomItem {
    private double chance;
    private int explosionDelay;
    private double damagePercent;
    private ParticleConfig particleConfig;

    private double followHeight = 2.0;
    private double followDistance = 0.1;
    private int updateRate = 2;
    private double particleRadius = 0.5;
    private int particlePoints = 8;

    @JsonIgnore
    private ParticleConfig getDefaultParticleConfig() {
        return ParticleConfig.builder()
                .enable(true)
                .particle(Particle.HEART)
                .count(1)
                .offSetX(0.3)
                .offSetY(0.3)
                .offSetZ(0.3)
                .extra(0.1)
                .build();
    }

    @JsonIgnore
    public ParticleConfig getParticleConfig() {
        return particleConfig != null ? particleConfig : getDefaultParticleConfig();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.format("%.1f", chance),
                "{delay}", String.valueOf(explosionDelay / 20),
                "{damage}", String.format("%.1f", damagePercent),
                "{height}", String.format("%.1f", followHeight),
                "{rate}", String.valueOf(updateRate)
        );
    }

    @JsonIgnore
    public static ValentineTNTItem createDefaultItem() {
        return ValentineTNTItem.builder()
                .enabled(true)
                .itemID("valentine_tnt")
                .categoryName("valentine_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TNT)
                        .name("#ff69b4Valentine's TNT")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff69b4Chance: &f{chance}%",
                                        "#ff69b4Explosion Delay: &f{delay}s",
                                        "#ff69b4Damage: &f{damage}% of current HP",
                                        "#ff69b4Height: &f{height} blocks",
                                        "#ff69b4Update Rate: &fevery {rate} ticks",
                                        "&7",
                                        "&7• Usage: #ff69b4{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.VALENTINE_TNT)
                .cooldown(10)
                .permissionBypass("getcustomitem.valentine.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(25.0)
                .explosionDelay(60)
                .damagePercent(30.0)
                .followHeight(2.0)
                .followDistance(0.1)
                .updateRate(2)
                .particleRadius(0.5)
                .particlePoints(8)
                .build();
    }
}