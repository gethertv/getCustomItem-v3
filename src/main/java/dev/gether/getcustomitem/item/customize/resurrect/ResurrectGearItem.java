package dev.gether.getcustomitem.item.customize.resurrect;

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
@JsonTypeName("resurrect_gear")
@SuperBuilder
@NoArgsConstructor
public class ResurrectGearItem extends CustomItem {
    private double chanceLostItem;
    private Set<EquipmentSlot> equipmentSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chanceLostItem),
                "{slots}", formatEquipmentSlots()
        );
    }

    private String formatEquipmentSlots() {
        if (equipmentSlots == null || equipmentSlots.isEmpty()) {
            return "Any";
        }
        
        List<String> slotNames = new ArrayList<>();
        for (EquipmentSlot slot : equipmentSlots) {
            slotNames.add(slot.name());
        }
        return String.join(", ", slotNames);
    }

    @JsonIgnore
    public static ResurrectGearItem createDefaultItem() {
        return ResurrectGearItem.builder()
                .enabled(true)
                .itemID("resurrect_gear")
                .categoryName("resurrect_gear_category")
                .usage(1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.NETHERITE_HELMET)
                        .name("#ff9900Resurrect Helmet")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ffb84dWhen equipped, teleports you to spawn",
                                        "#ffb84don death with a chance to keep items",
                                        "#ffb84ditem keep chance: &f{chance}%",
                                        "&7",
                                        "#ffb84dValid slots: &f{slots}",
                                        "&7",
                                        "&7• Usage: #ffb84d{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.RESURRECT_GEAR)
                .cooldown(60)
                .permissionBypass("getcustomitem.resurrectgear.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .chanceLostItem(50.0)
                .equipmentSlots(new HashSet<>(Collections.singletonList(EquipmentSlot.HEAD)))
                .cooldownMessage(true)
                .build();
    }
}