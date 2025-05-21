package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
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
@JsonTypeName("snake_revenge")
@SuperBuilder
@NoArgsConstructor
public class SnakeRevengeItem extends CustomItem {
    // Core settings
    private int searchRadius;
    private String noSnakeEffectMessage = "&cNo players with snake skin effect found!";
    private String snakeEffectRemovedMessage = "&aSnake skin effect removed from &e{count} &aplayers!";

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
                .dustOptions(new DustOptions(0, 255, 0, 1)) // Green color for snake theme
                .build();
    }

    @JsonIgnore
    public ParticleConfig getParticleConfig() {
        return particleConfig != null ? particleConfig : getDefaultParticleConfig();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius}", String.valueOf(searchRadius)
        );
    }

    @JsonIgnore
    public static SnakeRevengeItem createDefaultItem() {
        return SnakeRevengeItem.builder()
                .enabled(true)
                .itemID("snake_revenge")
                .categoryName("snake_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.FERMENTED_SPIDER_EYE)
                        .name("#00ff00Snake's Revenge")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#00ff00Search Radius: &f{radius} blocks",
                                        "&7",
                                        "&7• &fReveals hidden players with snake skin",
                                        "&7• Usage: #00ff00{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SNAKE_REVENGE) // You'll need to add this to ItemType enum
                .cooldown(10)
                .permissionBypass("getcustomitem.snakerevenge.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ENTITY_ENDER_DRAGON_HURT)
                        .build())
                .cooldownMessage(true)
                .searchRadius(5)
                .build();
    }
}