package dev.gether.getcustomitem.listener.damage;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.PushItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class PushItemListener extends AbstractCustomItemListener<PushItem> {

    public PushItemListener(ItemManager itemManager,
                            CooldownManager cooldownManager,
                            FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof PushItem pushItem)) return;
        if (!pushItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, pushItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // calculate the direction and final position
        Vector direction = damager.getLocation().getDirection().normalize().multiply(pushItem.getPushPower());
        Location finalLocation = victim.getLocation().add(direction);

        // check if the final location is in a non-PvP zone
        if (WorldGuardUtil.isDeniedFlag(finalLocation, victim, Flags.PVP)) {
            return;
        }

        // if it's safe to push, proceed with the push
        pushItem.playSound(damager.getLocation()); // play sound

        // alerts
        pushItem.notifyYourself(damager);
        pushItem.notifyOpponents(victim); // alert opponent

        // Apply the push
        victim.setVelocity(direction);
    }

}
