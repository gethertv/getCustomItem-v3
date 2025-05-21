package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.PotionEffectConfig;
import dev.gether.getutils.models.TitleMessage;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
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
@JsonTypeName("hit_effect")
@SuperBuilder
@NoArgsConstructor
public class HitEffectItem extends CustomItem {
    private List<PotionEffectConfig> potionEffectConfigs;
    private boolean removeEffect = false;
    private List<String> potionEffectNames;
    private double chance;
    private boolean yourSelf;
    private boolean opponents;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{chance}", String.valueOf(chance)
        );
    }

    @JsonIgnore
    public static HitEffectItem createDefaultItem() {
        return HitEffectItem.builder()
                .enabled(true)
                .itemID("wizard_staff")
                .categoryName("wizard_staff_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.REDSTONE_TORCH)
                        .name("#8c19ffWizard's staff")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#a74fff× Hit the player",
                                        "#a74fff  and give him custom effect",
                                        "#a74fff  Chance: &f{chance}%",
                                        "&7",
                                        "&7• Usage: #a74fff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.HIT_EFFECT)
                .cooldown(10)
                .permissionBypass("getcustomitem.wizardstaff.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .potionEffectConfigs(new ArrayList<>(
                        List.of(
                                new PotionEffectConfig("SPEED", 5, 1)
                        )
                ))
                .removeEffect(false)
                .chance(50)
                .potionEffectNames(new ArrayList<>(List.of("SPEED")))
                .yourSelf(false)
                .opponents(true)
                .build();
    }

}
