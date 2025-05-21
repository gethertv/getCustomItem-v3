package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonTypeName("anti_elytra_hook")
@SuperBuilder
@NoArgsConstructor
@Getter
public class AntiElytraHookItem extends CustomItem {

    private String distanceEscapeMessageSelf;
    private String distanceEscapeMessageFisher;
    private int distance;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{distance}", String.valueOf(distance)
        );
    }

    public void sendEscapeMessages(Player player, Player fisher) {
        MessageUtil.sendMessage(player, distanceEscapeMessageSelf);
        MessageUtil.sendMessage(fisher, distanceEscapeMessageFisher);
    }

    @JsonIgnore
    public static AntiElytraHookItem createDefaultItem() {
        return AntiElytraHookItem.builder()
                .enabled(true)
                .itemID("anti_elytra_hook")
                .categoryName("anti_elytra_hook_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.FISHING_ROD)
                        .name("#ff4d4dAnti-Elytra Hook")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff6666× Use this rod to catch players",
                                        "#ff6666× and prevent them from using elytra",
                                        "#ff6666× Max distance: &f{distance} &7blocks",
                                        "&7",
                                        "&7• Usage: #ff6666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.ANTI_ELYTRA_HOOK)
                .cooldown(15)
                .permissionBypass("getcustomitem.antielytra.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .distanceEscapeMessageSelf("&c&lELYTRA &8» &aYou've broken free from the hook!")
                .distanceEscapeMessageFisher("&c&lELYTRA &8» &cTarget escaped from your hook!")
                .distance(7)
                .build();
    }

}
