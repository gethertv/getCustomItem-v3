package dev.gether.getcustomitem.inv;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getutils.models.inventory.AbstractInventoryHolder;
import dev.gether.getutils.models.inventory.InventoryConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PreviewInventoryHolder extends AbstractInventoryHolder {

    private GetCustomItem customItem;

    public PreviewInventoryHolder(GetCustomItem plugin, Player player, InventoryConfig inventoryConfig) {
        super(plugin, player, inventoryConfig);
        this.customItem = plugin;
        initializeItems();
    }

    @Override
    protected void initializeItems() {
        int index = 0;
        int[] previewSlots = customItem.getFileManager().getConfig().getPreviewSlots();
        for (CustomItem item : customItem.getFileManager().getCustomItems()) {
            if(!item.isPreviewItem())
                continue;

            int slot = previewSlots[index];
            ItemStack itemStack = item.getItemStack();
            setItem(slot, itemStack);
            index++;
        }
    }
}
