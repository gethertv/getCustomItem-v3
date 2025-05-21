package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.PokeballItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.TeleportUtil;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class PokeballListener extends AbstractCustomItemListener<PokeballItem> {

    public PokeballListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof PokeballItem pokeballItem)) return;
        if (!pokeballItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (WorldGuardUtil.isDeniedFlag(player.getLocation(), player, Flags.PVP))
            return;

        if (!canUseItem(player, pokeballItem, event.getItemStack(), event.getEquipmentSlot())) return;

        Arrow arrow = pokeballItem.throwEntity(player);
        if (arrow != null) {
            arrow.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(GetCustomItem.getInstance(), pokeballItem.getItemID()));
            arrow.setMetadata(MetadataStorage.PROJECTILE_LOCATION, new FixedMetadataValue(GetCustomItem.getInstance(), player.getLocation().clone()));
        }

        pokeballItem.playSound(player.getLocation());
    }


    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if(!(projectile.getShooter() instanceof Player shooter)) return;
        if(!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if(!(event.getCustomItem() instanceof PokeballItem pokeballItem)) return;
        if (!pokeballItem.isEnabled()) return;

        // take a permission which ignore received effect
        if (hitPlayer.hasPermission(pokeballItem.getPermissionByPass()))
            return;

        // hook essentialsX fly
        if(GetCustomItem.getInstance().getHookManager().getEssentialsXHook().hasFlyPermission(shooter))
            return;

        List<MetadataValue> locationMetadata = projectile.getMetadata(MetadataStorage.PROJECTILE_LOCATION);
        if (!locationMetadata.isEmpty()) {
            Location shooterOriginalLoc = (Location) locationMetadata.get(0).value();
            if (pokeballItem.isSafeTeleport()) {
                TeleportUtil.safeTp(hitPlayer, shooterOriginalLoc);
            } else {
                hitPlayer.teleport(shooterOriginalLoc);
            }

            pokeballItem.notifyYourself(shooter);
            pokeballItem.notifyOpponents(hitPlayer);
        }

    }

}