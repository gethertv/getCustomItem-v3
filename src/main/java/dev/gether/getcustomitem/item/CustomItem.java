package dev.gether.getcustomitem.item;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getutils.GetConfig;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.TitleMessage;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.ColorFixer;
import dev.gether.getutils.utils.ItemUtil;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.PlayerUtil;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class CustomItem extends GetConfig {

    private static final String USAGE_SUFFIX = "_usage";

    @JsonIgnore
    private NamespacedKey usageKey;
    @JsonIgnore private NamespacedKey itemKey;

    private boolean enabled = true;
    private String itemID;
    private String categoryName;
    protected int usage;
    private Item item;
    private ItemType itemType;
    private int cooldown; // time in seconds
    private String permissionBypass;
    private SoundConfig soundConfig;
    private List<String> notifyYourself;
    private List<String> notifyOpponents;
    private TitleMessage titleYourself;
    private TitleMessage titleOpponents;
    private boolean visualCooldown = false;
    private boolean cooldownMessage = true;
    private boolean previewItem = false;

    @JsonIgnore
    private transient ItemStack itemStack;

    protected CustomItem(CustomItemBuilder<?, ?> builder) {
        this.itemID = builder.itemID;
        this.categoryName = builder.categoryName;
        this.enabled = builder.enabled;
        this.usage = builder.usage;
        this.item = builder.item;
        this.itemType = builder.itemType;
        this.cooldown = builder.cooldown;
        this.permissionBypass = builder.permissionBypass;
        this.soundConfig = builder.soundConfig;
        this.visualCooldown = builder.visualCooldown;
        this.cooldownMessage = builder.cooldownMessage;

        this.notifyYourself = builder.notifyYourself != null ? builder.notifyYourself : List.of(
                "&7",
                "#78ff69 × Example YOURSELF!",
                "&7"
        );
        this.notifyOpponents = builder.notifyOpponents != null ? builder.notifyOpponents : List.of(
                "&7",
                "#78ff69 × Example OPPONENTS!",
                "&7"
        );
        this.titleYourself = builder.titleYourself != null ? builder.titleYourself : new TitleMessage(false, "&aTitle Yourself", "&7Subtitle", 10, 50, 10);
        this.titleOpponents = builder.titleOpponents != null ? builder.titleOpponents : new TitleMessage(false, "&aTitle Opponents", "&7Subtitle", 10, 50, 10);
    }



    public void init() {
        this.usageKey = new NamespacedKey(GetCustomItem.getInstance(), itemID + USAGE_SUFFIX);
        this.itemKey = new NamespacedKey(GetCustomItem.getInstance(), itemID);
        itemStack = item.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            // set usage to persistent data
            itemMeta.getPersistentDataContainer().set(usageKey, PersistentDataType.INTEGER, usage);
            itemMeta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, itemID);
            itemMeta.getPersistentDataContainer().set(ItemManager.getItemKey(), PersistentDataType.STRING, itemID);

            List<String> lore = new ArrayList<>();
            if (itemMeta.hasLore())
                lore.addAll(Objects.requireNonNull(itemMeta.getLore()));

            // get replaced lore
            lore = getLore(lore, usage);

            itemMeta.setLore(ColorFixer.addColors(lore));
        }
        itemStack.setItemMeta(itemMeta);
    }

    public void playSound(Location location) {
        // check sound is enabled
        if(!soundConfig.isEnable())
            return;

        location.getWorld().playSound(location, soundConfig.getSound(), 1F, 1F);
    }

    @JsonIgnore
    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getUsage(ItemMeta itemMeta) {
        if (itemMeta == null)
            return 1;

        Integer value = itemMeta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
        return value != null ? value : 0;
    }

    @JsonIgnore
    public boolean isCustomItem(ItemStack itemStack) {
        if(itemStack == null) return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return false;

        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        return persistentDataContainer.has(itemKey, PersistentDataType.STRING);
    }

    @JsonIgnore
    public void maxRangeTask(Entity entity, double maxRange) {
        final Location startLocation = entity.getLocation();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (entity.isValid()) {
                    double distanceTraveled = entity.getLocation().distance(startLocation);
                    if(entity.isOnGround()) {
                        entity.remove();
                        this.cancel();
                    }
                    if (distanceTraveled >= maxRange) {
                        entity.remove();
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(GetCustomItem.getInstance(), 1L, 1L);
    }


    public void takeAmount(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        Integer usage = itemMeta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
        if (usage == null)
            return;

        // ignore verify usage value because in other case im verify the number of usage
        // and if the number is lower than 1 im just remove it
        itemMeta.getPersistentDataContainer().set(usageKey, PersistentDataType.INTEGER, usage - 1);
        itemStack.setItemMeta(itemMeta);

    }

    public void updateItem(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        Integer usage = itemMeta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
        if (usage == null)
            return;

        // default original item
        ItemStack originalItem = item.getItemStack().clone();
        ItemMeta originalMeta = originalItem.getItemMeta();

        if (originalMeta == null || !originalMeta.hasLore())
            return;

        List<String> lore = new ArrayList<>(Objects.requireNonNull(originalMeta.getLore()));
        lore = getLore(lore, usage);

        itemMeta.setLore(ColorFixer.addColors(lore));
        itemStack.setItemMeta(itemMeta);
    }

    protected List<String> getLore(List<String> lore, int usage) {
        Map<String, String> values = new HashMap<>(replacementValues());
        values.put("{usage}", usage == -1 ? GetCustomItem.getInstance().getFileManager().getLangConfig().getNoLimit() : String.valueOf(usage));

        return new ArrayList<>(lore.stream()
                .map(line -> {
                    for (Map.Entry<String, String> entry : values.entrySet()) {
                        line = line.replace(entry.getKey(), entry.getValue());
                    }
                    return line;
                })
                .toList());
    }

    public int getRemainingUses(ItemStack itemStack) {
        if (itemStack == null || !isCustomItem(itemStack)) {
            return 0;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return 0;
        }

        Integer usage = itemMeta.getPersistentDataContainer().get(usageKey, PersistentDataType.INTEGER);
        if (usage == null) {
            return 0;
        }

        if (usage == -1) {
            return Integer.MAX_VALUE;
        }

        return usage;
    }

    public void takeUsage(Player player, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        int usage = getUsage(itemStack.getItemMeta());
        if(usage == -1)
            return;

        int amount = itemStack.getAmount();
        // check if items is stacked
        ItemStack remainingItem = null;
        if(amount > 1) {
            remainingItem = itemStack.clone();
            remainingItem.setAmount(amount - 1);

            itemStack.setAmount(1); // set original item amount to one
        }
        if(usage == 1) {
            if(equipmentSlot == EquipmentSlot.OFF_HAND) {
                player.getInventory().setItemInOffHand(null);
            }
            else {
                player.getInventory().setItem(equipmentSlot, null);
            }
        } else {
            takeAmount(itemStack);
            updateItem(itemStack);
        }

        // give remaining item after the update USAGE in main item, because if
        // I'll give faster than update, they again will be stacked
        if(remainingItem != null)
            PlayerUtil.addItems(player, remainingItem); // give other item to inv
    }

    public void takeUsage(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if(ItemUtil.sameItem(mainHand, getItemStack())) {
            takeUsage(player, mainHand, EquipmentSlot.HAND);
        } else {
            takeUsage(player, player.getInventory().getItemInOffHand(), EquipmentSlot.OFF_HAND);
        }
    }

    public void notifyYourself(Player player) {
        // send title
        titleYourself.sendTo(player);

        if(notifyYourself.isEmpty())
            return;

        MessageUtil.sendMessage(player, String.join("\n", notifyYourself));
    }

    public void notifyOpponents(Player player) {
        // send title
        titleOpponents.sendTo(player);

        if(notifyOpponents.isEmpty())
            return;

        MessageUtil.sendMessage(player, String.join("\n", notifyOpponents));
    }

    protected abstract Map<String, String> replacementValues();


    @JsonIgnore
    public void apply(ItemStack itemInMainHand) {
        ItemMeta itemMeta = itemInMainHand.getItemMeta();
        if (itemMeta != null) {
            // set usage to persistent data
            itemMeta.getPersistentDataContainer().set(usageKey, PersistentDataType.INTEGER, usage);
            itemMeta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, itemID);
            itemMeta.getPersistentDataContainer().set(ItemManager.getItemKey(), PersistentDataType.STRING, itemID);
        }
        itemInMainHand.setItemMeta(itemMeta);
    }
}
