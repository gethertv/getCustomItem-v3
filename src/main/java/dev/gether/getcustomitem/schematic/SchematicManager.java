package dev.gether.getcustomitem.schematic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.gether.getutils.models.Cuboid;
import dev.gether.getutils.selector.RegionSelection;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SchematicManager {

    private final JavaPlugin plugin;
    private final File schematicsFolder;
    private final Gson gson;
    private final Map<String, Schematic> loadedSchematics = new HashMap<>();

    public SchematicManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        loadAllSchematics();
    }

    public void reload() {
        loadedSchematics.clear();
        loadAllSchematics();
    }

    public void createSchematic(Player player, String schematicName, RegionSelection regionSelection) {
        if (regionSelection == null || !regionSelection.isComplete()) {
            MessageUtil.sendMessage(player, "&cYou must select two locations with the selector!");
            return;
        }

        Cuboid cuboid = new Cuboid(regionSelection);
        List<Block> blockCuboidWithoutAIR = cuboid.getBlockCuboidWithoutAIR();

        // Znajdź blok centrum (LIME_WOOL)
        Block centerBlock = null;
        for (Block block : blockCuboidWithoutAIR) {
            if(block.getType() == Material.LIME_WOOL) {
                centerBlock = block;
                break;
            }
        }

        if(centerBlock == null) {
            MessageUtil.sendMessage(player, "&cSchematic center block (LIME_WOOL) not found!");
            return;
        }

        // Koordynaty bloku centrum
        int x = centerBlock.getX();
        int y = centerBlock.getY();
        int z = centerBlock.getZ();

        // Tworzenie listy bloków schematu
        List<SchematicBlock> schematicBlocks = new ArrayList<>();
        for (Block block : blockCuboidWithoutAIR) {
            if(block.getType() == Material.LIME_WOOL)
                continue;

            Location location = block.getLocation();
            int diffX = location.getBlockX() - x;
            int diffY = location.getBlockY() - y;
            int diffZ = location.getBlockZ() - z;

            SchematicBlock schematicBlock = new SchematicBlock(block.getBlockData().getAsString(), diffX, diffY, diffZ);
            schematicBlocks.add(schematicBlock);
        }

        // Tworzenie obiektu schematu
        Schematic schematic = new Schematic(schematicName, schematicBlocks);

        // Zapisywanie schematu do pliku
        saveSchematic(player, schematicName, schematic);
    }

    public void saveSchematic(Player player, String schematicName, Schematic schematic) {
        File schematicFile = new File(schematicsFolder, schematicName + ".json");

        try (FileWriter writer = new FileWriter(schematicFile)) {
            gson.toJson(schematic, writer);

            // Dodanie schematu do pamięci
            loadedSchematics.put(schematicName.toLowerCase(), schematic);

            MessageUtil.sendMessage(player, "&aSchematic '" + schematicName + "' saved successfully!");
        } catch (IOException e) {
            MessageUtil.sendMessage(player, "&cFailed to save schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadAllSchematics() {
        loadedSchematics.clear();

        if (!schematicsFolder.exists()) {
            return;
        }

        File[] files = schematicsFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                Schematic schematic = gson.fromJson(reader, Schematic.class);
                String name = file.getName().replace(".json", "").toLowerCase();
                loadedSchematics.put(name, schematic);

                MessageUtil.logMessage(ConsoleColor.GREEN, "Loaded schematic: " + name);
            } catch (IOException e) {
                MessageUtil.logMessage(ConsoleColor.RED,  "Failed to load schematic " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        MessageUtil.logMessage(ConsoleColor.GREEN,  "&aLoaded " + loadedSchematics.size() + " schematics.");
    }

    public Optional<Schematic> getSchematic(String name) {
        return Optional.ofNullable(loadedSchematics.get(name.toLowerCase()));
    }

    public boolean schematicExists(String name) {
        return loadedSchematics.containsKey(name.toLowerCase());
    }

    public boolean deleteSchematic(String name) {
        File schematicFile = new File(schematicsFolder, name + ".json");
        if (schematicFile.exists() && schematicFile.delete()) {
            loadedSchematics.remove(name.toLowerCase());
            return true;
        }
        return false;
    }

    public List<String> getSchematicNames() {
        return new ArrayList<>(loadedSchematics.keySet());
    }

    public void pasteSchematic(Location location, String schematicName, SchematicPasteOptions options, List<Location> trackedBlocks) {
        Optional<Schematic> schematicOpt = getSchematic(schematicName);

        if (schematicOpt.isEmpty()) {
            MessageUtil.logMessage(ConsoleColor.RED, "Schematic not found: " + schematicName);
            return;
        }

        Schematic schematic = schematicOpt.get();

        if (options.isPasteGradually()) {
            pasteSchematicGradually(plugin, location, schematic, options, trackedBlocks);
        } else {
            for (SchematicBlock block : schematic.getBlocks()) {
                Location blockLoc = location.clone().add(
                        block.getVectorX(),
                        block.getVectorY(),
                        block.getVectorZ()
                );

                if (blockLoc.getBlock().getType() != org.bukkit.Material.AIR) {
                    continue;
                }

                blockLoc.getBlock().setBlockData(org.bukkit.Bukkit.createBlockData(block.getBlockData()));
                if (trackedBlocks != null) {
                    trackedBlocks.add(blockLoc);
                }
            }
        }
    }

    private void pasteSchematicGradually(JavaPlugin plugin, Location location, Schematic schematic,
                                         SchematicPasteOptions options, List<Location> trackedBlocks) {
        List<SchematicBlock> blocks = schematic.getBlocks();
        int totalBlocks = blocks.size();
        int blocksPerTick = options.getBlocksPerTick();
        
        List<SchematicBlock> blocksToPlace = new ArrayList<>();
        List<Location> locationsToPlace = new ArrayList<>();

        for (SchematicBlock block : blocks) {
            Location blockLoc = location.clone().add(
                    block.getVectorX(),
                    block.getVectorY(),
                    block.getVectorZ()
            );

            if (blockLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                blocksToPlace.add(block);
                locationsToPlace.add(blockLoc);
            }
        }

        for (int i = 0; i < Math.ceil((double) blocksToPlace.size() / blocksPerTick); i++) {
            final int startIndex = i * blocksPerTick;
            final int endIndex = Math.min((i + 1) * blocksPerTick, blocksToPlace.size());

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int j = startIndex; j < endIndex; j++) {
                        SchematicBlock block = blocksToPlace.get(j);
                        Location blockLoc = locationsToPlace.get(j);

                        if (blockLoc.getBlock().getType() == org.bukkit.Material.AIR) {
                            blockLoc.getBlock().setBlockData(org.bukkit.Bukkit.createBlockData(block.getBlockData()));


                            if (trackedBlocks != null) {
                                trackedBlocks.add(blockLoc);
                            }
                        }
                    }
                }
            }.runTaskLater(plugin, i * 2);
        }
    }

}