package dev.gether.getcustomitem.listener.resurrect;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemResurrectEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.DeathTotemItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;

public class DeathTotemListener extends AbstractCustomItemListener<DeathTotemItem> {

    public DeathTotemListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemDamage(CustomItemResurrectEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof DeathTotemItem deathTotemItem)) return;
        if (!deathTotemItem.isEnabled()) return;
        Player player = event.getPlayer();
        if (!canUseItem(player, deathTotemItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);

        deathTotemItem.playSound(player.getLocation());
        deathTotemItem.notifyYourself(player);

        List<Player> nearPlayers = EntityUtil.findNearbyEntities(player.getLocation(), 6, Player.class).stream()
                .filter(p -> !p.getName().equals(player.getName())).toList();

        nearPlayers.forEach(deathTotemItem::notifyOpponents);

        player.setHealth(player.getMaxHealth());

        Location spawnLocation = fileManager.getConfig().getSpawnLocation();
        if (spawnLocation != null) {
            Bukkit.getScheduler().runTask(GetCustomItem.getInstance(), () -> player.teleport(spawnLocation));
        } else {
            MessageUtil.sendMessage(player, "&c[getCustomItem] DeathTotem not found spawn location. /getcustomitem setspawn");
        }

    }
}
