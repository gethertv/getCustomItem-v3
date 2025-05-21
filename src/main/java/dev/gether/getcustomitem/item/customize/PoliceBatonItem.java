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
@JsonTypeName("police_stick")
@SuperBuilder
@NoArgsConstructor
public class PoliceBatonItem extends CustomItem {

    private int blockDuration; // Duration in seconds
    private String hitMessage = "&aPrzyłożyłeś pałką gracza &e{player} &ana &e{duration} &asekund!";
    private String blockedMessage = "&cNie możesz używać przedmiotów eventowych przez &e{time} &csekund!";
    private boolean enableMessage = true;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{duration}", String.valueOf(blockDuration)
        );
    }

    @JsonIgnore
    public static PoliceBatonItem createDefaultItem() {
        return PoliceBatonItem.builder()
                .enabled(true)
                .itemID("police_baton")
                .categoryName("police_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.STICK)
                        .name("&#3498dbPałka Policyjna")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "&#3498dbCzas blokowania: &f{duration}s",
                                        "&7",
                                        "&7• &fUderz gracza aby uniemożliwić",
                                        "&7  &fużywanie przedmiotów eventowych",
                                        "&7• Usage: &#3498db{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.POLICE_BATON)
                .cooldown(5)
                .permissionBypass("getcustomitem.policebaton.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ENTITY_PLAYER_ATTACK_SWEEP)
                        .build())
                .cooldownMessage(true)
                .blockDuration(30)
                .build();
    }
}