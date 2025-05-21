package dev.gether.getcustomitem.listener.bbreak;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemBlockBreakEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.customize.DynamicAreaPickaxeItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DynamicAreaPickaxeListener extends AbstractCustomItemListener<DynamicAreaPickaxeItem> {


    public DynamicAreaPickaxeListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemBlockBreakEvent(CustomItemBlockBreakEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof DynamicAreaPickaxeItem dynamicAreaPickaxeItem)) return;
        if(!dynamicAreaPickaxeItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, dynamicAreaPickaxeItem, event.getItemStack(), event.getEquipmentSlot())) return;

        breakBlocksInArea(event.getBlock(), player, dynamicAreaPickaxeItem);

        dynamicAreaPickaxeItem.notifyYourself(player);
        dynamicAreaPickaxeItem.playSound(player.getLocation());
    }

    @Override
    protected boolean canUseItem(Player player, DynamicAreaPickaxeItem item, ItemStack itemStack, EquipmentSlot equipmentSlot) {
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

    private void breakBlocksInArea(Block centerBlock, Player player, DynamicAreaPickaxeItem pickaxe) {
        BlockFace blockFace = getBlockFace(player);
        int width = pickaxe.getMiningWidth();
        int height = pickaxe.getMiningHeight();
        int depth = pickaxe.getMiningDepth();

        for (int x = -(width / 2); x <= width / 2; x++) {
            for (int y = -(height / 2); y <= height / 2; y++) {
                for (int z = 0; z < depth; z++) {
                    if (blockFace == null) {
                        if (canBreak(centerBlock, player)) {
                            centerBlock.breakNaturally(player.getInventory().getItemInMainHand());
                        }
                        return;
                    }
                    Block relativeBlock = getRelativeBlock(centerBlock, blockFace, x, y, z);
                    if (canBreak(relativeBlock, player)) {
                        relativeBlock.breakNaturally(player.getInventory().getItemInMainHand());
                    }
                }
            }
        }
    }

    private Block getRelativeBlock(Block centerBlock, BlockFace blockFace, int x, int y, int z) {
        switch (blockFace) {
            case NORTH:
                return centerBlock.getRelative(x, y, z);
            case SOUTH:
                return centerBlock.getRelative(-x, y, -z);
            case EAST:
                return centerBlock.getRelative(-z, y, x);
            case WEST:
                return centerBlock.getRelative(z, y, -x);
            case UP:
                return centerBlock.getRelative(x, -z, y);
            case DOWN:
                return centerBlock.getRelative(x, z, -y);
            default:
                return centerBlock;
        }
    }

    private BlockFace getBlockFace(Player player) {
        List<Block> lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 5);
        if (lastTwoTargetBlocks.size() != 2 || !lastTwoTargetBlocks.get(1).getType().isOccluding()) return null;
        Block targetBlock = lastTwoTargetBlocks.get(1);
        Block adjacentBlock = lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    private boolean canBreak(Block block, Player player) {
        if (WorldGuardUtil.isDeniedFlag(block.getLocation(), player, Flags.BLOCK_BREAK)) {
            return false;
        }
        return !block.getType().equals(Material.BEDROCK) && !block.getType().equals(Material.AIR);
    }
}
