package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ShuffleInventoryItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ShuffleInvListener extends AbstractCustomItemListener<ShuffleInventoryItem> {

    public ShuffleInvListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ShuffleInventoryItem shuffleInventoryItem)) return;
        if (!shuffleInventoryItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, shuffleInventoryItem, event.getItemStack(), event.getEquipmentSlot())) return;

        shuffleInventoryItem.playSound(damager.getLocation());
        shuffleInventoryItem.notifyYourself(damager);
        shuffleInventoryItem.notifyOpponents(victim);

        shuffle(victim, shuffleInventoryItem.getShuffleSlots());
    }

    private void shuffle(Player player, List<Integer> slots) {
        Inventory inventory = player.getInventory();

        Map<Integer, ItemStack> itemsToShuffle = new HashMap<>();
        slots.forEach(slot -> {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                itemsToShuffle.put(slot, item);
                inventory.clear(slot);
            }
        });

        List<ItemStack> shuffledItems = new ArrayList<>(itemsToShuffle.values());
        Collections.shuffle(shuffledItems);

        List<Integer> emptySlots = slots.stream()
                .filter(itemsToShuffle::containsKey)
                .collect(Collectors.toList());

        for (int i = 0; i < shuffledItems.size(); i++) {
            inventory.setItem(emptySlots.get(i), shuffledItems.get(i));
        }
    }
}