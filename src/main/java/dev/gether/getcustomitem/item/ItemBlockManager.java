package dev.gether.getcustomitem.item;

import dev.gether.getcustomitem.GetCustomItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemBlockManager {
    @Getter
    private final Map<UUID, Long> blockedPlayers = new HashMap<>();
    private final Map<UUID, BukkitTask> removeBlockTasks = new HashMap<>();

    /**
     * Blocks a player from using custom items for a specified duration
     * @param player The player to block
     * @param durationSeconds The duration in seconds
     * @return true if player wasn't already blocked, false otherwise
     */
    public boolean blockPlayer(Player player, int durationSeconds) {
        UUID uuid = player.getUniqueId();
        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        
        // Cancel any existing removal task
        if (removeBlockTasks.containsKey(uuid)) {
            removeBlockTasks.get(uuid).cancel();
            removeBlockTasks.remove(uuid);
        }
        
        // Create a new task to remove the block after the duration
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                blockedPlayers.remove(uuid);
                removeBlockTasks.remove(uuid);
            }
        }.runTaskLater(GetCustomItem.getInstance(), durationSeconds * 20L);
        
        removeBlockTasks.put(uuid, task);
        
        // Return if player wasn't blocked before (for message purposes)
        return blockedPlayers.put(uuid, endTime) == null;
    }
    
    /**
     * Checks if a player is currently blocked from using custom items
     * @param player The player to check
     * @return true if player is blocked, false otherwise
     */
    public boolean isBlocked(Player player) {
        UUID uuid = player.getUniqueId();
        if (!blockedPlayers.containsKey(uuid)) {
            return false;
        }
        
        // Check if block has expired
        long endTime = blockedPlayers.get(uuid);
        if (System.currentTimeMillis() > endTime) {
            blockedPlayers.remove(uuid);
            if (removeBlockTasks.containsKey(uuid)) {
                removeBlockTasks.get(uuid).cancel();
                removeBlockTasks.remove(uuid);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the remaining block time in seconds
     * @param player The player to check
     * @return Remaining time in seconds, or 0 if not blocked
     */
    public int getRemainingTime(Player player) {
        UUID uuid = player.getUniqueId();
        if (!blockedPlayers.containsKey(uuid)) {
            return 0;
        }
        
        long endTime = blockedPlayers.get(uuid);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime > endTime) {
            return 0;
        }
        
        return (int) ((endTime - currentTime) / 1000);
    }
    
    /**
     * Removes a block from a player
     * @param player The player to unblock
     */
    public void unblockPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        blockedPlayers.remove(uuid);
        
        if (removeBlockTasks.containsKey(uuid)) {
            removeBlockTasks.get(uuid).cancel();
            removeBlockTasks.remove(uuid);
        }
    }
}