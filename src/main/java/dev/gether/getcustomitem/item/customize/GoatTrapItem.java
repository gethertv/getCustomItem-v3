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
@JsonTypeName("goat_trap")
@SuperBuilder
@NoArgsConstructor
public class GoatTrapItem extends CustomItem {
    private int duration;
    private int radius;
    private boolean blockElytra;
    private boolean onHitMode;
    private List<Material> blockedItems = new ArrayList<>();


    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{duration}", String.valueOf(duration),
                "{radius}", String.valueOf(radius)
        );
    }

    @JsonIgnore
    public static GoatTrapItem createDefaultItem() {
        List<Material> defaultBlockedItems = new ArrayList<>(List.of(
                Material.ENDER_PEARL,
                Material.CHORUS_FRUIT,
                Material.FISHING_ROD,
                Material.TRIDENT
        ));

        return GoatTrapItem.builder()
                .enabled(true)
                .itemID("goat_trap")
                .categoryName("goat_trap_category")
                .usage(5)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BARRIER)
                        .name("#ff6b6bGoat Trap")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff8585× Trap players in a barrier for",
                                        "#ff8585  {duration} seconds",
                                        "#ff8585× Affects players within {radius} blocks",
                                        "&7",
                                        "&7• Usage: #ff8585{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.GOAT_TRAP)
                .cooldown(30)
                .permissionBypass("getcustomitem.goattrap.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .duration(10)
                .radius(5)
                .onHitMode(true)
                .blockElytra(true)
                .blockedItems(defaultBlockedItems)
                .build();
    }
}