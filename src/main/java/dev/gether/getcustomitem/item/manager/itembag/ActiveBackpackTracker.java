package dev.gether.getcustomitem.item.manager.itembag;

import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveBackpackTracker {
    private final Map<UUID, BackpackSession> activeBackpacks = new ConcurrentHashMap<>();

    @Data
    public static class BackpackSession {
        private final UUID playerUUID;
        private final String playerName;
        private final long openTime;

        public BackpackSession(Player player) {
            this.playerUUID = player.getUniqueId();
            this.playerName = player.getName();
            this.openTime = System.currentTimeMillis();
        }
    }

    public boolean isBackpackOpen(UUID backpackUUID) {
        return activeBackpacks.containsKey(backpackUUID);
    }

    public BackpackSession getActiveSession(UUID backpackUUID) {
        return activeBackpacks.get(backpackUUID);
    }

    public void registerOpenBackpack(UUID backpackUUID, Player player) {
        activeBackpacks.put(backpackUUID, new BackpackSession(player));
    }

    public void unregisterBackpack(UUID backpackUUID) {
        activeBackpacks.remove(backpackUUID);
    }

    public UUID closeConflictingSession(UUID backpackUUID, Player newPlayer) {
        BackpackSession existingSession = activeBackpacks.get(backpackUUID);
        if (existingSession == null) return backpackUUID;

        Player existingPlayer = Bukkit.getPlayer(existingSession.getPlayerUUID());
        if (existingPlayer != null && existingPlayer.isOnline()) {
            existingPlayer.closeInventory();

            UUID newBackpackUUID = UUID.randomUUID();

            MessageUtil.logMessage(ConsoleColor.GREEN, String.format(
                    "Duplicate backpack access detected! Old BackpackUUID: %s, New BackpackUUID: %s\n" +
                            "Previous player: %s (%s)\n" +
                            "New player: %s (%s)",
                    backpackUUID,
                    newBackpackUUID,
                    existingSession.getPlayerName(),
                    existingSession.getPlayerUUID(),
                    newPlayer.getName(),
                    newPlayer.getUniqueId()
            ));

            return newBackpackUUID;
        }
        return backpackUUID;
    }
}