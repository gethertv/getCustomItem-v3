package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

@Getter
@Setter
@JsonTypeName("shield_item")
@SuperBuilder
@NoArgsConstructor
public class ShieldItem extends CustomItem {

    private double blockChance;
    private Set<EquipmentSlot> equipmentSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(blockChance)
        );
    }

    @JsonIgnore
    public static ShieldItem createDefaultItem() {
        return ShieldItem.builder()
                .enabled(true)
                .itemID("shield_item")
                .categoryName("shield_item_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.RED_DYE)
                        .name("#9e9e9eShield")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Hand this item",
                                        "#666666  and block hit",
                                        "#666666  block chance {chance}%",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SHIELD_ITEM)
                .cooldown(10)
                .permissionBypass("getcustomitem.lightningitem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(false)
                .blockChance(30)
                .equipmentSlots(new HashSet<>(Set.of(
                        EquipmentSlot.OFF_HAND
                )))
                .build();
    }

}
