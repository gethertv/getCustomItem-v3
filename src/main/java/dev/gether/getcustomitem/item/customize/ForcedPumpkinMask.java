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
@JsonTypeName("forced_pumpkin")
@SuperBuilder
@NoArgsConstructor
public class ForcedPumpkinMask extends CustomItem {

    private int seconds;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{seconds}", String.valueOf(seconds)
        );
    }

    @JsonIgnore
    public static ForcedPumpkinMask createDefaultItem() {
        return ForcedPumpkinMask.builder()
                .enabled(true)
                .itemID("forced_pumpkin")
                .categoryName("forced_pumpkin_category")
                .usage(10)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.PUMPKIN)
                        .name("#85f2ffCursed Pumpkin Mask")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#c7f9ff× Forces a pumpkin mask on",
                                        "#c7f9ff  target's head for {seconds}",
                                        "#c7f9ff  seconds",
                                        "&7",
                                        "&7• Usage: #c7f9ff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.FORCE_PUMPKIN)
                .cooldown(10)
                .permissionBypass("getcustomitem.forcedpumpkin.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .seconds(5)
                .build();
    }
}