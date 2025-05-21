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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@JsonTypeName("magic_totem")
@SuperBuilder
@NoArgsConstructor
public class MagicTotemItem extends CustomItem {
    private double chanceLostItem;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chanceLostItem)
        );
    }

    @JsonIgnore
    public static MagicTotemItem createDefaultItem() {
        return MagicTotemItem.builder()
                .enabled(true)
                .itemID("magic_totem")
                .categoryName("magic_totem_category")
                .usage(1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TOTEM_OF_UNDYING)
                        .name("#ff9900Magic Totem")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ffb84dProtect your items with",
                                        "#ffb84dchance to keep: &f{chance}%",
                                        "&7",
                                        "&7• Usage: #ffb84d{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.MAGIC_TOTEM)
                .cooldown(60)
                .permissionBypass("getcustomitem.magictotem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
//                .defaultNotifications()
                .chanceLostItem(50.0)
                .cooldownMessage(true)
                .build();
    }

}
