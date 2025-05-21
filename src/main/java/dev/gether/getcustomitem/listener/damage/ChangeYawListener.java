package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ChangeYawItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ChangeYawListener extends AbstractCustomItemListener<ChangeYawItem> {

    public ChangeYawListener(ItemManager itemManager,
                             CooldownManager cooldownManager,
                             FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof ChangeYawItem changeYawItem)) return;
        if(!changeYawItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, changeYawItem, event.getItemStack(), event.getEquipmentSlot())) return;

        double winTicket = GetCustomItem.getRandom().nextDouble() * 100.0D;
        if (winTicket <= changeYawItem.getChance()) {
            changeYawItem.playSound(damager.getLocation());

            changeYawItem.notifyYourself(damager);
            changeYawItem.notifyOpponents(victim);

            float currentYaw = victim.getLocation().getYaw();
            float newYaw = (currentYaw + changeYawItem.getPitchDegrees()) % 360.0F;
            victim.setRotation(newYaw, victim.getLocation().getPitch());
        }
    }

}