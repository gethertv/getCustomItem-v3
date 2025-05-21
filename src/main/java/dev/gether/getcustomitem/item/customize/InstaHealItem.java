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
@JsonTypeName("insta_heal")
@SuperBuilder
@NoArgsConstructor
public class InstaHealItem extends CustomItem {


    private double chance;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance)
        );
    }

    @JsonIgnore
    public static InstaHealItem createDefaultItem() {
        return InstaHealItem.builder()
                .enabled(true)
                .itemID("insta_heal")
                .categoryName("insta_heal_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLUE_DYE)
                        .name("#ff4040Vampire Blow")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff7a7a× Use this item",
                                        "#ff7a7a  to heal your self",
                                        "#ff7a7a  chance: {chance}%",
                                        "&7",
                                        "&7• Usage: #ff7a7a{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.INSTA_HEAL)
                .cooldown(10)
                .permissionBypass("getcustomitem.instaheal.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(100f)
                .build();
    }


}
