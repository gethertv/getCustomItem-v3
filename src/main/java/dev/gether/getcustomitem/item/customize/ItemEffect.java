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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("item_effect")
@SuperBuilder
@NoArgsConstructor
public class ItemEffect extends CustomItem {

    private List<PotionEffectConfig> potionEffectConfigs;
    private List<EquipmentSlot> equipmentSlots;

    @Override
    protected Map<String, String> replacementValues() {
        return new HashMap<>();
    }

    @JsonIgnore
    public static ItemEffect createItem(ItemVariant variant) {
        return switch (variant) {
            case EXAMPLE_1 -> ItemEffect.builder()
                    .enabled(true)
                    .itemID("crown")
                    .categoryName("crown_category")
                    .usage(-1)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.PLAYER_HEAD)
                            .base64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzMzNjNzNjODQ4OWE5Y2EzNWQ1NjAzMTMwOTE4Yjg3NjA5ODRlYjlkMzAyOGUzMGU3NDI0N2RmZjg3M2JmZSJ9fX0=")
                            .name("#9e9e9eCrown")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#666666× Wear this item",
                                            "#666666× to get effects",
                                            "&7",
                                            "&7• Usage: #666666{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(false)
                            .build())
                    .itemType(ItemType.ITEM_EFFECT)
                    .cooldown(10)
                    .permissionBypass("getcustomitem.crown.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .equipmentSlots(new ArrayList<>(List.of(
                            EquipmentSlot.HEAD
                    )))
                    .potionEffectConfigs(new ArrayList<>(
                            List.of(
                                    new PotionEffectConfig(
                                            "SPEED",
                                            3,
                                            1
                                    )
                            )
                    ))
                    .build();

            case EXAMPLE_2 -> ItemEffect.builder()
                    .enabled(true)
                    .itemID("cupids_stick")
                    .categoryName("cupids_stick_category")
                    .usage(-1)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.STICK)
                            .name("#9e9e9eCupid's Stick")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#666666× Hand this item",
                                            "#666666× to get effects",
                                            "&7",
                                            "&7• Usage: #666666{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(true)
                            .build())
                    .itemType(ItemType.ITEM_EFFECT)
                    .cooldown(10)
                    .permissionBypass("getcustomitem.crown.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .equipmentSlots(new ArrayList<>(List.of(
                            EquipmentSlot.HAND
                    )))
                    .potionEffectConfigs(new ArrayList<>(
                            List.of(
                                    new PotionEffectConfig(
                                            "SPEED",
                                            3,
                                            1
                                    )
                            )
                    ))
                    .build();

            default -> createDefaultItem();
        };
    }

    @JsonIgnore
    public static ItemEffect createDefaultItem() {
        return ItemEffect.builder()
                .enabled(true)
                .itemID("lollypop")
                .categoryName("lollypop_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.LIME_DYE)
                        .name("#9e9e9eLollypop")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Hand this item",
                                        "#666666× to get effects",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .enchantments(new HashMap<>(Map.of(
                                Enchantment.FIRE_ASPECT, 2,
                                Enchantment.DAMAGE_ALL, 6
                        )))
                        .unbreakable(true)
                        .glow(false)
                        .build())
                .itemType(ItemType.ITEM_EFFECT)
                .cooldown(10)
                .permissionBypass("getcustomitem.lollypop.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .equipmentSlots(new ArrayList<>(List.of(
                        EquipmentSlot.HAND
                )))
                .potionEffectConfigs(new ArrayList<>(
                        List.of(
                                new PotionEffectConfig(
                                        "SPEED",
                                        3,
                                        1
                                )
                        )
                ))
                .build();
    }
}
