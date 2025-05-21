package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ThrowingEnderPearlsItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class ThrowingEnderPearlsListener extends AbstractCustomItemListener<ThrowingEnderPearlsItem> {

    public ThrowingEnderPearlsListener(ItemManager itemManager,
                                       CooldownManager cooldownManager,
                                       FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ThrowingEnderPearlsItem throwingEnderPearlsItem)) return;
        if (!throwingEnderPearlsItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, throwingEnderPearlsItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        throwingEnderPearlsItem.playSound(player.getLocation()); // play sound

        // clean cobweb
        throwingEnderPearlsItem.throwEnderPearls(player);

    }
}