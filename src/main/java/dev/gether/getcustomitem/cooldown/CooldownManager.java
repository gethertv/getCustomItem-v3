package dev.gether.getcustomitem.cooldown;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final FileManager fileManager;
    private Map<String, Long> cooldowns = new HashMap<>(); // key: UUID + CategoryItem, value: end time in ms
    private final GetCustomItem plugin;
    @Getter
    protected HashMap<UUID, Long> blockUseItem = new HashMap<>();

    public CooldownManager(FileManager fileManager, GetCustomItem plugin) {
        this.fileManager = fileManager;
        this.plugin = plugin;
        startCleanupTask();
    }

    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredCooldowns();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60, 20L * 60); // 20 tick * 60 = 1 min
    }

    private void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();

        Iterator<Map.Entry<String, Long>> iterator = cooldowns.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (entry.getValue() <= currentTime) {
                iterator.remove();
            }
        }
    }

    public double getCooldownSecond(Player player, CustomItem customItem) {
        if(customItem.getCooldown() == 0)
            return 0;

        String key = getPlayerKeyItem(player, customItem);
        Long cooldownEndMS = cooldowns.get(key);
        long currentTimeMillis = System.currentTimeMillis();

        if(cooldownEndMS == null || cooldownEndMS <= currentTimeMillis) {
            return 0;
        }

        long diffTime = cooldownEndMS - currentTimeMillis;
        double secondsLeft = (double) diffTime / 1000;

        return Double.parseDouble(String.format(Locale.US, "%.2f", secondsLeft));
    }

    public void setCooldown(Player player, CustomItem customItem) {
        if(customItem.getCooldown() == 0)
            return;

        String key = getPlayerKeyItem(player, customItem);
        long cooldownEndMS = System.currentTimeMillis() + 1000L * customItem.getCooldown();

        cooldowns.put(key, cooldownEndMS);

        if(customItem.isVisualCooldown() && !player.hasPermission(customItem.getPermissionBypass()))
            player.setCooldown(customItem.getItemStack().getType(), 20 * customItem.getCooldown());
    }

    private String getPlayerKeyItem(Player player, CustomItem customItem) {
        return player.getUniqueId() + customItem.getCategoryName();
    }

    public void clearAllCache(Player player) {
        fileManager.getCustomItems().forEach(customItem -> {
            String key = getPlayerKeyItem(player, customItem);
            cooldowns.remove(key);
        });
    }

    public boolean isOnCooldown(String key) {
        if (!cooldowns.containsKey(key)) {
            return false;
        }
        return System.currentTimeMillis() < cooldowns.get(key);
    }

    public long getCooldownTime(String key) {
        if (!cooldowns.containsKey(key)) {
            return 0;
        }
        long cooldownTime = cooldowns.get(key) - System.currentTimeMillis();
        return Math.max(cooldownTime, 0);
    }

    public void setCooldown(String key, int seconds) {
        long cooldownTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(key, cooldownTime);
    }
}