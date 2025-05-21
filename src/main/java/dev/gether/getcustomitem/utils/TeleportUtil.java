package dev.gether.getcustomitem.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeleportUtil {

    public static void safeTp(Player player, Location location) {
        Location safeLoc = findSafeLocation(location);
        player.teleport(safeLoc);
    }

    private static Location findSafeLocation(Location targetLoc) {
        Location safeLoc = targetLoc.clone();

        if(isSafeLocation(safeLoc)) {
            return safeLoc;
        }

        for(int y = 0; y >= -10; y--) {
            for(int x = -1; x <= 1; x++) {
                for(int z = -1; z <= 1; z++) {
                    Location checkLoc = targetLoc.clone().add(x, y, z);
                    if(isSafeLocation(checkLoc)) {
                        return checkLoc;
                    }
                }
            }
        }

        return targetLoc;
    }

    private static boolean isSafeLocation(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = loc.clone().subtract(0, 1, 0).getBlock();

        return feet.getType() == Material.AIR &&
                head.getType() == Material.AIR &&
                ground.getType().isSolid();
    }
}