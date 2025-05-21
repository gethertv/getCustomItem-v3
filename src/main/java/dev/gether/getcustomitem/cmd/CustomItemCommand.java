package dev.gether.getcustomitem.cmd;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.inv.PreviewInventoryHolder;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.region.CustomRegion;
import dev.gether.getutils.models.Cuboid;
import dev.gether.getutils.selector.RegionSelection;
import dev.gether.getutils.utils.MessageUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Command(name = "getcustomitem", aliases = {"getitem", "citem"})
@Permission("getcustomitem.admin")
public class CustomItemCommand {

    private final GetCustomItem plugin;

    public CustomItemCommand(GetCustomItem plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "give")
    public void giveItem(@Context CommandSender commandSender, @Arg("nickname") Player target, @Arg("name_item") CustomItem customItem, @Arg("amount") int amount) {
        if(customItem.getItem().getName().equalsIgnoreCase("ALL")) {
            MessageUtil.sendMessage(commandSender, "Not found item!");
            return;
        }
        ItemStack itemStack = customItem.getItemStack().clone(); // clone item to change amount
        itemStack.setAmount(amount); // set new amount
        if(customItem.getItemType() == ItemType.INFINITY_FIREWORK) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setCustomModelData(GetCustomItem.getRandom().nextInt(9999999));
            itemStack.setItemMeta(itemMeta);
        }

        target.getInventory().addItem(itemStack); // add item to the player (target)
        MessageUtil.sendMessage(commandSender, "&aSuccessful give the item to player!");

    }

    @Execute(name = "seteffect")
    public void giveItem(@Context Player player, @Arg("name_item") CustomItem customItem) {
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if(itemInMainHand.getType() == Material.AIR) {
            MessageUtil.sendMessage(player, "&cYou are not holding an air!");
            return;
        }
        customItem.apply(itemInMainHand);
        MessageUtil.sendMessage(player, "&aSuccessful set the effect!");

    }

    @Execute(name = "setspawn")
    public void spawnLocation(@Context Player player) {
        plugin.getFileManager().getConfig().setSpawnLocation(player.getLocation());
        plugin.getFileManager().getConfig().save();

        MessageUtil.sendMessage(player, "&aSuccessful spawn location set!");
    }

    @Execute(name = "preview")
    public void inventory(@Context CommandSender sender, @Arg Player target) {
        PreviewInventoryHolder previewInventoryHolder = new PreviewInventoryHolder(plugin, target, plugin.getFileManager().getConfig().getInventory());
        previewInventoryHolder.open();
    }

    @Execute(name = "cooldown reset")
    public void debugeMode(@Context CommandSender sender, @Arg Player target) {
        plugin.getCooldownManager().clearAllCache(target);
        MessageUtil.sendMessage(target, "&aSuccessful cooldown reset!");
    }


    @Execute(name = "debug")
    public void debugeMode(@Context CommandSender sender, @Arg("debug") boolean debug) {
        plugin.getFileManager().getConfig().setDebug(debug);
        MessageUtil.sendMessage(sender, "&aSuccessful debugging set "+debug);
    }

    @Execute(name = "reload")
    public void reloadConfig(@Context CommandSender commandSender) {
        plugin.getFileManager().reload();
        plugin.getSchematicManager().reload();
        MessageUtil.sendMessage(commandSender, "#40ff76Successfully reloaded config!");

    }


    @Execute(name = "attributes list")
    public void listAttributes(@Context CommandSender sender, @Arg("player") Player target) {
        MessageUtil.sendMessage(sender, "&6Attributes for " + target.getName() + ":");
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = target.getAttribute(attribute);
            if (instance == null) continue;

            if (!instance.getModifiers().isEmpty()) {
                MessageUtil.sendMessage(sender, "&7" + attribute.name() + ":");
                for (AttributeModifier modifier : instance.getModifiers()) {
                    MessageUtil.sendMessage(sender, "  &7• &f" + modifier.getName() +
                            " &7(&f" + String.format("%.3f", modifier.getAmount()) + "&7)");
                }
            }
        }
    }

    @Execute(name = "attributes clear")
    public void clearAttributes(@Context CommandSender sender, @Arg("player") Player target,
                                @Arg("item-id") String identifier) {
        int removed = 0;

        UUID buffUUID = UUID.nameUUIDFromBytes((identifier).getBytes());
        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = target.getAttribute(attribute);
            if (instance == null) continue;

            for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
                if(modifier.getUniqueId().equals(buffUUID)) {
                    instance.removeModifier(modifier);
                    removed++;
                }
            }
        }

        MessageUtil.sendMessage(sender, "&aSuccessfully removed " + removed + " attributes from player " + target.getName() + "!");
    }
}
