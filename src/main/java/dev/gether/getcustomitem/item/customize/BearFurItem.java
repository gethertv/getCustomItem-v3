package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.TitleMessage;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
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
@JsonTypeName("bear_fur")
@SuperBuilder
@NoArgsConstructor
public class BearFurItem extends CustomItem {

    private double reducedDamage;
    private int seconds;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{reduced-damage}", String.valueOf(reducedDamage),
                "{seconds}", String.valueOf(seconds)
        );
    }

    @JsonIgnore
    public static BearFurItem createDefaultItem() {
        return BearFurItem.builder()
                .enabled(true)
                .itemID("bear_fur")
                .categoryName("bear_fur_category")
                .usage(10)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.PHANTOM_MEMBRANE)
                        .name("#85f2ffBear fur")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#c7f9ff× Use this item to reduced",
                                        "#c7f9ff  your damage by {reduced-damage}%",
                                        "#c7f9ff  for {seconds} seconds",
                                        "&7",
                                        "&7• Usage: #c7f9ff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.BEAR_FUR)
                .cooldown(10)
                .permissionBypass("getcustomitem.bearfur.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .reducedDamage(50)
                .seconds(5)
                .build();
    }

}
