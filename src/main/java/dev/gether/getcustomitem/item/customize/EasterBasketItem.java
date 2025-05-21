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
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("easter_basket")
@SuperBuilder
@NoArgsConstructor
public class EasterBasketItem extends CustomItem {
    // Core settings
    private int searchRadius;
    private int effectDuration;
    private boolean removeTotem;

    private String totemRemovedMessage = "&aZabrano totem graczowi &e{player}!";
    private String totemLostMessage = "&cTwój totem został zabrany przez &e{player}!";
    private String noTotemsMessage = "&cW pobliżu nie ma graczy z totemami!";
    
    private ParticleConfig particleConfig;

    @JsonIgnore
    private ParticleConfig getDefaultParticleConfig() {
        return ParticleConfig.builder()
                .enable(true)
                .particle(Particle.REDSTONE)
                .count(10)
                .offSetX(0.1)
                .offSetY(0.1)
                .offSetZ(0.1)
                .extra(0.01)
                .dustOptions(new DustOptions(255, 215, 0, 1)) // Złoty kolor
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
                "{duration}", String.valueOf(effectDuration)
        );
    }

    @JsonIgnore
    public static EasterBasketItem createDefaultItem() {
        return EasterBasketItem.builder()
                .enabled(true)
                .itemID("easter_basket")
                .categoryName("easter_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.FLOWER_POT)
                        .name("#ffdd00Koszyczek Wielkanocny")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ffdd00Zasięg działania: &f{radius} bloków",
                                        "#ffdd00Czas trwania efektu: &f{duration}s",
                                        "&7",
                                        "&7• &dZabiera totemy pobliskim graczom",
                                        "&7• Użycia: #ffdd00{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.EASTER_BASKET)
                .cooldown(30)
                .permissionBypass("getcustomitem.easterbasket.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ENTITY_RABBIT_AMBIENT)
                        .build())
                .cooldownMessage(true)
                .searchRadius(3)
                .effectDuration(5)
                .removeTotem(true)
                .build();
    }
}