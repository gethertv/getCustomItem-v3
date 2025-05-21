package dev.gether.getcustomitem.listener;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractCustomItemListener<T extends CustomItem> implements Listener {

    protected final ItemManager itemManager;
    protected final CooldownManager cooldownManager;
    protected final FileManager fileManager;

    protected AbstractCustomItemListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        this.itemManager = itemManager;
        this.cooldownManager = cooldownManager;
        this.fileManager = fileManager;
    }


    protected void blockPlayer(Player player, int seconds) {
        long endTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldownManager.getBlockUseItem().put(player.getUniqueId(), endTime);
    }

    protected boolean canUseItem(Player player, T item, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        Long blockedUntil = cooldownManager.getBlockUseItem().get(player.getUniqueId());
        if (blockedUntil != null && blockedUntil > System.currentTimeMillis()) {
            long remainingTime = blockedUntil - System.currentTimeMillis();
            long remainingSeconds = remainingTime / 1000;
            String formattedTime = TimeUtil.formatTimeShort(remainingSeconds);

            MessageUtil.sendMessage(player, fileManager.getLangConfig().getCannotUseSpecialItem()
                    .replace("{time}", formattedTime));
            return false;
        } else if (blockedUntil != null) {
            cooldownManager.getBlockUseItem().remove(player.getUniqueId());
        }

        double cooldownSeconds = cooldownManager.getCooldownSecond(player, item);
        if (cooldownSeconds <= 0 || player.hasPermission(item.getPermissionBypass())) {
            cooldownManager.setCooldown(player, item);
            item.takeUsage(player, itemStack, equipmentSlot);
            return true;
        } else {
            if (item.isCooldownMessage()) {
                String formattedTime = TimeUtil.formatTimeShort((long) cooldownSeconds);
                MessageUtil.sendMessage(player, fileManager.getLangConfig().getHasCooldown()
                        .replace("{time}", formattedTime));
            }
            return false;
        }
    }

}