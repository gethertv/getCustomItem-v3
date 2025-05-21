package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalEntityMakeDamageListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;


    public GlobalEntityMakeDamageListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim))
            return;

        if(victim.hasMetadata("NPC"))
            return;

        ItemStack itemInMainHand = damager.getInventory().getItemInMainHand();
        Optional<String> customItemID = itemManager.findItemID(itemInMainHand);
        if (customItemID.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return;

        CustomItem customItem = customItemByKey.get();
        if (!regionManager.canUseItem(customItem, damager.getLocation())) {
            return;
        }

        if (!regionManager.canUseItem(customItem, victim.getLocation())) {
            return;
        }

        /* world-guard section */
        // check the using player is in PVP region
        if(WorldGuardUtil.isDeniedFlag(damager.getLocation(), damager, Flags.PVP)) {
            return;
        }
        if(WorldGuardUtil.isDeniedFlag(victim.getLocation(), victim, Flags.PVP)) {
            return;
        }

        CustomItemDamageEvent customItemDamageEvent = new CustomItemDamageEvent(damager, victim, customItem, itemInMainHand, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(customItemDamageEvent);

        if(customItemDamageEvent.isCancelled())
            return;

        DebugMode.debug(customItemByKey.get());
        event.setCancelled(customItemDamageEvent.isCancelEvent());

    }
}
