package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.InstaHealItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

public class InstaHealListener extends AbstractCustomItemListener<InstaHealItem> {

    private final Random random = new Random();

    public InstaHealListener(ItemManager itemManager,
                             CooldownManager cooldownManager,
                             FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }



    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof InstaHealItem instaHealItem)) return;
        if (!instaHealItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, instaHealItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // play sound
        instaHealItem.playSound(player.getLocation()); // play sound

        // chance to insta heal
        if(random.nextDouble() < (instaHealItem.getChance() / 100d)) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

            // alert yourself
            instaHealItem.notifyYourself(player);
        }

    }

}