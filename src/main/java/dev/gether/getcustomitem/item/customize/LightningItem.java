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
@JsonTypeName("lighting_item")
@SuperBuilder
@NoArgsConstructor
public class LightningItem extends CustomItem {

    private double multiplyDamage;
    private boolean takeHeart;
    private double chance;
    private double heartPercentage;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.format("%.2f", chance)
        );
    }

    @JsonIgnore
    public static LightningItem createDefaultItem() {
        return LightningItem.builder()
                .enabled(true)
                .itemID("lightning_item")
                .categoryName("lightning_item_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.DIAMOND_AXE)
                        .name("#9e9e9eLightning Axe")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Hit player",
                                        "#666666  and shoot lightning",
                                        "#666666  taking damage or heart",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.LIGHTNING_ITEM)
                .cooldown(10)
                .permissionBypass("getcustomitem.lightningitem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(100)
                .multiplyDamage(0.3)
                .takeHeart(true)
                .heartPercentage(0.33)
                .build();
    }
}
