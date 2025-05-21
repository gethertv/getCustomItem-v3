package dev.gether.getcustomitem.listener.resurrect;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.resurrect.ResurrectGearItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

public class ResurrectGearListener extends AbstractCustomItemListener<ResurrectGearItem> {

    private final FileManager fileManager;
    private final ItemManager itemManager;

    public ResurrectGearListener(ItemManager itemManager,
                                 CooldownManager cooldownManager,
                                 FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.fileManager = fileManager;
        this.itemManager = itemManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage > 0) return;

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = getItemInSlot(equipment, slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            Optional<String> itemID = itemManager.findItemID(itemStack);
            if (itemID.isEmpty()) continue;

            Optional<CustomItem> customItemOpt = itemManager.findCustomItemByKey(itemID.get());
            if (customItemOpt.isEmpty()) continue;

            if (!(customItemOpt.get() instanceof ResurrectGearItem resurrectGearItem)) continue;
            if (!resurrectGearItem.isEnabled()) continue;

            if (!resurrectGearItem.getEquipmentSlots().isEmpty() &&
                    !resurrectGearItem.getEquipmentSlots().contains(slot)) continue;

            if (!canUseItem(player, resurrectGearItem, itemStack, slot)) continue;

            event.setCancelled(true);

            resurrectGearItem.notifyYourself(player);

            processDeathWithResurrectGear(player, resurrectGearItem);

            Location spawnLocation = fileManager.getConfig().getSpawnLocation();
            if (spawnLocation != null) {
                Bukkit.getScheduler().runTask(GetCustomItem.getInstance(), () -> {
                    player.teleport(spawnLocation);
                    double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    player.setHealth(maxHealth);
                    player.setFoodLevel(20);
                    player.setFireTicks(0);
                });
            } else {
                MessageUtil.sendMessage(player, "&c[getCustomItem] ResurrectGear not found spawn location. /getcustomitem setspawn");
            }
            break;
        }
    }

    private ItemStack getItemInSlot(EntityEquipment equipment, EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> equipment.getHelmet();
            case CHEST -> equipment.getChestplate();
            case LEGS -> equipment.getLeggings();
            case FEET -> equipment.getBoots();
            case HAND -> equipment.getItemInMainHand();
            case OFF_HAND -> equipment.getItemInOffHand();
            default -> null;
        };
    }

    private void processDeathWithResurrectGear(Player player, ResurrectGearItem resurrectGearItem) {
        PlayerInventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            double chance = GetCustomItem.getRandom().nextDouble() * 100;
            if (chance < resurrectGearItem.getChanceLostItem()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                inventory.setItem(i, null);
            }
        }
    }
}