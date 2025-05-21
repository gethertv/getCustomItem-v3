package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.SchematicPlacerItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.schematic.Schematic;
import dev.gether.getcustomitem.schematic.SchematicManager;
import dev.gether.getcustomitem.schematic.SchematicPasteOptions;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class SchematicPlacerListener extends AbstractCustomItemListener<SchematicPlacerItem> {

    private final SchematicManager schematicManager;
    private final GetCustomItem plugin;

    @Getter
    private static final Map<UUID, Set<TemporarySchematic>> activeStructures = new HashMap<>();

    public SchematicPlacerListener(ItemManager itemManager,
                           CooldownManager cooldownManager,
                           FileManager fileManager,
                           SchematicManager schematicManager,
                           GetCustomItem plugin) {
        super(itemManager, cooldownManager, fileManager);
        this.schematicManager = schematicManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof SchematicPlacerItem schematicPlacerItem)) return;
        if (!schematicPlacerItem.isEnabled()) return;

        Player player = event.getPlayer();
        
        if (!canUseItem(player, schematicPlacerItem, event.getItemStack(), event.getEquipmentSlot())) return;

        String schematicName = schematicPlacerItem.getSchematicName();
        if (!schematicManager.schematicExists(schematicName)) {
            MessageUtil.logMessage(ConsoleColor.RED, "Schematic " + schematicName + " doesn't exist!");
            event.setCancelled(true);
            return;
        }

        SchematicPasteOptions options = SchematicPasteOptions.defaults()
                .setPasteGradually(schematicPlacerItem.isGradualPaste())
                .setBlocksPerTick(schematicPlacerItem.getBlocksPerTick())
                .setTrackBlocks(true);
        
        List<Location> pastedBlocks = new ArrayList<>();

        Location location = player.getLocation();

        schematicManager.pasteSchematic(location, schematicName, options, pastedBlocks);
        
        BukkitTask removalTask = new BukkitRunnable() {
            @Override
            public void run() {
                Collections.reverse(pastedBlocks);
                
                int blocksPerTickRemoval = 20;
                List<List<Location>> chunks = new ArrayList<>();
                
                for (int i = 0; i < pastedBlocks.size(); i += blocksPerTickRemoval) {
                    chunks.add(pastedBlocks.subList(i, Math.min(i + blocksPerTickRemoval, pastedBlocks.size())));
                }
                
                for (int i = 0; i < chunks.size(); i++) {
                    List<Location> chunk = chunks.get(i);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            for (Location blockLoc : chunk) {
                                blockLoc.getBlock().setType(org.bukkit.Material.AIR);
                            }
                        }
                    }.runTaskLater(plugin, i * 2);
                }
                
                Set<TemporarySchematic> playerStructures = activeStructures.get(player.getUniqueId());
                if (playerStructures != null) {
                    playerStructures.removeIf(struct -> struct.getRemovalTask().getTaskId() == this.getTaskId());
                    if (playerStructures.isEmpty()) {
                        activeStructures.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskLater(plugin, schematicPlacerItem.getDuration() * 20L);

        activeStructures.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>())
                .add(new TemporarySchematic(schematicName, pastedBlocks, removalTask));

        schematicPlacerItem.playSound(location);

    }

    @Getter
    private static class TemporarySchematic {
        private final String schematicName;
        private final List<Location> blockLocations;
        private final BukkitTask removalTask;
        
        public TemporarySchematic(String schematicName, List<Location> blockLocations, BukkitTask removalTask) {
            this.schematicName = schematicName;
            this.blockLocations = blockLocations;
            this.removalTask = removalTask;
        }
        
    }

    public static void cancelAllStructures(Player player) {
        cancelAllStructures(player.getUniqueId());
    }

    public static void cancelAllStructures(UUID playerId) {
        Set<TemporarySchematic> playerStructures = activeStructures.get(playerId);
        if (playerStructures != null) {
            for (TemporarySchematic structure : playerStructures) {
                structure.getRemovalTask().cancel();
                for (Location location : structure.getBlockLocations()) {
                    location.getBlock().setType(org.bukkit.Material.AIR);
                }
            }
            activeStructures.remove(playerId);
        }
    }

    public static void cancelAllStructures() {
        for (UUID playerId : new HashSet<>(activeStructures.keySet())) {
            cancelAllStructures(playerId);
        }
    }

}