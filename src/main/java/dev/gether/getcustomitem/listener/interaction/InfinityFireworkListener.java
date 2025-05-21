package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.InfinityFireworkItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class InfinityFireworkListener extends AbstractCustomItemListener<InfinityFireworkItem> {

    public InfinityFireworkListener(ItemManager itemManager,
                                    CooldownManager cooldownManager,
                                    FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }



    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof InfinityFireworkItem infinityFireworkItem)) return;
        if (!infinityFireworkItem.isEnabled()) return;

        Player player = event.getPlayer();
        if(!player.isGliding() && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelEvent(false);
        event.setCancelled(false);

        ItemStack itemStack = event.getItemStack().clone();

        if (!canUseItem(player, infinityFireworkItem, itemStack, event.getEquipmentSlot())) {
            event.setCancelEvent(true);
            return;
        };

//        player.getInventory().setItem(event.getEquipmentSlot(), null);

        // play sound
        infinityFireworkItem.playSound(player.getLocation()); // play sound

        new BukkitRunnable() {
            @Override
            public void run() {
               player.getInventory().setItem(event.getEquipmentSlot(), itemStack);
            }
        }.runTaskLater(GetCustomItem.getInstance(), 1L);
    }

}