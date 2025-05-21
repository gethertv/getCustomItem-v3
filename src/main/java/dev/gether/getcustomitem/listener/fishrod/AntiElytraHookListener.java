package dev.gether.getcustomitem.listener.fishrod;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomFishEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.customize.AntiElytraHookItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AntiElytraHookListener extends AbstractCustomItemListener<AntiElytraHookItem> {

    private final GetCustomItem plugin;
    private final Map<UUID, FishHook> activeHooks = new HashMap<>();
    private final Map<UUID, BukkitRunnable> hookTrackers = new HashMap<>();

    public AntiElytraHookListener(ItemManager itemManager,
                                  CooldownManager cooldownManager,
                                  FileManager fileManager,
                                  GetCustomItem plugin) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }


    @EventHandler
    public void onCustomItemInteract(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof AntiElytraHookItem)) return;

        event.setCancelEvent(false);
    }

    @EventHandler
    public void onCustomFish(CustomFishEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof AntiElytraHookItem antiElytraHookItem)) return;
        if (!antiElytraHookItem.isEnabled()) return;


        Player fisher = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            startHookTracking(fisher, event.getHook(), antiElytraHookItem, event.getItemStack(), event.getEquipmentSlot());
        } else if (event.getState() == PlayerFishEvent.State.REEL_IN ||
                event.getState() == PlayerFishEvent.State.IN_GROUND) {
            removeHook(fisher);
        }
    }

    private void startHookTracking(Player fisher, FishHook hook, AntiElytraHookItem item, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        cancelExistingTracker(fisher);


        if (!canUseItem(fisher, item, itemStack, equipmentSlot)) return;

        BukkitRunnable tracker = new BukkitRunnable() {
            @Override
            public void run() {
                if (!validateHookState(fisher, hook)) {
                    cleanupAndCancel(fisher, hook);
                    return;
                }

                Location hookLoc = hook.getLocation();
                if (hookLoc.getBlock().getType().isSolid()) {
                    cleanupAndCancel(fisher, hook);
                    return;
                }

                checkForNearbyPlayers(fisher, hook, hookLoc, item);
            }

            private void cleanupAndCancel(Player fisher, FishHook hook) {
                removeHook(fisher);
                if (hook.isValid()) hook.remove();
                cancel();
            }
        };

        tracker.runTaskTimer(plugin, 0L, 1L);
        hookTrackers.put(fisher.getUniqueId(), tracker);
    }

    private boolean validateHookState(Player fisher, FishHook hook) {
        if (!hook.isValid() || !fisher.isOnline()) return false;
        return true;
    }

    private void checkForNearbyPlayers(Player fisher, FishHook hook, Location hookLoc, AntiElytraHookItem item) {
        for (Entity entity : hookLoc.getWorld().getNearbyEntities(hookLoc, 0.5, 0.5, 0.5)) {
            if (!(entity instanceof Player target)) continue;
            if (target.equals(fisher)) continue;

            if (!canHookPlayer(fisher, target, item)) {
                removeHook(fisher);
                hook.remove();
                return;
            }

            hookPlayer(fisher, target, hook, item);
            break;
        }
    }

    private boolean canHookPlayer(Player fisher, Player target, AntiElytraHookItem item) {
        if (target.hasPermission(item.getPermissionBypass())) return false;

        return !WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP) &&
                !WorldGuardUtil.isDeniedFlag(fisher.getLocation(), fisher, Flags.PVP);
    }

    private void hookPlayer(Player fisher, Player target, FishHook hook, AntiElytraHookItem item) {
        if (activeHooks.containsKey(fisher.getUniqueId()) &&
                target.hasMetadata(MetadataStorage.FISHING_HOOK) &&
                target.getMetadata(MetadataStorage.FISHING_HOOK).get(0).value().equals(fisher.getUniqueId())) {
            return;
        }

        if (target.isGliding()) {
            target.setGliding(false);
        }

        target.setMetadata(MetadataStorage.FISHING_HOOK, new FixedMetadataValue(plugin, fisher.getUniqueId()));
        target.setMetadata(MetadataStorage.CUSTOM_ITEM, new FixedMetadataValue(plugin, item.getItemID()));
        activeHooks.put(fisher.getUniqueId(), hook);

        item.notifyYourself(fisher);
        item.notifyOpponents(target);

        cooldownManager.setCooldown(fisher, item);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata(MetadataStorage.FISHING_HOOK)) return;
        if (!player.hasMetadata(MetadataStorage.CUSTOM_ITEM)) return;

        MetadataValue metadataValue = player.getMetadata(MetadataStorage.CUSTOM_ITEM).get(0);
        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(metadataValue.asString());
        if(customItemByKey.isEmpty()) return;

        if(!(customItemByKey.get() instanceof AntiElytraHookItem antiElytraHookItem)) return;
        UUID fisherUUID = (UUID) player.getMetadata(MetadataStorage.FISHING_HOOK).get(0).value();
        checkHookDistance(player, fisherUUID, antiElytraHookItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasMetadata(MetadataStorage.FISHING_HOOK)) return;
        if (!player.hasMetadata(MetadataStorage.CUSTOM_ITEM)) return;

        MetadataValue metadataValue = player.getMetadata(MetadataStorage.CUSTOM_ITEM).get(0);
        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(metadataValue.asString());
        if(customItemByKey.isEmpty()) return;

        if(!(customItemByKey.get() instanceof AntiElytraHookItem antiElytraHookItem)) return;

        UUID fisherUUID = (UUID) player.getMetadata(MetadataStorage.FISHING_HOOK).get(0).value();
        Player fisher = plugin.getServer().getPlayer(fisherUUID);

        if (fisher != null) {
            double distance = player.getLocation().distance(fisher.getLocation());
            if (distance > antiElytraHookItem.getDistance()) {
                cleanupHookedPlayer(player);
            } else {
                event.setCancelled(true);
            }
        } else {
            cleanupHookedPlayer(player);
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        removeHook(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        removeHook(player);
        cleanupHookedPlayer(player);
    }

    private void checkHookDistance(Player player, UUID fisherUUID, AntiElytraHookItem item) {
        Player fisher = plugin.getServer().getPlayer(fisherUUID);
        FishHook hook = activeHooks.get(fisherUUID);

        if (fisher == null || hook == null || !hook.isValid()) {
            cleanupHookedPlayer(player);
            return;
        }

        double distance = player.getLocation().distance(fisher.getLocation());
        if (distance > item.getDistance()) {
            cleanupHookedPlayer(player);
            activeHooks.remove(fisherUUID);
            if (hook.isValid()) {
                hook.remove();
            }

            sendEscapeMessage(player, fisher);
        }
    }

    private void sendEscapeMessage(Player target, Player fisher) {
        itemManager.findCustomItemByType(ItemType.ANTI_ELYTRA_HOOK, fisher.getInventory().getItemInMainHand())
                .ifPresent(item -> {
                    if (item instanceof AntiElytraHookItem antiElytraHookItem) {
                        antiElytraHookItem.sendEscapeMessages(target, fisher);
                    }
                });
    }

    private void cancelExistingTracker(Player fisher) {
        UUID fisherUUID = fisher.getUniqueId();
        if (hookTrackers.containsKey(fisherUUID)) {
            hookTrackers.get(fisherUUID).cancel();
            hookTrackers.remove(fisherUUID);
        }
    }

    private void cleanupHookedPlayer(Player player) {
        if (player.hasMetadata(MetadataStorage.FISHING_HOOK)) {
            player.removeMetadata(MetadataStorage.FISHING_HOOK, plugin);
        }
        if (player.hasMetadata(MetadataStorage.CUSTOM_ITEM)) {
            player.removeMetadata(MetadataStorage.CUSTOM_ITEM, plugin);
        }
    }

    private void removeHook(Player fisher) {
        UUID fisherUUID = fisher.getUniqueId();

        cancelExistingTracker(fisher);

        FishHook hook = activeHooks.remove(fisherUUID);
        if (hook != null && hook.isValid()) {
            hook.remove();
        }

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.hasMetadata(MetadataStorage.FISHING_HOOK)) continue;

            UUID hookedByUUID = (UUID) onlinePlayer.getMetadata(MetadataStorage.FISHING_HOOK).get(0).value();
            if (hookedByUUID.equals(fisherUUID)) {
                cleanupHookedPlayer(onlinePlayer);
            }
        }
    }
}