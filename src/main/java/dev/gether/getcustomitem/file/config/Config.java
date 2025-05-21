package dev.gether.getcustomitem.file.config;

import dev.gether.getutils.GetConfig;
import dev.gether.getutils.annotation.Comment;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.inventory.DynamicItem;
import dev.gether.getutils.models.inventory.InventoryConfig;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Config extends GetConfig {

    @Comment({
            "",
            "author - https://www.spigotmc.org/resources/authors/gethertv.571046/",
            "discord: https://dc.gether.dev",
            "",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html",
            "https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html",
            ""
    })
    private String discord = "https://dc.gether.dev";

    private boolean removeTotemEffect = true;

    private double knockbackX = 0.1;
    private double knockbackZ = 0.1;

    private boolean debug = false;
    private boolean disableAnvilCustomItem = true;

    private boolean defaultItems = true;

    private Location spawnLocation = null;

    private String cooldownMessage = "&cMusisz odczekac {time}";
    private Map<Object, Integer> cooldown = new HashMap<>(Map.of(
            EntityType.PLAYER, 15
    ));

    private int[] previewSlots = new int[]{10,11,12,13,14,15,16,17,18,19,20};

    private InventoryConfig inventory = InventoryConfig.builder()
            .title("&0Eventowki")
            .size(54)
            .cancelClicks(true)
            .decorations(new ArrayList<>(List.of(
                    DynamicItem.builder()
                            .item(Item.builder()
                                    .material(Material.BLACK_STAINED_GLASS_PANE)
                                    .name("&7")
                                    .lore(new ArrayList<>(List.of()))
                                    .build())
                            .slots(new ArrayList<>(List.of(
                                    0,1,2,3,4,5,6,7,8
                            )))
                            .build()
            )))
            .build();
}
