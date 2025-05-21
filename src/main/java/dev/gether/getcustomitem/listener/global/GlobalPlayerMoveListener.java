package dev.gether.getcustomitem.listener.global;

import dev.gether.getcustomitem.event.CustomItemMoveEvent;
import dev.gether.getcustomitem.event.CustomItemResurrectEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalPlayerMoveListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;
    private EquipmentSlot[] equipmentSlots = new EquipmentSlot[] {EquipmentSlot.FEET};

    public GlobalPlayerMoveListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        for (int i = 0; i < equipmentSlots.length; i++) {
            EquipmentSlot equipmentSlot = equipmentSlots[i];
            ItemStack item = player.getInventory().getItem(equipmentSlot);
            Optional<String> customItemID = itemManager.findItemID(item);
            if (customItemID.isEmpty())
                continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
            if (customItemByKey.isEmpty()) continue;

            CustomItem customItem = customItemByKey.get();
            if (!regionManager.canUseItem(customItem, player.getLocation())) {
                return;
            }

            CustomItemMoveEvent customItemMoveEvent = new CustomItemMoveEvent(player, customItem, item, equipmentSlot);
            Bukkit.getPluginManager().callEvent(customItemMoveEvent);

            if(customItemMoveEvent.isCancelled())
                return;

            DebugMode.debug(customItemByKey.get());
            event.setCancelled(customItemMoveEvent.isCancelEvent());
            return;
        }

    }
}
