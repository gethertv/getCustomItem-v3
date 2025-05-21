package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.*;

@Getter
@Setter
@JsonTypeName("attribute_buff")
@SuperBuilder
@NoArgsConstructor
public class AttributeBuffItem extends CustomItem {

    private Map<String, Double> attributes = new HashMap<>();
    private boolean resetAfterDeath = false;

    @Override
    protected Map<String, String> replacementValues() {
        Map<String, String> values = new HashMap<>();
        attributes.forEach((attribute, value) ->
                values.put("{" + attribute.toLowerCase() + "}", String.format("%.1f", value))
        );
        return values;
    }

    @JsonIgnore
    public static AttributeBuffItem createDefaultItem() {
        return AttributeBuffItem.builder()
                .enabled(true)
                .itemID("attribute_buff")
                .categoryName("buff_category")
                .usage(1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLAZE_POWDER)
                        .name("&6Permanent Attribute Buff")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "&6Permanent attributes:",
                                        "&7• Attack Damage: &c+{generic_attack_damage}",
                                        "&7• Attack Speed: &c+{generic_attack_speed}",
                                        "&7• Max Health: &c+{generic_max_health}",
                                        "&7• Movement Speed: &c+{generic_movement_speed}",
                                        "&7",
                                        "&7• Usage: &6{usage}",
                                        "&7"
                                )
                        ))
                        .glow(true)
                        .build())
                .itemType(ItemType.ATTRIBUTE_BUFF)
                .attributes(Map.of(
                        "GENERIC_ATTACK_DAMAGE", 5.0,
                        "GENERIC_ATTACK_SPEED", 2.0,
                        "GENERIC_MAX_HEALTH", 4.0,
                        "GENERIC_MOVEMENT_SPEED", 0.2
                ))
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .build();
    }
}