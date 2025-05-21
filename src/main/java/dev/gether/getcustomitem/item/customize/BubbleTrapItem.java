package dev.gether.getcustomitem.item.customize;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@JsonTypeName("bubble_trap")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class BubbleTrapItem extends CustomItem {
    private int radius;
    private Material material;
    private int clearTime;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of("{radius}", String.valueOf(radius));
    }

    public void createSquare(Location location) {
        Set<BlockState> blockStates = new HashSet<>();
        int radiusSquared = (radius - 1) * (radius - 1);
        int outerRadiusSquared = (radius + 1) * (radius + 1);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSquared = x * x + y * y + z * z;
                    if (distanceSquared <= radiusSquared || distanceSquared >= outerRadiusSquared) {
                        continue;
                    }

                    Location blockLocation = location.clone().add(x, y, z);
                    Block block = blockLocation.getBlock();

                    if (block.getType() != Material.AIR ||
                            WorldGuardUtil.isDeniedFlag(blockLocation, null, Flags.BLOCK_PLACE)) {
                        continue;
                    }

                    blockStates.add(block.getState());
                    block.setType(material);
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                blockStates.forEach(state -> state.update(true));
                blockStates.clear();
            }
        }.runTaskLater(GetCustomItem.getInstance(), 20L * clearTime);
    }

    @JsonIgnore
    public static BubbleTrapItem createDefaultItem() {
        return BubbleTrapItem.builder()
                .enabled(true)
                .itemID("bubble_trap")
                .categoryName("bubble_trap_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.OBSIDIAN)
                        .name("#7d4dffObsidian Trap")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#9c76ffCreate obsidian bubble trap",
                                        "#9c76ffwith radius: &f{radius}",
                                        "&7",
                                        "&7• Usage: #9c76ff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.BUBBLE_TRAP)
                .cooldown(20)
                .permissionBypass("getcustomitem.bubbletrap.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .radius(6)
                .material(Material.OBSIDIAN)
                .clearTime(15)
                .build();
    }
}