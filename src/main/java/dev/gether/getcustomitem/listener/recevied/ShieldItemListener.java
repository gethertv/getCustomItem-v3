package dev.gether.getcustomitem.listener.recevied;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.event.CustomItemReceivedDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ShieldItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

public class ShieldItemListener extends AbstractCustomItemListener<ShieldItem> {


    private final Random random = new Random();

    public ShieldItemListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemReceivedDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ShieldItem shieldItem)) return;
        if (!shieldItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        double winTicket = random.nextDouble() * 100;
        if(winTicket <= shieldItem.getBlockChance()) {

            if (!canUseItem(victim, shieldItem, event.getItemStack(), event.getEquipmentSlot())) return;

            event.setCancelEvent(true);

            // particles and sound
            shieldItem.playSound(victim.getLocation()); // play sound

            // alerts
            shieldItem.notifyYourself(damager);
            shieldItem.notifyOpponents(victim);

        }

    }

}