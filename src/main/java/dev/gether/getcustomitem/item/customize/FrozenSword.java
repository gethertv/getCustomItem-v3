package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.TitleMessage;
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
@JsonTypeName("frozen_sword")
@SuperBuilder
@NoArgsConstructor
public class FrozenSword extends CustomItem {
    private int frozenSeconds;
    private double chanceToFrozen;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chanceToFrozen),
                "{seconds}", String.valueOf(frozenSeconds)
        );
    }

    @JsonIgnore
    public static FrozenSword createDefaultItem() {
        return FrozenSword.builder()
                .enabled(true)
                .itemID("frozen_sword")
                .categoryName("frozen_sword_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.OXEYE_DAISY)
                        .name("#3366ffFrozen sword")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#527dff× Hit the player to have",
                                        "#527dff  a {chance}% chance of freezing",
                                        "#527dff  them for {seconds} seconds!",
                                        "&7",
                                        "&7• Usage: #527dff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.FROZEN_SWORD)
                .cooldown(10)
                .permissionBypass("getcustomitem.frozensword.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .frozenSeconds(2)
                .chanceToFrozen(20)
                .build();
    }
}
