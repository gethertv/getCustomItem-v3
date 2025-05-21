package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.DropToInventoryItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DropToInventoryItemListener extends AbstractCustomItemListener<DropToInventoryItem> {

    private final ItemManager itemManager;

    public DropToInventoryItemListener(ItemManager itemManager,
                                       CooldownManager cooldownManager,
                                       FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof DropToInventoryItem dropToInventoryItem)) return;
        if (!dropToInventoryItem.isEnabled()) return;

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            return;
        }

        boolean itemDropToInv = findItemDropToInv(killer.getInventory());
        if (!itemDropToInv) {
            return;
        }

        List<ItemStack> itemsToAdd = new ArrayList<>(event.getDrops());
        event.getDrops().clear();
        itemsToAdd.forEach(itemStack -> {
            PlayerUtil.addItems(killer, itemStack);
        });
    }

    private boolean findItemDropToInv(PlayerInventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if(item==null)
                continue;

            Optional<String> itemID = itemManager.findItemID(item);
            if (itemID.isEmpty())
                continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemID.get());
            if(customItemByKey.isEmpty())
                continue;

            if(!(customItemByKey.get() instanceof DropToInventoryItem))
                continue;

            return true;
        }
        return false;
    }
}