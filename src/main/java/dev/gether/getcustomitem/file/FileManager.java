package dev.gether.getcustomitem.file;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.file.config.Config;
import dev.gether.getcustomitem.file.config.DatabaseConfig;
import dev.gether.getcustomitem.file.config.LangConfig;
import dev.gether.getcustomitem.file.config.RegionsConfig;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.ConfigManager;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileManager {

    @Getter
    private final LangConfig langConfig = ConfigManager.create(LangConfig.class, it -> {
        it.setFile(new File(GetCustomItem.getInstance().getDataFolder(), "lang.yml"));
        it.load();
    });

    @Getter
    private final Config config = ConfigManager.create(Config.class, it -> {
        it.setFile(new File(GetCustomItem.getInstance().getDataFolder(), "config.yml"));
        it.load();
    });


    @Getter
    private final DatabaseConfig databaseConfig = ConfigManager.create(DatabaseConfig.class, it-> {
        it.setFile(new File(GetCustomItem.getInstance().getDataFolder(), "database.yml"));
        it.load();
    });

    @Getter
    private final RegionsConfig regionsConfig = ConfigManager.create(RegionsConfig.class, it-> {
        it.setFile(new File(GetCustomItem.getInstance().getDataFolder(), "regions.yml"));
        it.load();
    });

    @Getter
    private Set<CustomItem> customItems = new HashSet<>();
    public static File FILE_PATH_ITEMS = new File(GetCustomItem.getInstance().getDataFolder(), "/items/");;

    public FileManager() {
        initDefaultItems();
    }

    public void reload() {
        langConfig.load();
        config.load();
        initDefaultItems();
        customItems.clear();

        loadItems();

    }

    public void loadItems() {
        loadItemsRecursively(FILE_PATH_ITEMS);
    }

    public void initDefaultItems() {
        if(config.isDefaultItems()) {
            DefaultItem defaultItem = new DefaultItem();
            defaultItem.getCustomItems().forEach(customItem -> {
                String itemID = customItem.getItemID();
                MessageUtil.logMessage(ConsoleColor.YELLOW, "[getCustomItem-v3] Default item loading... "+itemID);
                if(itemID == null)
                    return;

                File file = new File(FILE_PATH_ITEMS, "{name}.yml".replace("{name}", itemID));
                if(file.exists())
                    return;

                customItem.setFile(file);
                customItem.load();
            });
        }
    }

    private void loadItemsRecursively(File directory) {
        File[] files = directory.listFiles();
        if (files == null) {
            MessageUtil.logMessage(ConsoleColor.RED, "No files found in " + directory.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                loadItemsRecursively(file);
            } else if (file.getName().endsWith(".yml")) {
                processItemFile(file);
            }
        }
    }

    private void processItemFile(File file) {
        String itemName = file.getName().replace(".yml", "");
        try {
            ItemType itemType = getItemTypeFromFile(file);
            if (itemType == null) {
                MessageUtil.logMessage(ConsoleColor.RED, "Unknown item type for item: " + itemName);
                return;
            }

            Class<? extends CustomItem> itemClass = itemType.getItemClass();
            CustomItem customItem = ConfigManager.create(itemClass, it -> {
                it.setFile(file);
                it.load();
            });

            if (customItem == null) {
                MessageUtil.logMessage(ConsoleColor.RED, "Failed to load item: " + itemName + ". CustomItem is null.");
                return;
            }

            customItem.init();
            this.customItems.add(customItem);
            MessageUtil.logMessage(ConsoleColor.GREEN, "Loaded item " + itemName);
        } catch (Exception e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error loading item " + itemName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ItemType getItemTypeFromFile(File file) {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> data = yaml.load(inputStream);
            if (data.containsKey("itemType")) {
                String itemTypeString = (String) data.get("itemType");
                return ItemType.valueOf(itemTypeString);
            }
        } catch (IOException | IllegalArgumentException e) {
            MessageUtil.logMessage(ConsoleColor.RED, "Error reading itemType from file: " + file.getName() + ". Error: " + e.getMessage());
        }
        return null;
    }



}
