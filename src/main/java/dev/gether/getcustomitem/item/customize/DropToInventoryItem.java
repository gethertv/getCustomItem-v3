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
@JsonTypeName("drop_to_inv")
@SuperBuilder
@NoArgsConstructor
public class DropToInventoryItem extends CustomItem {

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of();
    }

    @JsonIgnore
    public static DropToInventoryItem createDefaultItem() {
        return DropToInventoryItem.builder()
                .enabled(true)
                .itemID("drop_to_inv")
                .categoryName("drop_to_inv_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.PLAYER_HEAD)
                        .base64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzMzNjNzNjODQ4OWE5Y2EzNWQ1NjAzMTMwOTE4Yjg3NjA5ODRlYjlkMzAyOGUzMGU3NDI0N2RmZjg3M2JmZSJ9fX0=")
                        .name("#9e9e9eCrown of Looting")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "&7Automatically sends dropped items",
                                        "&7from killed players directly",
                                        "&7to your inventory.",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.DROP_TO_INV)
                .cooldown(5)
                .permissionBypass("getcustomitem.droptoinv.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .build();
    }

}
