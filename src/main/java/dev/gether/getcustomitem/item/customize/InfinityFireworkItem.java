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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("infinity_firework")
@SuperBuilder
@NoArgsConstructor
public class InfinityFireworkItem extends CustomItem {


    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
        );
    }

    @JsonIgnore
    public static InfinityFireworkItem createDefaultItem() {
        return InfinityFireworkItem.builder()
                .enabled(true)
                .itemID("infinity_firework")
                .categoryName("firework_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.FIREWORK_ROCKET)
                        .name("#1e90ffInfinity Firework")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#4db8ff× Special firework that",
                                        "#4db8ff  never runs out",
                                        "#4db8ff  activation chance: {chance}%",
                                        "&7",
                                        "&7• Usage: #4db8ffInfinite",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.INFINITY_FIREWORK)
                .cooldown(3)
                .permissionBypass("getcustomitem.infinityfirework.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH)
                        .build())
                .cooldownMessage(true)
                .build();
    }
}