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
@JsonTypeName("throw_up")
@SuperBuilder
@NoArgsConstructor
public class ThrowUpItem extends CustomItem {

    private boolean includingYou;
    private double pushPowerSelf;

    private boolean otherPlayers;
    private double pushPowerOpponents;
    private double upwardPowerSelf;
    private double upwardPowerOpponents;

    private int radius;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius}", String.valueOf(radius),
                "{push-opponents}", String.valueOf(pushPowerOpponents),
                "{push-yourself}", String.valueOf(pushPowerSelf)
        );
    }

    @JsonIgnore
    public static ThrowUpItem createDefaultItem() {
        return ThrowUpItem.builder()
                .enabled(true)
                .itemID("throw_up_upward")
                .categoryName("throw_up_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.STICK)
                        .name("#9e9e9ePush stick")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Use this item",
                                        "#666666  to throw up players",
                                        "#666666  in radius: {radius}",
                                        "#666666  power opponents: &f{push-opponents}",
                                        "#666666  power yourself: &f{push-yourself}",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.THROW_UP)
                .cooldown(10)
                .permissionBypass("getcustomitem.pushitem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .includingYou(true)
                .otherPlayers(true)
                .pushPowerSelf(1.5)
                .pushPowerOpponents(1.5)
                .upwardPowerSelf(0.5)
                .upwardPowerOpponents(0.5)
                .radius(7)
                .build();
    }
}
