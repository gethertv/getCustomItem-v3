package dev.gether.getcustomitem.item.manager.itembag;

import dev.gether.getcustomitem.item.customize.ItemsBag;
import dev.gether.getutils.models.inventory.AbstractInventoryHolder;
import dev.gether.getutils.models.inventory.InventoryConfig;
import dev.gether.getutils.utils.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ItemBagInventoryHolder extends AbstractInventoryHolder {
    private final ItemsBag itemsBag;
    private final UUID backpackUUID;
    private final ItemBagInventory itemBagInventory;
    private final ActiveBackpackTracker backpackTracker;

    public ItemBagInventoryHolder(Plugin plugin,
                                  Player player,
                                  InventoryConfig inventoryConfig,
                                  ItemsBag itemsBag,
                                  UUID backpackUUID,
                                  ItemBagInventory itemBagInventory,
                                  ActiveBackpackTracker backpackTracker) {
        super(plugin, player, inventoryConfig);
        this.itemsBag = itemsBag;
        this.backpackUUID = backpackUUID;
        this.itemBagInventory = itemBagInventory;
        this.backpackTracker = backpackTracker;

        this.inventory.setContents(itemBagInventory.getInventory().getContents());
        backpackTracker.registerOpenBackpack(backpackUUID, player);

        initializeItems();
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        super.handleClick(event);
        int clickedSlot = event.getRawSlot();
        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if(clickedItem == null)
            return;

        if(clickedSlot >= 0 && clickedSlot < inventory.getSize() - 1) {
            PlayerUtil.addItems(player, clickedItem);
            inventory.setItem(clickedSlot, null);
            itemBagInventory.getInventory().setItem(clickedSlot, null);

        }
    }


    @Override
    protected void initializeItems() {
        setItem(itemsBag.getWithdrawItem(), event -> handleTakeAllItems(player));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void close() {
        super.close();
        handleClose();
    }

    public void handleClose() {
        backpackTracker.unregisterBackpack(backpackUUID);
    }

    private void handleTakeAllItems(Player player) {
        for (int i = 0; i < inventory.getSize()-1; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                PlayerUtil.addItems(player, item);
                inventory.setItem(i, null);
                itemBagInventory.getInventory().setItem(i, null);
            }
        }
    }
}