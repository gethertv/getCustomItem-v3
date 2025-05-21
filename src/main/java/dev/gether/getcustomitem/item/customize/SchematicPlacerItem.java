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
@JsonTypeName("schematic_placer")
@SuperBuilder
@NoArgsConstructor
public class SchematicPlacerItem extends CustomItem {

    private String schematicName;
    private int duration;
    private boolean gradualPaste;
    private int blocksPerTick;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{schematic}", schematicName,
                "{duration}", String.valueOf(duration)
        );
    }

    @JsonIgnore
    public static SchematicPlacerItem createDefaultItem() {
        return SchematicPlacerItem.builder()
                .enabled(true)
                .itemID("schematic_placer")
                .categoryName("structure_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.STRUCTURE_BLOCK)
                        .name("#4287f5Temporary Structure")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#87adf5× Place a temporary structure",
                                        "#87adf5  that will disappear after",
                                        "#87adf5  {duration} seconds",
                                        "&7",
                                        "&7• Structure: #87adf5{schematic}",
                                        "&7• Duration: #87adf5{duration}s",
                                        "&7",
                                        "&7• Usage: #87adf5{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SCHEMATIC_PLACER)
                .cooldown(30)
                .permissionBypass("getcustomitem.schematicplacer.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_STONE_PLACE)
                        .build())
                .cooldownMessage(true)
                .schematicName("default_structure")
                .duration(60)
                .gradualPaste(true)
                .blocksPerTick(20)
                .build();
    }
}