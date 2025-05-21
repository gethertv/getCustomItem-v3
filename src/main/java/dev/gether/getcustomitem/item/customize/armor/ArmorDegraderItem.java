package dev.gether.getcustomitem.item.customize.armor;

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
@JsonTypeName("armor_degrader")
@SuperBuilder
@NoArgsConstructor
public class ArmorDegraderItem extends CustomItem {
    private double durabilityDamagePercent;
    private Set<EquipmentSlot> targetEquipmentSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{degradation_percent}", String.valueOf(durabilityDamagePercent),
                "{target_slots}", formatTargetSlots()
        );
    }

    private String formatTargetSlots() {
        if (targetEquipmentSlots == null || targetEquipmentSlots.isEmpty()) {
            return "All armor";
        }

        List<String> slotNames = new ArrayList<>();
        for (EquipmentSlot slot : targetEquipmentSlots) {
            slotNames.add(slot.name());
        }
        return String.join(", ", slotNames);
    }

    @JsonIgnore
    public static ArmorDegraderItem createDefaultItem() {
        Set<EquipmentSlot> defaultTargetSlots = new HashSet<>(Arrays.asList(
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        ));

        return ArmorDegraderItem.builder()
                .enabled(true)
                .itemID("armor_degrader")
                .categoryName("armor_degrader_category")
                .usage(100)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TRIDENT)
                        .name("#ff9900Armor Shredder")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ffb84dDamages enemy armor durability",
                                        "#ffb84dby &f{degradation_percent}%&f of maximum",
                                        "#ffb84dTargets: &f{target_slots}",
                                        "&7",
                                        "&7• Usage: #ffb84d{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.ARMOR_DEGRADER)
                .cooldown(2)
                .permissionBypass("getcustomitem.armordegrader.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ITEM_TRIDENT_HIT)
                        .build())
                .durabilityDamagePercent(25.0)
                .targetEquipmentSlots(defaultTargetSlots)
                .cooldownMessage(true)
                .build();
    }
}