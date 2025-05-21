package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.BubbleTrapItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class BubbleTrapListener extends AbstractCustomItemListener<BubbleTrapItem> {

    public BubbleTrapListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof BubbleTrapItem bubbleTrapItem)) return;
        if(!bubbleTrapItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, bubbleTrapItem, event.getItemStack(), event.getEquipmentSlot())) return;

        bubbleTrapItem.playSound(player.getLocation());
        bubbleTrapItem.createSquare(player.getLocation().getBlock().getLocation());
        bubbleTrapItem.notifyYourself(player);

        EntityUtil.findNearbyEntities(player.getLocation(), bubbleTrapItem.getRadius(), Player.class).stream()
                .filter(p -> !p.getName().equals(player.getName()))
                .forEach(bubbleTrapItem::notifyOpponents);

    }

}
