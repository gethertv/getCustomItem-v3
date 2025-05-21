package dev.gether.getcustomitem.listener.bbreak;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemBlockBreakEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.DynamicAreaPickaxeItem;
import dev.gether.getcustomitem.item.customize.itemtier.ActionEvent;
import dev.gether.getcustomitem.item.customize.itemtier.ItemTier;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ItemTierListener extends AbstractCustomItemListener<ItemTier> {

    private final ItemManager itemManager;
    private final CooldownManager cooldownManager;
    private final FileManager fileManager;

    public ItemTierListener(ItemManager itemManager,
                            CooldownManager cooldownManager,
                            FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.itemManager = itemManager;
        this.cooldownManager = cooldownManager;
        this.fileManager = fileManager;
    }

    @EventHandler
    public void onCustomItemBlockBreakEvent(CustomItemBlockBreakEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof ItemTier itemTier)) return;
        if(!itemTier.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, itemTier, event.getItemStack(), event.getEquipmentSlot())) return;

        Block block = event.getBlock();
        handlePlayerAction(player, null, ActionEvent.BREAK_BLOCK, block.getType(), itemTier, event.getItemStack());
    }

    @Override
    protected boolean canUseItem(Player player, ItemTier item, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        double cooldownSeconds = cooldownManager.getCooldownSecond(player, item);
        if (cooldownSeconds <= 0 || player.hasPermission(item.getPermissionBypass())) {
            cooldownManager.setCooldown(player, item);

            int remainingUses = item.getRemainingUses(itemStack);
            if (remainingUses == 1) {
                Bukkit.getScheduler().runTask(GetCustomItem.getInstance(), () -> {
                    item.takeUsage(player, itemStack, equipmentSlot);
                });
            } else {
                item.takeUsage(player, itemStack, equipmentSlot);
            }
            return true;
        } else {
            if(item.isCooldownMessage())
                MessageUtil.sendMessage(player, fileManager.getLangConfig().getHasCooldown().replace("{time}", String.valueOf(cooldownSeconds)));

            return false;
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if(killer == null) return;

        if(killer == event.getEntity()) return;

        Optional<String> customItemID = itemManager.findItemID(killer.getInventory().getItemInMainHand());
        if (customItemID.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(customItemID.get());
        if (customItemByKey.isEmpty()) return;

        if(customItemByKey.get() instanceof ItemTier itemTier) {
            itemTier.getEquipmentSlots().forEach(equipmentSlot -> {
                ItemStack itemStack = killer.getInventory().getItem(equipmentSlot);

                handlePlayerAction(killer, entity, ActionEvent.KILL_ENTITY, entity.getType(), itemTier, itemStack);
            });
        }
    }

    public void handlePlayerAction(Player player, Entity entity, ActionEvent actionEvent, Object entityType, ItemTier itemTier, ItemStack itemStack) {
        if(itemStack == null) return;
        boolean status = itemTier.isItemTier(itemStack);
        if(!status)
            return;
        if(itemTier.isMaxLevel(itemStack))
            return;

        if (fileManager.getConfig().getCooldown().containsKey(entityType.toString())) {
            String playerUUID = player.getUniqueId().toString();
            String cooldownKey = playerUUID + ":" + entityType.toString();
            if(entityType == EntityType.PLAYER && entity != null){
                cooldownKey = playerUUID + ":"+ entity.getUniqueId();
            }

            if (cooldownManager.isOnCooldown(cooldownKey)) {
                long timeLeft = cooldownManager.getCooldownTime(cooldownKey);
                String formattedTime = formatTime(timeLeft);
                String cooldownMessage = fileManager.getConfig().getCooldownMessage()
                        .replace("{time}", formattedTime);
                MessageUtil.sendMessage(player, cooldownMessage);
                return;
            }
            // Set cooldown
            int cooldownSeconds = fileManager.getConfig().getCooldown().get(entityType.toString());
            cooldownManager.setCooldown(cooldownKey, cooldownSeconds);
        }

        itemTier.action(player, actionEvent, entityType, 1, itemStack);
    }

    private String formatTime(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}