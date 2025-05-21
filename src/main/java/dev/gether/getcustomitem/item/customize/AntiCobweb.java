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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@JsonTypeName("anty_cobweb")
@NoArgsConstructor
public class AntiCobweb extends CustomItem {

    private boolean forceDelete = false;
    private int radiusX;
    private int radiusY;


    public void cleanCobweb(Player player, Location location) {
        for (int x = -radiusX + 1; x < radiusX; x++) {
            for (int y = -radiusY + 1; y < radiusY; y++) {
                for (int z = -radiusX + 1; z < radiusX; z++) {
                    Location tempLoc = location.clone().add(x, y, z);
                    if (WorldGuardUtil.isDeniedFlag(tempLoc, null, Flags.BLOCK_BREAK)) {
                        continue;
                    }
                    Block block = tempLoc.getBlock();
                    if (block.getType() !=Material.COBWEB) {
                        continue;
                    }

                    boolean tempBlock = GetCustomItem.getInstance().getHookManager().destroyBlock(player, block.getLocation());
                    if(tempBlock)
                        continue;

                    if(!forceDelete)
                        continue;

                    block.setType(Material.AIR);
                }
            }
        }
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius-x}", String.valueOf(radiusX),
                "{radius-y}", String.valueOf(radiusY)
                    );
    }

    @JsonIgnore
    public static AntiCobweb createDefaultItem() {
        return AntiCobweb.builder()
                .enabled(true)
                .itemID("anty_cobweb")
                .categoryName("anty_cobweb_category")
                .usage(1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.SOUL_LANTERN)
                        .name("#1aff00Anty-cobweb")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#78ff69× Use this item to",
                                        "#78ff69  remove all cobweb",
                                        "#78ff69  in radius &7(&f{radius-x}&7x&f{radius-y}&7)",
                                        "&7",
                                        "&7• Usage: #78ff69{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.ANTY_COBWEB)
                .cooldown(10)
                .permissionBypass("getcustomitem.antycobweb.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
//                .defaultNotifications()
                .radiusX(2)
                .cooldownMessage(true)
                .radiusY(2)
                .build();
    }

}
