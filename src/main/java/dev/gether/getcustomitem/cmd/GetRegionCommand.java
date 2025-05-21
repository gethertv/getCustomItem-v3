package dev.gether.getcustomitem.cmd;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.region.CustomRegion;
import dev.gether.getutils.selector.RegionSelection;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.PlayerUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

@Command(name = "getregion", aliases = "gr")
@Permission("getregion.admin")
public class GetRegionCommand {

    private final GetCustomItem plugin;

    public GetRegionCommand(GetCustomItem plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "create")
    public void createRegion(@Context Player player, @Arg("name") String name) {
        if (plugin.getFileManager().getRegionsConfig().getRegions().stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(name))) {
            MessageUtil.sendMessage(player, "&cRegion with name &e" + name + " &calready exists!");
            return;
        }

        RegionSelection selection = plugin.getSelectorManager().getSelection(player);
        if (selection == null || !selection.isComplete()) {
            MessageUtil.sendMessage(player, "&cYou do not have a selection! /getregion selector");
            return;
        }
        CustomRegion region = new CustomRegion(1, name, selection.firstPoint(), selection.secondPoint(), new HashSet<>(), new HashSet<>());
        plugin.getFileManager().getRegionsConfig().getRegions().add(region);
        plugin.getFileManager().getRegionsConfig().save();

        MessageUtil.sendMessage(player, "&aSuccessfully created region &e" + name);
    }

    @Execute(name = "selector")
    public void giveSelector(@Context Player player) {
        PlayerUtil.addItems(player, plugin.getSelectorManager().getSelectorItem().clone());
        MessageUtil.sendMessage(player, "&aYou received the region selector!");
    }

    @Execute(name = "delete")
    public void deleteRegion(@Context CommandSender sender, @Arg("region") CustomRegion region) {
        plugin.getFileManager().getRegionsConfig().getRegions().remove(region);
        plugin.getFileManager().getRegionsConfig().save();
        MessageUtil.sendMessage(sender, "&aSuccessfully deleted region &e" + region.getName());
    }

    @Execute(name = "priority")
    public void setPriority(@Context CommandSender sender, @Arg("region") CustomRegion region, @Arg("priority") int priority) {
        region.setPriority(priority);
        plugin.getFileManager().getRegionsConfig().save();
        MessageUtil.sendMessage(sender, "&aSet priority of region &e" + region.getName() + " &ato &e" + priority);
    }

    @Execute(name = "disabled add")
    public void addItem(@Context CommandSender sender, @Arg("region") CustomRegion region, @Arg("item") CustomItem item) {
        if (item.getItemID().equals("ALL")) {
            region.getDisabledItems().add("ALL");
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aDisabled &eALL &aitems in region &e" + region.getName());
        } else {
            region.getDisabledItems().add(item.getItemID());
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aAdded item &e" + item.getItemID() + " &ato region &e" + region.getName());
        }
    }

    @Execute(name = "disabled remove")
    public void removeItem(@Context CommandSender sender, @Arg("region") CustomRegion region, @Arg("item") CustomItem item) {
        if (item.getItemID().equals("ALL")) {
            region.getDisabledItems().remove("ALL");
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aEnabled &eALL &aitems in region &e" + region.getName());
        } else if (region.getDisabledItems().remove(item.getItemID())) {
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aRemoved item &e" + item.getItemID() + " &afrom region &e" + region.getName());
        } else {
            MessageUtil.sendMessage(sender, "&cItem &e" + item.getItemID() + " &cwas not disabled in region &e" + region.getName());
        }
    }

    @Execute(name = "allowed add")
    public void addAllowedItem(@Context CommandSender sender, @Arg("region") CustomRegion region, @Arg("item") CustomItem item) {
        if (item.getItemID().equals("ALL")) {
            region.getAllowedItems().add("ALL");
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aAllowed &eALL &aitems in region &e" + region.getName());
        } else {
            region.getAllowedItems().add(item.getItemID());
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aAllowed item &e" + item.getItemID() + " &ain region &e" + region.getName());
        }
    }

    @Execute(name = "allowed remove")
    public void removeAllowedItem(@Context CommandSender sender, @Arg("region") CustomRegion region, @Arg("item") CustomItem item) {
        if (item.getItemID().equals("ALL")) {
            region.getAllowedItems().remove("ALL");
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aRemoved &eALL &afrom allowed items in region &e" + region.getName());
        } else if (region.getAllowedItems().remove(item.getItemID())) {
            plugin.getFileManager().getRegionsConfig().save();
            MessageUtil.sendMessage(sender, "&aRemoved item &e" + item.getItemID() + " &afrom allowed items in region &e" + region.getName());
        } else {
            MessageUtil.sendMessage(sender, "&cItem &e" + item.getItemID() + " &cwas not allowed in region &e" + region.getName());
        }
    }

    @Execute(name = "info")
    public void getInfo(@Context Player player) {
        Location loc = player.getLocation();
        List<CustomRegion> regions = plugin.getFileManager().getRegionsConfig().getRegions().stream()
                .filter(region -> plugin.getRegionManager().isInRegion(loc, region))
                .sorted((r1, r2) -> Integer.compare(r2.getPriority(), r1.getPriority()))
                .toList();

        if (regions.isEmpty()) {
            MessageUtil.sendMessage(player, "&cYou are not in any region!");
            return;
        }

        MessageUtil.sendMessage(player, "&6You are in following regions:");
        for (CustomRegion region : regions) {
            MessageUtil.sendMessage(player, "&7- &e" + region.getName() + " &7(Priority: &f" + region.getPriority() + "&7)");
            if (!region.getDisabledItems().isEmpty()) {
                MessageUtil.sendMessage(player, "  &7Disabled items:");
                for (String itemId : region.getDisabledItems()) {
                    MessageUtil.sendMessage(player, "  &7- &c" + itemId);
                }
            }
            if (!region.getAllowedItems().isEmpty()) {
                MessageUtil.sendMessage(player, "  &7Allowed items:");
                for (String itemId : region.getAllowedItems()) {
                    MessageUtil.sendMessage(player, "  &7- &a" + itemId);
                }
            }
        }
    }

    @Execute(name = "info")
    public void getRegionInfo(@Context CommandSender sender, @Arg("region") CustomRegion region) {
        MessageUtil.sendMessage(sender, "&6Region information for &e" + region.getName() + "&6:");
        MessageUtil.sendMessage(sender, "&7Priority: &f" + region.getPriority());
        MessageUtil.sendMessage(sender, "&7Position 1: &f" + formatLocation(region.getPos1()));
        MessageUtil.sendMessage(sender, "&7Position 2: &f" + formatLocation(region.getPos2()));

        if (region.getDisabledItems().isEmpty()) {
            MessageUtil.sendMessage(sender, "&7No disabled items");
        } else {
            MessageUtil.sendMessage(sender, "&7Disabled items:");
            for (String itemId : region.getDisabledItems()) {
                MessageUtil.sendMessage(sender, "&7- &c" + itemId);
            }
        }

        if (region.getAllowedItems().isEmpty()) {
            MessageUtil.sendMessage(sender, "&7No allowed items");
        } else {
            MessageUtil.sendMessage(sender, "&7Allowed items:");
            for (String itemId : region.getAllowedItems()) {
                MessageUtil.sendMessage(sender, "&7- &a" + itemId);
            }
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f (%s)", loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
}
