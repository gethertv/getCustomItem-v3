package dev.gether.getcustomitem.listener.global;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.event.CustomItemResurrectEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class GlobalEntityResurrectListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;
    private EquipmentSlot[] equipmentSlots = new EquipmentSlot[] {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND};

    public GlobalEntityResurrectListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityResurrect(EntityResurrectEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;

        if(!GetCustomItem.getInstance().getFileManager().getConfig().isRemoveTotemEffect()) {
            Collection<PotionEffect> activeEffects = new ArrayList<>(player.getActivePotionEffects());

            Bukkit.getScheduler().runTask(GetCustomItem.getInstance(), () -> {
                for(PotionEffect effect : activeEffects) {
                    player.addPotionEffect(effect, true);
                }
            });
        }

        for (int i = 0; i < equipmentSlots.length; i++) {
            EquipmentSlot equipmentSlot = equipmentSlots[i];
            ItemStack item = player.getInventory().getItem(equipmentSlot);

            if(item.getType() != Material.TOTEM_OF_UNDYING) continue;

            Optional<String> customItemID = itemManager.findItemID(item);
            if (customItemID.isEmpty())
                continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
            if (customItemByKey.isEmpty()) continue;

            CustomItem customItem = customItemByKey.get();
            if (!regionManager.canUseItem(customItem, player.getLocation())) {
                return;
            }

            CustomItemResurrectEvent customItemResurrectEvent = new CustomItemResurrectEvent(player, customItem, item, equipmentSlot);
            Bukkit.getPluginManager().callEvent(customItemResurrectEvent);

            if(customItemResurrectEvent.isCancelled())
                return;

            DebugMode.debug(customItemByKey.get());
            event.setCancelled(customItemResurrectEvent.isCancelEvent());
        }

    }
}
