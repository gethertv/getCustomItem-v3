package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.SnowballTPItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.TeleportUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class SnowballTeleport extends AbstractCustomItemListener<SnowballTPItem> {

    private final Plugin plugin;

    public SnowballTeleport(Plugin plugin, ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof SnowballTPItem snowballTPItem)) return;
        if (!snowballTPItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, snowballTPItem, event.getItemStack(), event.getEquipmentSlot())) return;

        snowballTPItem.playSound(player.getLocation());
        Snowball snowball = snowballTPItem.throwSnowball(player);

        snowball.setMetadata(MetadataStorage.PROJECTILE_LOCATION, new FixedMetadataValue(plugin, player.getLocation().clone()));
        snowball.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(plugin, snowballTPItem.getItemID()));
    }

    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if (!(event.getCustomItem() instanceof SnowballTPItem snowballTPItem)) return;
        if (!snowballTPItem.isEnabled()) return;

        // take a permission which ignore received effect
        if (hitPlayer.hasPermission(snowballTPItem.getPermissionBypass()))
            return;

        // hook essentialsX fly
        if (GetCustomItem.getInstance().getHookManager().getEssentialsXHook().hasFlyPermission(shooter)  && !(shooter.hasPermission(snowballTPItem.getPermissionBypass())))
            return;

        List<MetadataValue> locationMetadata = projectile.getMetadata(MetadataStorage.PROJECTILE_LOCATION);
        if (!locationMetadata.isEmpty()) {
            Location shooterLocation = (Location) locationMetadata.get(0).value();
            Location hitPlayerLocation = hitPlayer.getLocation().clone();

            if (snowballTPItem.isSafeTeleport()) {
                TeleportUtil.safeTp(hitPlayer, shooterLocation);
                TeleportUtil.safeTp(shooter, hitPlayerLocation);
            } else {
                hitPlayer.teleport(shooterLocation);
                shooter.teleport(hitPlayerLocation);
            }

            snowballTPItem.notifyYourself(shooter);
            snowballTPItem.notifyOpponents(hitPlayer);
        }

    }
}