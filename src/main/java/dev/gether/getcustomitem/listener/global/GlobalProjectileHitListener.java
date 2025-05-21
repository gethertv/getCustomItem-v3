package dev.gether.getcustomitem.listener.global;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.utils.DebugMode;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

public class GlobalProjectileHitListener implements Listener {

    private final ItemManager itemManager;
    private final RegionManager regionManager;

    public GlobalProjectileHitListener(ItemManager itemManager, RegionManager regionManager) {
        this.itemManager = itemManager;
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        List<MetadataValue> metadata = projectile.getMetadata(MetadataStorage.PROJECTILE_METADATA);
        if(metadata.isEmpty())
            return;

        if(event.getHitBlock() != null) {
            if (WorldGuardUtil.isDeniedFlag(event.getHitBlock().getLocation(), null, Flags.BLOCK_BREAK)) {
                return;
            }
        }

        if(event.getHitEntity() != null) {
            boolean isCitizensNPC = event.getHitEntity().hasMetadata("NPC");
            if (isCitizensNPC) return;

            if (WorldGuardUtil.isDeniedFlag(event.getHitEntity().getLocation(), null, Flags.PVP)) {
                return;
            }
        }

        for (MetadataValue metadatum : metadata) {
            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(metadatum.asString());
            if(customItemByKey.isEmpty()) continue;


            CustomItem customItem = customItemByKey.get();
            if(event.getHitEntity() != null) {
                if (!regionManager.canUseItem(customItem, event.getHitEntity().getLocation())) {
                    return;
                }
            }
            if(event.getHitBlock() != null) {
                if (!regionManager.canUseItem(customItem, event.getHitBlock().getLocation())) {
                    return;
                }
            }

            CustomProjectileHitEvent customProjectileHitEvent = new CustomProjectileHitEvent(projectile, customItem, event.getHitEntity(), event.getHitBlock());
            Bukkit.getPluginManager().callEvent(customProjectileHitEvent);

            if(customProjectileHitEvent.isCancelled())
                return;


            DebugMode.debug(customItemByKey.get());
            event.setCancelled(customProjectileHitEvent.isCancelEvent());
            return;
        }
    }
}
