package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ForcedPumpkinMask;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ForcedPumpkinMaskListener extends AbstractCustomItemListener<ForcedPumpkinMask> {

    private final Map<UUID, BukkitRunnable> activePumpkins = new HashMap<>();
    private final Map<UUID, ItemStack> previousHelmets = new HashMap<>();
    private final Map<UUID, Long> pumpkinEndTime = new ConcurrentHashMap<>();

    public ForcedPumpkinMaskListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ForcedPumpkinMask forcedPumpkinMask)) return;
        if (!forcedPumpkinMask.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, forcedPumpkinMask, event.getItemStack(), event.getEquipmentSlot())) return;

        forcedPumpkinMask.playSound(player.getLocation());
        forcedPumpkinMask.notifyYourself(player);

        EntityUtil.findNearbyEntities(player.getLocation(), 5, Player.class).stream()
                .filter(target -> target != player)
                .filter(target -> !target.hasMetadata("NPC"))
                .filter(target -> !WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP))
                .forEach(target -> applyPumpkin(target, forcedPumpkinMask));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) return;
        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType() != Material.CARVED_PUMPKIN) return;
        if (!activePumpkins.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePumpkinEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {
        removePumpkinEffect(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE) {
            removePumpkinEffect(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPreDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();

        if (activePumpkins.containsKey(playerUUID) && previousHelmets.containsKey(playerUUID)) {
            if (activePumpkins.get(playerUUID) != null) {
                activePumpkins.get(playerUUID).cancel();
            }

            ItemStack pumpkinToRemove = null;
            for (ItemStack item : event.getDrops()) {
                if (item != null && item.getType() == Material.CARVED_PUMPKIN) {
                    pumpkinToRemove = item;
                    break;
                }
            }

            if (pumpkinToRemove != null) {
                event.getDrops().remove(pumpkinToRemove);

                ItemStack originalHelmet = previousHelmets.get(playerUUID);
                if (originalHelmet != null) {
                    event.getDrops().add(originalHelmet);
                }
            }

            cleanupPumpkinData(playerUUID);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            UUID playerUUID = player.getUniqueId();
            if (activePumpkins.containsKey(playerUUID) && previousHelmets.containsKey(playerUUID)) {
                ItemStack originalHelmet = previousHelmets.get(playerUUID);
                player.getInventory().setHelmet(originalHelmet);

                if (activePumpkins.get(playerUUID) != null) {
                    activePumpkins.get(playerUUID).cancel();
                }
                cleanupPumpkinData(playerUUID);
            }
        }
    }

    private void applyPumpkin(Player target, ForcedPumpkinMask item) {
        UUID targetUUID = target.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long additionalTime = item.getSeconds() * 1000L;

        if (!previousHelmets.containsKey(targetUUID)) {
            previousHelmets.put(targetUUID, target.getInventory().getHelmet());
        }

        target.getInventory().setHelmet(new ItemStack(Material.CARVED_PUMPKIN));

        item.notifyOpponents(target);

        long newEndTime = pumpkinEndTime.getOrDefault(targetUUID, currentTime) + additionalTime;
        pumpkinEndTime.put(targetUUID, newEndTime);

        if (activePumpkins.containsKey(targetUUID)) {
            activePumpkins.get(targetUUID).cancel();
        }

        BukkitRunnable pumpkinTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() >= pumpkinEndTime.getOrDefault(targetUUID, 0L)) {
                    restorePreviousHelmet(target);
                    this.cancel();
                }
            }
        };

        pumpkinTask.runTaskTimer(GetCustomItem.getInstance(), 20L, 20L); // Sprawdza co sekundę
        activePumpkins.put(targetUUID, pumpkinTask);
    }

    private void restorePreviousHelmet(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!target.isOnline()) {
            cleanupPumpkinData(targetUUID);
            return;
        }

        ItemStack helmet = target.getInventory().getHelmet();
        if (helmet != null && helmet.getType() == Material.CARVED_PUMPKIN) {
            ItemStack previousHelmet = previousHelmets.get(targetUUID);
            target.getInventory().setHelmet(previousHelmet);
        }

        cleanupPumpkinData(targetUUID);
    }

    private void cleanupPumpkinData(UUID playerUUID) {
        activePumpkins.remove(playerUUID);
        previousHelmets.remove(playerUUID);
        pumpkinEndTime.remove(playerUUID);
    }

    private void removePumpkinEffect(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (activePumpkins.containsKey(playerUUID)) {
            activePumpkins.get(playerUUID).cancel();
            restorePreviousHelmet(player);
        }
    }
}