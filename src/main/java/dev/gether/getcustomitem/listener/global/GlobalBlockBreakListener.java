package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemBlockBreakEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalBlockBreakListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;


    public GlobalBlockBreakListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Location location = block.getLocation();
        final ItemStack item = player.getInventory().getItemInMainHand();

        Optional<String> customItemID = itemManager.findItemID(item);
        if (customItemID.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return;

        if(WorldGuardUtil.isDeniedFlag(block.getLocation(), player, Flags.BLOCK_BREAK)) {
            return;
        }

        CustomItem customItem = customItemByKey.get();
        if (!regionManager.canUseItem(customItem, block.getLocation())) {
            return;
        }

        CustomItemBlockBreakEvent customItemBlockBreakEvent = new CustomItemBlockBreakEvent(player, block, customItem, item, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(customItemBlockBreakEvent);

        if(customItemBlockBreakEvent.isCancelled())
            return;

        DebugMode.debug(customItemByKey.get());
        event.setCancelled(customItemBlockBreakEvent.isCancelEvent());
    }


}
