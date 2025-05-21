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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("push_item")
@SuperBuilder
@NoArgsConstructor
public class PushItem extends CustomItem {

    private double chance;
    private double pushPower;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance),
                "{power-push}", String.valueOf(pushPower)
        );
    }

    @JsonIgnore
    public static PushItem createDefaultItem() {
        return PushItem.builder()
                .enabled(true)
                .itemID("push_item")
                .categoryName("push_item_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLAZE_ROD)
                        .name("#9e9e9ePush stick")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Use this item",
                                        "#666666  to push opponents",
                                        "#666666  chance: {chance}%",
                                        "#666666  power: {power-push}",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.PUSH_ITEM)
                .cooldown(10)
                .permissionBypass("getcustomitem.pushitem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(100f)
                .pushPower(1.5)
                .build();
    }

}