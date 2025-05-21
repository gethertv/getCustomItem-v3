package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
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
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;

@Getter
@Setter
@JsonTypeName("reflection_effect")
@SuperBuilder
@NoArgsConstructor
public class ReflectionEffectItem extends CustomItem {
    private List<PotionEffectConfig> potionEffectConfigs;
    private Set<EquipmentSlot> equipmentSlots;
    private double chance;
    private boolean yourSelf;
    private boolean opponents;
    private boolean cancelInteract;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance)
        );
    }

    @JsonIgnore
    public static ReflectionEffectItem createDefaultItem() {
        return ReflectionEffectItem.builder()
                .enabled(true)
                .itemID("reflection_effect")
                .categoryName("reflection_effect_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.NETHERITE_HELMET)
                        .name("#8c19ffReflection Effect")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#a74fff× Get damage and give effect",
                                        "#a74fff  Chance: &f{chance}%",
                                        "&7",
                                        "&7• Usage: #a74fff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.REFLECTION_EFFECT)
                .cooldown(10)
                .permissionBypass("getcustomitem.reflectioneffect.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(false)
                .potionEffectConfigs(new ArrayList<>(
                        List.of(
                                new PotionEffectConfig("SPEED", 5, 1)
                        )
                ))
                .chance(50)
                .yourSelf(false)
                .opponents(true)
                .cooldownMessage(false)
                .equipmentSlots(new HashSet<>(
                        Set.of(EquipmentSlot.HEAD)
                ))
                .cancelInteract(false)
                .build();
    }

}
