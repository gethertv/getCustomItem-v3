package dev.gether.getcustomitem.item.customize.resurrect;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.PotionConverUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@JsonTypeName("confusion_totem")
@SuperBuilder
@NoArgsConstructor
@Getter
public class ConfusionTotemItem extends CustomItem {

    private int radius;
    private List<PotionEffectConfig> potionEffects;
    private boolean affectYourself;

    public void swapPlayersAndApplyEffects(Player user, List<Player> nearbyPlayers) {
        List<Location> locations = new ArrayList<>();
        for (Player player : nearbyPlayers) {
            locations.add(player.getLocation());
        }
        
        for (int i = locations.size() - 1; i > 0; i--) {
            int index = GetCustomItem.getRandom().nextInt(i + 1);
            Location temp = locations.get(index);
            locations.set(index, locations.get(i));
            locations.set(i, temp);
        }
        
        for (int i = 0; i < nearbyPlayers.size(); i++) {
            Player player = nearbyPlayers.get(i);
            player.teleport(locations.get(i));
            if (player != user || affectYourself) {
                applyEffects(player);
            }
        }
    }

    private void applyEffects(Player player) {
        List<PotionEffect> potionEffectFromConfig = PotionConverUtil.getPotionEffectFromConfig(potionEffects);
        for (PotionEffect effect : potionEffectFromConfig) {
            player.addPotionEffect(effect);
        }
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius}", String.valueOf(radius),
                "{affect_yourself}", String.valueOf(affectYourself)
        );
    }

    @JsonIgnore
    public static ConfusionTotemItem createDefaultItem() {
        return ConfusionTotemItem.builder()
                .enabled(true)
                .itemID("confusion_totem")
                .categoryName("totems")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TOTEM_OF_UNDYING)
                        .name("&dConfusion Totem")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff6666× Swaps all players within radius",
                                        "#ff6666× of &f{radius} &7blocks and applies effects",
                                        "#ff6666× Affects you: &f{affect_yourself}",
                                        "&7",
                                        "&7• Usage: #ff6666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.CONFUSION_TOTEM)
                .cooldown(30)
                .permissionBypass("getcustomitem.confusiontotem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .radius(10)
                .potionEffects(Arrays.asList(
                        new PotionEffectConfig("CONFUSION", 3, 1),
                        new PotionEffectConfig("BLINDNESS", 3, 1)
                ))
                .cooldownMessage(true)
                .affectYourself(true)
                .build();
    }
}