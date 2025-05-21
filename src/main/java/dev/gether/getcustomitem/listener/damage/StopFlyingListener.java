package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.PushItem;
import dev.gether.getcustomitem.item.customize.StopFlyingItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StopFlyingListener extends AbstractCustomItemListener<StopFlyingItem> {

    private final GetCustomItem plugin;
    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();

    public StopFlyingListener(ItemManager itemManager,
                              CooldownManager cooldownManager,
                              FileManager fileManager,
                              GetCustomItem plugin) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof StopFlyingItem stopFlyingItem)) return;
        if (!stopFlyingItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, stopFlyingItem, event.getItemStack(), event.getEquipmentSlot())) return;

        stopFlyingItem.notifyYourself(damager);
        stopFlyingItem.notifyOpponents(victim);

        applyStopFlyingEffect(victim, stopFlyingItem.getStopFlyingTime());
    }

    @EventHandler
    public void onElytraUse(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.hasMetadata(MetadataStorage.PLAYER_MARK)) {
            event.setCancelled(true);
            MessageUtil.sendMessage(player, fileManager.getLangConfig().getCannotUseElytraWhileHooked());
        }
    }

//    @EventHandler
//    public void onCustomFish(CustomFishEvent event) {
//        if (!(event.getCustomItem() instanceof StopFlyingItem stopFlyingItem)) return;
//        if (!stopFlyingItem.isEnabled()) return;
//
//        Player player = event.getPlayer();
//
//        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
//        if (!(event.getCaught() instanceof Player hookedPlayer)) return;
//
//        if (!canUseItem(player, stopFlyingItem, event.getItemStack(), event.getEquipmentSlot())) return;
//
//        stopFlyingItem.notifyYourself(player);
//        stopFlyingItem.notifyOpponents(hookedPlayer);
//
//        applyStopFlyingEffect(hookedPlayer, stopFlyingItem.getStopFlyingTime());
//    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeEffect(player);
    }

    private void applyStopFlyingEffect(Player player, int duration) {
        removeEffect(player);

        player.setMetadata(MetadataStorage.PLAYER_MARK, new FixedMetadataValue(plugin, true));
        player.setGliding(false);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                removeEffect(player);
                MessageUtil.sendMessage(player, fileManager.getLangConfig().getCanFlyAgain());
            }
        };

        task.runTaskLater(plugin, duration * 20L);
        activeEffects.put(player.getUniqueId(), task);
    }

    private void removeEffect(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (activeEffects.containsKey(playerUUID)) {
            activeEffects.get(playerUUID).cancel();
            activeEffects.remove(playerUUID);
        }
        if (player.hasMetadata(MetadataStorage.PLAYER_MARK)) {
            player.removeMetadata(MetadataStorage.PLAYER_MARK, plugin);
        }
    }
}