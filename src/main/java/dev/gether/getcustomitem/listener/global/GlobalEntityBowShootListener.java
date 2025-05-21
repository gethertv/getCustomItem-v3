package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomItemBowShootEvent;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class GlobalEntityBowShootListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;


    public GlobalEntityBowShootListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) {
            return;
        }
        final ItemStack item = event.getBow();
        if(item == null) return;

        Optional<String> customItemID = itemManager.findItemID(item);
        if (customItemID.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return;

        CustomItem customItem = customItemByKey.get();
        if (!regionManager.canUseItem(customItem, shooter.getLocation())) {
            return;
        }

        if(WorldGuardUtil.isDeniedFlag(shooter.getLocation(), shooter, Flags.PVP)) {
            return;
        }

        CustomItemBowShootEvent customItemBowShootEvent = new CustomItemBowShootEvent(shooter, (Projectile) event.getProjectile(), customItem, item, EquipmentSlot.HAND);
        Bukkit.getPluginManager().callEvent(customItemBowShootEvent);

        if(customItemBowShootEvent.isCancelled())
            return;

        DebugMode.debug(customItemByKey.get());
        event.setCancelled(customItemBowShootEvent.isCancelEvent());

    }
}
