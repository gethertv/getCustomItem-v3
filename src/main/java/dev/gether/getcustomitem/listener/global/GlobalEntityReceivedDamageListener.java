package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.event.CustomItemReceivedDamageEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalEntityReceivedDamageListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;
    private EquipmentSlot[] armorSlots = new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND, EquipmentSlot.HAND};

    public GlobalEntityReceivedDamageListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim))
            return;

        if(victim.hasMetadata("NPC"))
            return;


        /* world-guard section */
        // check the using player is in PVP region
        if(WorldGuardUtil.isDeniedFlag(damager.getLocation(), damager, Flags.PVP)) {
            return;
        }
        if(WorldGuardUtil.isDeniedFlag(victim.getLocation(), victim, Flags.PVP)) {
            return;
        }


        for (int i = 0; i < armorSlots.length; i++) {
            EquipmentSlot armorSlot = armorSlots[i];

            ItemStack itemStack = victim.getInventory().getItem(armorSlot);
            Optional<String> customItemID = itemManager.findItemID(itemStack);
            if (customItemID.isEmpty())
                continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
            if (customItemByKey.isEmpty()) continue;

            CustomItem customItem = customItemByKey.get();
            if (!regionManager.canUseItem(customItem, damager.getLocation())) {
                return;
            }

            if (!regionManager.canUseItem(customItem, victim.getLocation())) {
                return;
            }

            CustomItemReceivedDamageEvent customItemReceivedDamageEvent = new CustomItemReceivedDamageEvent(damager, victim, customItem, itemStack, armorSlot);
            Bukkit.getPluginManager().callEvent(customItemReceivedDamageEvent);


            if(customItemReceivedDamageEvent.isCancelled())
                continue;

            DebugMode.debug(customItemByKey.get());
            event.setCancelled(customItemReceivedDamageEvent.isCancelEvent());
        }

    }
}
