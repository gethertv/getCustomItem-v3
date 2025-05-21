package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.inventory.InventoryConfig;
import dev.gether.getutils.models.inventory.StaticItem;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@JsonTypeName("items_bag")
@SuperBuilder
@NoArgsConstructor
public class ItemsBag extends CustomItem {

    public NamespacedKey ITEMSBAG_KEY;
    public NamespacedKey ITEMSBAG_UUID_KEY;
    private InventoryConfig inventoryConfig;
    private StaticItem withdrawItem;

    @JsonIgnore
    private transient ItemStack itemBag;
    @JsonIgnore
    private ItemStack takeAllItemStack;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of();
    }

    @Override
    public void init() {
        super.init();
        this.takeAllItemStack = withdrawItem != null ? withdrawItem.getItem().getItemStack() : null;
        this.ITEMSBAG_KEY = new NamespacedKey(GetCustomItem.getInstance(), getItemID());
        this.ITEMSBAG_UUID_KEY = new NamespacedKey(GetCustomItem.getInstance(), "itemsbag-uuid");

        ItemStack stack = getItemStack();
        if (stack == null) {
            throw new IllegalStateException("getItemStack() return null for ItemsBag: " + getItemID());
        }
        itemBag = stack;

        ItemMeta itemMeta = itemBag.getItemMeta();
        if (itemMeta != null) {
            itemMeta.getPersistentDataContainer().set(ITEMSBAG_KEY, PersistentDataType.STRING, getItemID());
            itemBag.setItemMeta(itemMeta);
        }
    }


    @JsonIgnore
    public UUID getBackpackUUID(ItemStack backpackItem) {
        if (!isBackpack(backpackItem)) {
            return null;
        }
        ItemMeta meta = backpackItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        String uuidString = meta.getPersistentDataContainer().get(ITEMSBAG_UUID_KEY, PersistentDataType.STRING);
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

    @JsonIgnore
    public boolean isBackpack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(ITEMSBAG_KEY, PersistentDataType.STRING);
    }


    @JsonIgnore
    public void updateBackpackUUID(ItemStack backpack, UUID newUUID) {
        if (!isBackpack(backpack)) return;
        ItemMeta meta = backpack.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().set(ITEMSBAG_UUID_KEY, PersistentDataType.STRING, newUUID.toString());
        backpack.setItemMeta(meta);
    }

    @JsonIgnore
    public static ItemsBag createDefaultItem() {
        return ItemsBag.builder()
                .enabled(true)
                .itemID("item_bag")
                .categoryName("item_bag_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.PLAYER_HEAD)
                        .base64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM3YTM1NTIyZjY3YjJhZjkyMzQ1NTkyODQ2YjcwMmI5YWZiOWQ3YzhkYmFkNWVhMTUwNjczYzllNDRkZTMifX19")
                        .name("#f2ff69Loot Bag")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#beff69Stores items from defeated players!",
                                        "&7",
                                        "&7• Usage: #beff69{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.ITEMS_BAG)
                .cooldown(5)
                .permissionBypass("getcustomitem.itembag.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(false)
                .withdrawItem(StaticItem.builder()
                        .item(Item.builder()
                                .amount(1)
                                .material(Material.LIME_DYE)
                                .name("#beff69Withdraw Items")
                                .lore(new ArrayList<>(
                                        List.of(
                                                "",
                                                "&7Click to withdraw all items",
                                                ""
                                        )
                                ))
                                .build())
                        .slot(53)
                        .build())
                .inventoryConfig(InventoryConfig.builder()
                        .size(54)
                        .title("&0Withdraw items")
                        .decorations(new ArrayList<>())
                        .build())
                .build();
    }


}