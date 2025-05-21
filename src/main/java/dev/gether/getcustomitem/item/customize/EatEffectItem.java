package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("eat_effect")
@SuperBuilder
@NoArgsConstructor
public class EatEffectItem extends CustomItem {

    private boolean affectSelf;
    private boolean affectOthers;
    private int radius;
    private List<PotionEffectConfig> potionEffects;
    private List<String > removeEffects;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of("{radius}", String.valueOf(radius));
    }

    @JsonIgnore
    public static EatEffectItem createDefaultItem() {
        return EatEffectItem.builder()
                .enabled(true)
                .itemID("eat_effect_item")
                .categoryName("eat_effect_category")
                .usage(3)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.GOLDEN_APPLE)
                        .name("#00ffffEat Effect Apple")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#90c2ffGives or removes effects in a",
                                        "#90c2ff&f{radius} &7radius when eaten",
                                        "&7",
                                        "&7Usage: #ff004c{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.EAT_EFFECT)
                .cooldown(10)
                .permissionBypass("getcustomitem.eateffect.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .affectSelf(true)
                .affectOthers(true)
                .radius(5)
                .potionEffects(new ArrayList<>(List.of(
                        new PotionEffectConfig("SPEED", 3, 1)
                )))
                .removeEffects(new ArrayList<>())
                .build();
    }
}
