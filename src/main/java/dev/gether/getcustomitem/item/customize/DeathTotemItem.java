package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonTypeName("death_totem")
@SuperBuilder
@NoArgsConstructor
public class DeathTotemItem extends CustomItem {

    public boolean isDeathTotem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != Material.TOTEM_OF_UNDYING) return false;
        ItemMeta meta = itemStack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(
                new NamespacedKey(GetCustomItem.getInstance(), "death_totem"), 
                PersistentDataType.BYTE
        );
    }

    @Override
    public void init() {
        super.init();
        ItemMeta meta = getItemStack().getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(
                    new NamespacedKey(GetCustomItem.getInstance(), "death_totem"),
                    PersistentDataType.BYTE,
                    (byte) 1
            );
            getItemStack().setItemMeta(meta);
        }
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Collections.emptyMap();
    }

    @JsonIgnore
    public static DeathTotemItem createDefaultItem() {
        return DeathTotemItem.builder()
                .enabled(true)
                .itemID("death_totem")
                .categoryName("death_totem")
                .usage(1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.TOTEM_OF_UNDYING)
                        .name("&4Death Totem")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff6666× Preserves your items after death",
                                        "&7",
                                        "&7• Usage: #ff6666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.DEATH_TOTEM)
                .cooldown(30)
                .permissionBypass("getcustomitem.death_totem.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .build();
    }
}