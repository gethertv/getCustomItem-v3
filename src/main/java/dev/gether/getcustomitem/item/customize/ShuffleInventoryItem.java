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
@JsonTypeName("shuffle_inv_item")
@SuperBuilder
@NoArgsConstructor
public class ShuffleInventoryItem extends CustomItem {

    private double chance;
    private List<Integer> shuffleSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of("{chance}", String.valueOf(chance));
    }

    @JsonIgnore
    public static ShuffleInventoryItem createDefaultItem() {
        return ShuffleInventoryItem.builder()
                .enabled(true)
                .itemID("shuffle_inv")
                .categoryName("shuffle_inv_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.PLAYER_HEAD)
                        .name("#9e9e9eRubik's cube")
                        .base64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzMzNjNzNjODQ4OWE5Y2EzNWQ1NjAzMTMwOTE4Yjg3NjA5ODRlYjlkMzAyOGUzMGU3NDI0N2RmZjg3M2JmZSJ9fX0=")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Hit player to",
                                        "#666666× shuffle his inventory",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SHUFFLE_ITEM)
                .cooldown(10)
                .permissionBypass("getcustomitem.shuffleinv.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .chance(100.0)
                .shuffleSlots(new ArrayList<>(List.of(0,1,2,3,4,5,6,7,8)))
                .build();
    }

}
