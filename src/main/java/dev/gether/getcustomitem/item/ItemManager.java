package dev.gether.getcustomitem.item;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getutils.utils.ItemUtil;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemManager {

    @Getter
    private final static NamespacedKey itemKey = new NamespacedKey(GetCustomItem.getInstance(), "item_id");
    private final FileManager fileManager;

    public ItemManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public Optional<CustomItem> findCustomItemByType(ItemType itemType, ItemStack itemStack) {
        return fileManager.getCustomItems().stream()
                .filter(item -> item.getItemType() == itemType)
                .filter(item -> ItemUtil.sameItemName(item.getItemStack(), itemStack)).findFirst();
    }

    public Optional<CustomItem> findCustomItemByKey(String key) {
        return fileManager.getCustomItems().stream()
                .filter(item -> item.getItemID().equalsIgnoreCase(key)).findFirst();
    }

//    public Optional<CustomItem> findCustomItemByType(ItemType itemType) {
//        return fileManager.getCustomItems().stream()
//                .filter(item -> item.getItemType() == itemType)
//                .findFirst();
//    }

    public List<CustomItem> findAllCustomItemByType(ItemType itemType) {
        return fileManager.getCustomItems().stream()
                .filter(item -> item.getItemType() == itemType).toList();
    }

    public SuggestionResult getAllItemKey() {
        return SuggestionResult.of(fileManager.getCustomItems().stream()
                .map(CustomItem::getItemID)
                .collect(Collectors.toList()));
    }
    public void initItems() {
        fileManager.getCustomItems().forEach(CustomItem::init);
    }

    public boolean isCustomItem(ItemStack itemStack) {
        return findItemID(itemStack).isPresent();
    }

    public Optional<String> findItemID(ItemStack itemStack) {
        if(itemStack == null) return Optional.empty();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return Optional.empty();

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        String itemID = persistentDataContainer.get(this.itemKey, PersistentDataType.STRING);
        if(itemID == null) return Optional.empty();
        return Optional.of(itemID);
    }


    /*
    public Optional<ItemStack> findItemStackByType(ItemType itemType) {
        return config.getCustomItems().stream()
                .filter(item -> item.getItemType() == itemType)
                .map(item -> item.getItem().getItemStack()).findFirst();
    }
    */


}
