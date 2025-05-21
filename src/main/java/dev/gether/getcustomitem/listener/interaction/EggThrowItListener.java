package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.EggThrowItItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.List;

public class EggThrowItListener extends AbstractCustomItemListener<EggThrowItItem> {


    public EggThrowItListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EggThrowItItem eggThrowItItem)) return;
        if (!eggThrowItItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, eggThrowItItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        eggThrowItItem.playSound(player.getLocation()); // play sound

        // clean cobweb
        eggThrowItItem.throwEgg(player);

    }

    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Egg egg = event.getEgg();

        List<MetadataValue> metadata = egg.getMetadata(MetadataStorage.PROJECTILE_METADATA);
        if(metadata.isEmpty()) return;

        event.setHatching(false);
    }


    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if (!(event.getCustomItem() instanceof EggThrowItItem eggThrowItItem)) return;
        if (!eggThrowItItem.isEnabled()) return;

        if (hitPlayer.hasPermission(eggThrowItItem.getPermissionBypass()))
            return;

        if (eggThrowItItem.isFly() && (hitPlayer.isFlying() || hitPlayer.getAllowFlight())) {
            hitPlayer.setFlying(false);
            hitPlayer.setAllowFlight(false);
        }

        projectile.remove();

        Vector velocity = hitPlayer.getVelocity();
        velocity.setY(eggThrowItItem.getShootPower());

        hitPlayer.setVelocity(velocity);

        // alert yourself
        eggThrowItItem.notifyYourself(shooter);
        eggThrowItItem.notifyOpponents(hitPlayer);

    }

}