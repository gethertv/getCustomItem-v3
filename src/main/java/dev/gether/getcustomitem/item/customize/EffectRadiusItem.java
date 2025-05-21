package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.ItemVariant;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
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
@JsonTypeName("effect_radius")
@SuperBuilder
@NoArgsConstructor
public class EffectRadiusItem extends CustomItem {

    private boolean includingYou;
    private boolean otherPlayers;
    private boolean disableKnockback;
    private int radius;
    private List<PotionEffectConfig> activeEffects;
    private List<String> removeEffects;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius}", String.valueOf(radius)
        );
    }

    @JsonIgnore
    public static EffectRadiusItem createItem(ItemVariant variant) {
        return switch (variant) {
            case EXAMPLE_1 -> EffectRadiusItem.builder()
                    .enabled(true)
                    .itemID("yeti_eye")
                    .categoryName("yeti_eye_category")
                    .usage(5)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.SPIDER_EYE)
                            .name("#0015ffYeti eye")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#2e3fff× Use this item to give",
                                            "#2e3fff  the weakness effect on X seconds",
                                            "#2e3fff  to near players &7(&f{radius}&7x&f{radius}&7)",
                                            "&7",
                                            "&7• Usage: #ffba61{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(true)
                            .build())
                    .itemType(ItemType.EFFECT_RADIUS)
                    .cooldown(10)
                    .permissionBypass("getcustomitem.yetieye.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .includingYou(true)
                    .otherPlayers(true)
                    .disableKnockback(false)
                    .radius(5)
                    .activeEffects(new ArrayList<>(
                            List.of(
                                    new PotionEffectConfig(
                                            "WEAKNESS",
                                            3,
                                            1
                                    )
                            )
                    ))
                    .removeEffects(new ArrayList<>())
                    .build();

            case EXAMPLE_2 -> EffectRadiusItem.builder()
                    .enabled(true)
                    .itemID("air_filter")
                    .categoryName("air_filter_category")
                    .usage(1)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.FLINT)
                            .name("#608a71Air filter")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#96b0a0× Use this item to clean",
                                            "#96b0a0  a negative effect from yourself",
                                            "&7",
                                            "&7• Usage: #96b0a0{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(true)
                            .build())
                    .itemType(ItemType.EFFECT_RADIUS)
                    .cooldown(10)
                    .permissionBypass("getcustomitem.airfilter.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .includingYou(true)
                    .otherPlayers(false)
                    .disableKnockback(false)
                    .radius(2)
                    .activeEffects(new ArrayList<>())
                    .removeEffects(new ArrayList<>(
                            List.of(
                                    "WEAKNESS"
                            )
                    ))
                    .build();

            case EXAMPLE_3 -> EffectRadiusItem.builder()
                    .enabled(true)
                    .itemID("levitation_rod")
                    .categoryName("levitation_rod_category")
                    .usage(3)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.BLAZE_ROD)
                            .name("#e6c9ffLevitation rod")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#d4b3ff× Use this item to give",
                                            "#d4b3ff  the levitation effect for X seconds",
                                            "#d4b3ff  to near players &7(&f{radius}&7x&f{radius}&7)",
                                            "&7",
                                            "&7• Usage: #d4b3ff{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(true)
                            .build())
                    .itemType(ItemType.EFFECT_RADIUS)
                    .cooldown(15)
                    .permissionBypass("getcustomitem.levitationrod.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .includingYou(true)
                    .otherPlayers(true)
                    .disableKnockback(false)
                    .radius(4)
                    .activeEffects(new ArrayList<>(
                            List.of(
                                    new PotionEffectConfig(
                                            "LEVITATION",
                                            5,
                                            1
                                    )
                            )
                    ))
                    .removeEffects(new ArrayList<>())
                    .build();

            default -> createDefaultItem();
        };
    }

    @JsonIgnore
    public static EffectRadiusItem createDefaultItem() {
        return EffectRadiusItem.builder()
                .enabled(true)
                .itemID("ice_rod")
                .categoryName("ice_rod_category")
                .usage(3)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TRIDENT)
                        .name("#737d9cIce rod")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#9aa1b8× Use this item to remove",
                                        "#9aa1b8  all positive effects from",
                                        "#9aa1b8  players within a &f{radius}&7x&f{radius}#9aa1b8 radius",
                                        "&7",
                                        "&7• Usage: #9aa1b8{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.EFFECT_RADIUS)
                .cooldown(10)
                .permissionBypass("getcustomitem.icerod.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .includingYou(false)
                .otherPlayers(true)
                .disableKnockback(false)
                .radius(5)
                .activeEffects(new ArrayList<>())
                .removeEffects(new ArrayList<>(
                        List.of(
                                "SPEED",
                                "INCREASE_DAMAGE",
                                "JUMP"
                        )
                ))
                .build();
    }
}