package dev.gether.getcustomitem.listener.global;

import dev.gether.getcustomitem.event.CustomFishEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalFishListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;

    public GlobalFishListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();

        boolean handled = handleFishingItem(event, player,
                player.getInventory().getItemInMainHand(),
                EquipmentSlot.HAND);
        if (handled) return;

        handleFishingItem(event, player,
                player.getInventory().getItemInOffHand(),
                EquipmentSlot.OFF_HAND);
    }

    private boolean handleFishingItem(PlayerFishEvent event, Player player,
                                      ItemStack item, EquipmentSlot slot) {
        if (item == null) return false;

        Optional<String> customItemID = itemManager.findItemID(item);
        if (customItemID.isEmpty()) return false;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return false;


        CustomItem customItem = customItemByKey.get();
        if (!regionManager.canUseItem(customItem, player.getLocation())) {
            return true;
        }

        CustomFishEvent customFishEvent = new CustomFishEvent(
                player,
                customItem,
                item,
                slot,
                event.getState(),
                event.getCaught(),
                event.getHook()
        );

        Bukkit.getPluginManager().callEvent(customFishEvent);

        if (customFishEvent.isCancelled()) return true;

        DebugMode.debug(customItemByKey.get());
        event.setCancelled(customFishEvent.isCancelEvent());
        return true;
    }
}