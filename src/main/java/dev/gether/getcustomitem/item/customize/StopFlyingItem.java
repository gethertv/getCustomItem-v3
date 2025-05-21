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
@JsonTypeName("stop_flying")
@SuperBuilder
@NoArgsConstructor
public class StopFlyingItem extends CustomItem {
    private double multiply;
    private double divideHeight;
    private double divideGliding;
    private int stopFlyingTime;
    private List<Material> cannotUseMaterial;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of();
    }

    @JsonIgnore
    public static StopFlyingItem createDefaultItem() {
        return StopFlyingItem.builder()
                .enabled(true)
                .itemID("stop_flying")
                .categoryName("stop_flying_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BLAZE_ROD)
                        .name("#f2ff69Anti-Elytra Rod")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#beff69Prevents Elytra use on hit!",
                                        "&7",
                                        "&7• Usage: #beff69{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.STOP_FLYING)
                .cooldown(5)
                .permissionBypass("getcustomitem.stopflying.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .multiply(4)
                .divideHeight(1.7)
                .divideGliding(2.0)
                .stopFlyingTime(15)
                .cannotUseMaterial(new ArrayList<>(
                        List.of(Material.FIREWORK_ROCKET)
                ))
                .build();
    }

}
