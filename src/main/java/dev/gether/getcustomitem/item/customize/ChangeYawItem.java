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
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonTypeName("change_yaw")
@SuperBuilder
@NoArgsConstructor
@Getter
public class ChangeYawItem extends CustomItem {

    private double chance;
    private float pitchDegrees;
    private Set<EquipmentSlot> equipmentSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance),
                "{pitchDegrees}", String.valueOf(pitchDegrees)
        );
    }

    @JsonIgnore
    public static ChangeYawItem createDefaultItem() {
        return ChangeYawItem.builder()
                .enabled(true)
                .itemID("change_yaw")
                .categoryName("change_yaw_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLAZE_ROD)
                        .name("#ff4d4dChange Yaw Rod")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff6666× Use this rod to change player's yaw",
                                        "#ff6666× Chance: &f{chance}%",
                                        "#ff6666× Pitch: &f{pitchDegrees}°",
                                        "&7",
                                        "&7• Usage: #ff6666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.CHANGE_YAW)
                .cooldown(15)
                .permissionBypass("getcustomitem.changeyaw.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(50.0)
                .pitchDegrees(90.0f)
                .equipmentSlots(new HashSet<>(Set.of(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)))
                .build();
    }
}