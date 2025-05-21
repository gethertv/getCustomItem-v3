package dev.gether.getcustomitem.item.customize.itemtier;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.getutils.models.Item;
import lombok.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TierData {
    private Map<ActionEvent, Map<Object, Double>> actionEvents;
    private Item item;
    private double requirementValue;
    @JsonIgnore
    private ItemStack itemStack;

    @JsonIgnore
    public void init(NamespacedKey ITEM_TIER, NamespacedKey ITEM_TIER_PROGRESS) {
        itemStack = item.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.getPersistentDataContainer().set(ITEM_TIER, PersistentDataType.INTEGER, 0);
        itemMeta.getPersistentDataContainer().set(ITEM_TIER_PROGRESS, PersistentDataType.DOUBLE, 0.0);
        itemStack.setItemMeta(itemMeta);
    }

}
