package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ItemsBag;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalInteractListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;


    public GlobalInteractListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        if(item == null) return;

        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Optional<String> customItemID = itemManager.findItemID(item);
        if (customItemID.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return;

        CustomItem customItem = customItemByKey.get();
        if (!regionManager.canUseItem(customItem, player.getLocation())) {
            event.setCancelled(true);
            return;
        }

        // check if the final location is in a non-PvP zone
        if (WorldGuardUtil.isDeniedFlag(player.getLocation(), player, Flags.PVP) && !(customItemByKey.get() instanceof ItemsBag)) {
            event.setCancelled(true);
            return;
        }

        CustomItemInteractEvent customItemInteractEvent = new CustomItemInteractEvent(player,customItem, item, event.getHand(), event.getAction());
        Bukkit.getPluginManager().callEvent(customItemInteractEvent);

        if(customItemInteractEvent.isCancelled())
            return;

        DebugMode.debug(customItemByKey.get());
        event.setCancelled(customItemInteractEvent.isCancelEvent());
    }

}
