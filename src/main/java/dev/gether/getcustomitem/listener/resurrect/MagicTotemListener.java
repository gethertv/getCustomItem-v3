package dev.gether.getcustomitem.listener.resurrect;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemResurrectEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.resurrect.MagicTotemItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class MagicTotemListener extends AbstractCustomItemListener<MagicTotemItem> {

    private final FileManager fileManager;

    public MagicTotemListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.fileManager = fileManager;
    }

    @EventHandler
    public void onCustomItemResurrect(CustomItemResurrectEvent event) {
        if(event.isCancelled()) return;
        Player player = event.getPlayer();
        if(!(event.getCustomItem() instanceof MagicTotemItem magicTotemItem)) return;
        if (!magicTotemItem.isEnabled()) return;

        if (!canUseItem(player, magicTotemItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);

        magicTotemItem.notifyYourself(player);
        processDeathWithTotem(player, magicTotemItem);

        EntityUtil.findNearbyEntities(player.getLocation(), 5, Player.class).stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .forEach(magicTotemItem::notifyOpponents);

        Location spawnLocation = fileManager.getConfig().getSpawnLocation();
        if (spawnLocation != null) {
            Bukkit.getScheduler().runTask(GetCustomItem.getInstance(), () -> player.teleport(spawnLocation));
        } else {
            MessageUtil.sendMessage(player, "&c[getCustomItem] MagicTotem not found spawn location. /getcustomitem setspawn");
        }
    }

    private void processDeathWithTotem(Player player, MagicTotemItem magicTotemItem) {
        // after remove the magic totem, take a player inventory to get actually items
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            double chance = GetCustomItem.getRandom().nextDouble() * 100;
            if (chance < magicTotemItem.getChanceLostItem()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                inventory.setItem(i, null);
            }
        }
    }
}