package dev.gether.getcustomitem.region;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import org.bukkit.Location;

import java.util.Comparator;
import java.util.List;

public class RegionManager {

    private final GetCustomItem plugin;
    private final FileManager fileManager;

    public RegionManager(GetCustomItem plugin, FileManager fileManager) {
        this.plugin = plugin;
        this.fileManager = fileManager;
    }

    public boolean canUseItem(CustomItem item, Location location) {
        List<CustomRegion> regions = fileManager.getRegionsConfig().getRegions().stream()
                .filter(r -> isInRegion(location, r))
                .sorted(Comparator.comparingInt(CustomRegion::getPriority).reversed())
                .toList();

        if (regions.isEmpty()) {
            return true;
        }

        String itemId = item.getItemID();

        for (CustomRegion region : regions) {
            if (region.getDisabledItems().contains("ALL") || region.getDisabledItems().contains(itemId)) {
                return false;
            }
            if (region.getAllowedItems().contains("ALL") || region.getAllowedItems().contains(itemId)) {
                return true;
            }
        }

        return true;
    }


    public boolean isInRegion(Location loc, CustomRegion region) {
        if (!loc.getWorld().equals(region.getPos1().getWorld())) return false;

        double minX = Math.min(region.getPos1().getX(), region.getPos2().getX());
        double maxX = Math.max(region.getPos1().getX(), region.getPos2().getX());
        double minY = Math.min(region.getPos1().getY(), region.getPos2().getY());
        double maxY = Math.max(region.getPos1().getY(), region.getPos2().getY());
        double minZ = Math.min(region.getPos1().getZ(), region.getPos2().getZ());
        double maxZ = Math.max(region.getPos1().getZ(), region.getPos2().getZ());

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }

}