package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.AntiCobweb;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class AntiCobwebListener extends AbstractCustomItemListener<AntiCobweb> {

    public AntiCobwebListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof AntiCobweb antyCobweb)) return;
        Player player = event.getPlayer();

        if (!antyCobweb.isEnabled()) return;
        if (!canUseItem(player, antyCobweb, event.getItemStack(), event.getEquipmentSlot())) return;

        antyCobweb.playSound(player.getLocation());
        antyCobweb.cleanCobweb(player, player.getLocation());
        antyCobweb.notifyYourself(player);

    }

}