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

@JsonTypeName("lasso_trap")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class LassoTrapItem extends CustomItem {
    private int teleportDelay;
    private double teleportChance;
    private double catchRadius;
    private double breakDistance;
    private String breakMessage;
    private String successMessage;
    private String failMessage;
    private String noRange;
    private double pullStrength;
    private String targetCaughtMessage;
    private SoundConfig pullSound;
    private SoundConfig failSound;
    private boolean showParticles;
    private String particleType;
    private int particleCount;
    private boolean enableKnockback;
    private double knockbackStrength;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{delay}", String.valueOf(teleportDelay),
                "{chance}", String.valueOf(teleportChance),
                "{radius}", String.valueOf(catchRadius),
                "{break_distance}", String.valueOf(breakDistance),
                "{pull_strength}", String.valueOf(pullStrength)
        );
    }

    @JsonIgnore
    public static LassoTrapItem createDefaultItem() {
        return LassoTrapItem.builder()
                .enabled(true)
                .itemID("lasso_trap")
                .categoryName("lasso_trap_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.LEAD)
                        .name("#ffaa00Magic Lasso")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ffcc00Catch players with your lasso!",
                                        "#ffcc00After &f{delay}s &fyou have &f{chance}% &fchance",
                                        "#ffcc00to pull them towards you",
                                        "&7",
                                        "#ffcc00Catch radius: &f{radius} blocks",
                                        "#ffcc00Break distance: &f{break_distance} blocks",
                                        "#ffcc00Pull strength: &f{pull_strength}",
                                        "&7",
                                        "&7• Usage: #ffcc00{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.LASSO_TRAP)
                .cooldown(20)
                .permissionBypass("getcustomitem.lassotrap.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .pullSound(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .failSound(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .teleportDelay(3)
                .teleportChance(50)
                .catchRadius(10.0)
                .noRange("#ff6666No players found in range!")
                .breakDistance(10.0)
                .pullStrength(1.5)
                .breakMessage("#ff6666Your lasso broke because the target was too far!")
                .successMessage("#66ff66&lSuccessfully pulled the player!")
                .failMessage("#ff6666&lFailed to pull the player!")
                .targetCaughtMessage("#ffcc00&lYou've been caught by a lasso!")
                .showParticles(true)
                .particleType("CRIT")
                .particleCount(20)
                .enableKnockback(true)
                .knockbackStrength(0.5)
                .build();
    }
}