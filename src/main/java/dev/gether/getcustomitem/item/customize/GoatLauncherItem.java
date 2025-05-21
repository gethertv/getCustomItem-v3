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
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("goat_launcher")
@SuperBuilder
@NoArgsConstructor
public class GoatLauncherItem extends CustomItem {
    private EntityType entityType;
    private int goatCount;
    private int pushDistance;
    private double pushPower;
    private int goatLifetime;
    private int goatSpacing;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{goats}", String.valueOf(goatCount),
                "{distance}", String.valueOf(pushDistance),
                "{power}", String.valueOf(pushPower)
        );
    }

    @JsonIgnore
    public static GoatLauncherItem createDefaultItem() {
        return GoatLauncherItem.builder()
                .enabled(true)
                .itemID("goat_launcher")
                .categoryName("goat_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.CLOCK)
                        .name("#fc9003Goat Launcher")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#fc9003Goats: &f{goats}",
                                        "#fc9003Push Power: &f{power}",
                                        "#fc9003Push Distance: &f{distance} blocks",
                                        "&7",
                                        "&7• Usage: #fc9003{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.GOAT_LAUNCHER)
                .cooldown(20)
                .permissionBypass("getcustomitem.goatlauncher.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .entityType(EntityType.SHEEP)
                .goatCount(5)
                .pushDistance(10)
                .pushPower(2.0)
                .goatLifetime(3)
                .goatSpacing(2)
                .build();
    }
}