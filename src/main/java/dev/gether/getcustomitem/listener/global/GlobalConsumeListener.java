package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemConsumeEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ItemsBag;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

public class GlobalConsumeListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;

    public GlobalConsumeListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        final Player player = event.getPlayer();
        final ItemStack consumedItem = event.getItem();
        Optional<String> customItemID = itemManager.findItemID(consumedItem);
        if (customItemID.isEmpty()) {
            return;
        }
        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) {
            return;
        }

        CustomItem customItem = customItemByKey.get();

        if (!regionManager.canUseItem(customItem, player.getLocation())) {
            event.setCancelled(true);
            return;
        }

        EquipmentSlot usedSlot = EquipmentSlot.HAND;
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (offHandItem != null && offHandItem.isSimilar(consumedItem)) {
            usedSlot = EquipmentSlot.OFF_HAND;
        }

        CustomItemConsumeEvent customItemConsumeEvent = new CustomItemConsumeEvent(player, customItem, consumedItem, usedSlot);
        Bukkit.getPluginManager().callEvent(customItemConsumeEvent);

        player.getInventory().setItem(usedSlot, customItemConsumeEvent.getItemStack());


        event.setCancelled(customItemConsumeEvent.isCancelEvent());

        DebugMode.debug(customItem);

        if(customItem.getUsage() <= 0) {
            event.setCancelled(true);
            return;
        }

    }
}
