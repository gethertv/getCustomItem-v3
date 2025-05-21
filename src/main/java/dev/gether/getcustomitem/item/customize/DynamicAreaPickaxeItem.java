package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonTypeName("dynamic_area_pickaxe")
@SuperBuilder
@NoArgsConstructor
@Getter
public class DynamicAreaPickaxeItem extends CustomItem {

    private int miningWidth;
    private int miningHeight;
    private int miningDepth;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{mining_width}", String.valueOf(miningWidth),
                "{mining_height}", String.valueOf(miningHeight),
                "{mining_depth}", String.valueOf(miningDepth)
        );
    }

    @JsonIgnore
    public static DynamicAreaPickaxeItem createDefaultItem() {
        return DynamicAreaPickaxeItem.builder()
                .enabled(true)
                .itemID("dynamic_area_pickaxe")
                .categoryName("tools_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.DIAMOND_PICKAXE)
                        .name("#4287f5Dynamic Area Pickaxe")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#42c5f5× Mine blocks in a configurable area!",
                                        "&7",
                                        "&7• Width: #42c5f5{mining_width}",
                                        "&7• Height: #42c5f5{mining_height}",
                                        "&7• Depth: #42c5f5{mining_depth}",
                                        "&7• Usage: #42c5f5{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.DYNAMIC_AREA_PICKAXE)
                .cooldown(-1)
                .permissionBypass("getcustomitem.dynamicpickaxe.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .miningWidth(3)
                .miningHeight(3)
                .miningDepth(3)
                .build();
    }
}