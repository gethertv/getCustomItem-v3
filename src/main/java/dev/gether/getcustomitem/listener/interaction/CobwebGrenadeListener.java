
package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.customize.CobwebGrenade;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class CobwebGrenadeListener extends AbstractCustomItemListener<CobwebGrenade> {

    private final ItemManager itemManager;

    public CobwebGrenadeListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.itemManager = itemManager;
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CobwebGrenade cobwebGrenade)) return;
        if (!cobwebGrenade.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, cobwebGrenade, event.getItemStack(), event.getEquipmentSlot())) return;

        // create grenade
        ThrownPotion thrownPotion = (ThrownPotion) player.getWorld().spawnEntity(player.getLocation().clone().add(0, 1.1, 0), EntityType.SPLASH_POTION);
        thrownPotion.setItem(event.getItemStack());

        Location playerLocation = player.getLocation().clone().add(0, cobwebGrenade.getHeightVelocity(), 0);
        Vector velocity = playerLocation.getDirection().multiply(cobwebGrenade.getMultiply());
        thrownPotion.setVelocity(velocity); // throw grenade

        // particles and sound
        cobwebGrenade.runParticles(thrownPotion); // particles
        cobwebGrenade.playSound(player.getLocation()); // play sound

        // alert yourself
        cobwebGrenade.notifyYourself(player);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPotionSplash(PotionSplashEvent event) {
        if(event.isCancelled()) return;

        ThrownPotion potion = event.getPotion();
        Location location = event.getEntity().getLocation();
        ItemStack itemStack = potion.getItem();

        List<CustomItem> allCustomItemByType = itemManager.findAllCustomItemByType(ItemType.COBWEB_GRENADE);
        if(allCustomItemByType.isEmpty())
            return;

        for (CustomItem customItemByType : allCustomItemByType) {
            if (customItemByType instanceof CobwebGrenade cobwebGrenade) {
                if (cobwebGrenade.isCustomItem(itemStack)) {
                    cobwebGrenade.spawnCobweb(location); // spawn cobweb
                }
            }
        }

    }

}
