package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.PoliceBatonItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class PoliceBatonListener extends AbstractCustomItemListener<PoliceBatonItem> {

    public PoliceBatonListener(ItemManager itemManager, CooldownManager cooldownManager,
                               FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof PoliceBatonItem policeBatonItem)) return;
        if (!policeBatonItem.isEnabled()) return;
        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, policeBatonItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        policeBatonItem.playSound(damager.getLocation()); // play sound

        // alerts
        policeBatonItem.notifyYourself(damager);
        policeBatonItem.notifyOpponents(victim); // alert opponent

        // Block the victim from using custom items
        blockPlayer(victim, policeBatonItem.getBlockDuration());

    }

}