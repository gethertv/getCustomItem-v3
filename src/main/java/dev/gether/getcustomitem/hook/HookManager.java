package dev.gether.getcustomitem.hook;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.hook.impl.EssentialsXHook;
import dev.gether.getechantingitem.EnchantingItem;
import dev.gether.gettempblock.GetTempBlock;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Getter
public class HookManager {

    private EssentialsXHook essentialsXHook;
    private EnchantingItem enchantingItem;
    private GetTempBlock getTempBlock;

    public HookManager(GetCustomItem plugin) {
        Plugin enchantingItem =  plugin.getServer().getPluginManager().getPlugin("enchantingItem");
        if (enchantingItem != null) {
            this.enchantingItem = (EnchantingItem) enchantingItem;
            MessageUtil.logMessage(ConsoleColor.GREEN, "[getCustomItem-v3] Successfully hooked into EnchantingItem");
        }
        Plugin getTempBlock =  plugin.getServer().getPluginManager().getPlugin("getTempBlock");
        if (getTempBlock != null) {
            this.getTempBlock = (GetTempBlock) getTempBlock;
            MessageUtil.logMessage(ConsoleColor.GREEN, "[getCustomItem-v3] Successfully hooked into GetTempBlock");
        }
        essentialsXHook = new EssentialsXHook(plugin);
    }

    public boolean destroyBlock(Player player, Location location) {
        if(getTempBlock != null) {
            if (!getTempBlock.getBlockCleanManager().containsBlock(location)) {
                if (!player.hasPermission("gettempblock.admin"))
                    return true;

                location.getBlock().setType(Material.AIR);
                return true;
            }
            location.getBlock().setType(Material.AIR);
            getTempBlock.getBlockCleanManager().destoryBlock(location);
            return true;
        }
        return false;
    }

    public void placeBlock(Location location) {
        if(getTempBlock != null) {
            getTempBlock.getBlockCleanManager().placeBlock(location);
        }
    }
    public boolean isCobwebThrow(Location location) {
        if(getTempBlock != null) {
            return getTempBlock.getBlockCleanManager().containsBlock(location);
        }
        return false;
    }
}
