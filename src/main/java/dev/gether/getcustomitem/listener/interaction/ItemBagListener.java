package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ItemsBag;
import dev.gether.getcustomitem.item.manager.itembag.ItemBagInventoryHolder;
import dev.gether.getcustomitem.item.manager.itembag.ItemBagManager;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemBagListener extends AbstractCustomItemListener<ItemsBag> {

    private final ItemManager itemManager;
    private final ItemBagManager itemBagManager;
    private final RegionManager regionManager;

    private final Map<UUID, LastHitInfo> lastHitMap = new ConcurrentHashMap<>();

    private static final long HIT_EXPIRY_TIME = 30000; // 30 sec

    public ItemBagListener(ItemManager itemManager,
                           CooldownManager cooldownManager,
                           FileManager fileManager,
                           ItemBagManager itemBagManager,
                           RegionManager regionManager) {
        super(itemManager, cooldownManager, fileManager);
        this.itemManager = itemManager;
        this.itemBagManager = itemBagManager;
        this.regionManager = regionManager;
    }

    @Getter
    private static class LastHitInfo {
        private final UUID attackerUUID;
        private final long timestamp;

        public LastHitInfo(UUID attackerUUID) {
            this.attackerUUID = attackerUUID;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isValid() {
            return System.currentTimeMillis() - timestamp < HIT_EXPIRY_TIME;
        }
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ItemsBag itemsBag)) return;
        if (!itemsBag.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, itemsBag, event.getItemStack(), event.getEquipmentSlot())) return;

        int amount = event.getItemStack().getAmount();
        if (amount > 1) {
            event.getItemStack().setAmount(amount - 1);

            ItemStack newItem = event.getItemStack().clone();
            newItem.setAmount(1);
            ItemMeta meta = newItem.getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(getRandomCustomModelData());
                newItem.setItemMeta(meta);
            }
            PlayerUtil.addItems(player, newItem);
        }
        // play sound
        itemsBag.playSound(player.getLocation());

        itemBagManager.openBackpack(player, event.getItemStack(), itemsBag);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        lastHitMap.put(victim.getUniqueId(), new LastHitInfo(attacker.getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        lastHitMap.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        InventoryView openInventory = player.getOpenInventory();
        if (openInventory.getTopInventory().getHolder() instanceof ItemBagInventoryHolder itemBagInventoryHolder) {
            Item itemDrop = event.getItemDrop();
            ItemStack itemStack = itemDrop.getItemStack();

            Optional<String> itemID = itemManager.findItemID(itemStack);
            if(itemID.isEmpty()) return;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemID.get());
            if(customItemByKey.isEmpty()) return;

            if(!(customItemByKey.get() instanceof ItemsBag)) return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof ItemBagInventoryHolder) {
            for (Integer rawSlot : event.getRawSlots()) {
                if(rawSlot >= 0 && rawSlot < inventory.getSize()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) {
            LastHitInfo lastHitInfo = lastHitMap.get(victim.getUniqueId());

            if (lastHitInfo != null && lastHitInfo.isValid()) {
                UUID attackerUUID = lastHitInfo.getAttackerUUID();
                Player attacker = victim.getServer().getPlayer(attackerUUID);

                if (attacker != null && attacker.isOnline()) {
                    killer = attacker;
                }
            }

            lastHitMap.remove(victim.getUniqueId());
        }

        if (killer == null) {
            return;
        }

        List<ItemStack> backpacks = findBackpacksInInventory(killer.getInventory());
        if (backpacks.isEmpty()) {
            return;
        }

        List<ItemStack> itemsToAdd = new ArrayList<>(event.getDrops());
        List<ItemStack> itemsNotAdded = new ArrayList<>(itemsToAdd);

        for (ItemStack backpackItem : backpacks) {
            Optional<String> itemID = itemManager.findItemID(backpackItem);
            if(itemID.isEmpty()) continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemID.get());
            if(customItemByKey.isEmpty()) continue;

            if(!(customItemByKey.get() instanceof ItemsBag itemsBag)) continue;

            if (!regionManager.canUseItem(itemsBag, victim.getLocation())) {
                return;
            }

            UUID backpackUUID = itemsBag.getBackpackUUID(backpackItem);
            if (backpackUUID == null) continue;

            itemsNotAdded = itemBagManager.addItemsToBackpack(backpackUUID, itemsNotAdded);

            if (itemsNotAdded.isEmpty()) {
                break;
            }
        }

        event.getDrops().clear();
        event.getDrops().addAll(itemsNotAdded);
    }

    private List<ItemStack> findBackpacksInInventory(PlayerInventory inventory) {
        List<ItemStack> backpacks = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            if(item == null) continue;
            Optional<String> itemID = itemManager.findItemID(item);
            if(itemID.isEmpty()) continue;

            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemID.get());
            if(customItemByKey.isEmpty()) continue;

            if(!(customItemByKey.get() instanceof ItemsBag itemsBag)) continue;

            backpacks.add(item);
        }
        return backpacks;
    }

    private int getRandomCustomModelData() {
        return GetCustomItem.getRandom().nextInt(1000000) + 1;
    }

}