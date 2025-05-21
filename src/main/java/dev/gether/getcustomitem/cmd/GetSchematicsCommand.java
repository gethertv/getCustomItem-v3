package dev.gether.getcustomitem.cmd;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.schematic.Schematic;
import dev.gether.getcustomitem.schematic.SchematicManager;
import dev.gether.getcustomitem.schematic.SchematicPasteOptions;
import dev.gether.getutils.selector.RegionSelection;
import dev.gether.getutils.utils.MessageUtil;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Command(name = "getschematic", aliases = "gs")
@Permission("getschematic.admin")
public class GetSchematicsCommand {

    private final GetCustomItem plugin;
    private final SchematicManager schematicManager;

    public GetSchematicsCommand(GetCustomItem plugin, SchematicManager schematicManager) {
        this.plugin = plugin;
        this.schematicManager = schematicManager;
    }

    @Execute(name = "create")
    public void createSchematic(@Context Player player, @Arg("name") String name) {
        if (schematicManager.schematicExists(name)) {
            MessageUtil.sendMessage(player, "&cSchematic with name &e" + name + " &calready exists!");
            return;
        }

        RegionSelection selection = plugin.getSelectorManager().getSelection(player);
        if (selection == null || !selection.isComplete()) {
            MessageUtil.sendMessage(player, "&cYou do not have a selection! Use /getschematic selector to select an area.");
            return;
        }

        schematicManager.createSchematic(player, name, selection);
    }

    @Execute(name = "delete")
    public void deleteSchematic(@Context CommandSender sender, @Arg("name") String name) {
        if (!schematicManager.schematicExists(name)) {
            MessageUtil.sendMessage(sender, "&cSchematic with name &e" + name + " &cdoesn't exist!");
            return;
        }

        if (schematicManager.deleteSchematic(name)) {
            MessageUtil.sendMessage(sender, "&aSuccessfully deleted schematic &e" + name);
        } else {
            MessageUtil.sendMessage(sender, "&cFailed to delete schematic &e" + name);
        }
    }

    @Execute(name = "paste")
    public void pasteSchematic(@Context Player player, @Arg("name") String name) {
        if (!schematicManager.schematicExists(name)) {
            MessageUtil.sendMessage(player, "&cSchematic with name &e" + name + " &cdoesn't exist!");
            return;
        }

        Location location = player.getLocation();
        SchematicPasteOptions options = SchematicPasteOptions.defaults()
                .setPasteGradually(true)
                .setBlocksPerTick(20);

        List<Location> blockLocations = new ArrayList<>();
        schematicManager.pasteSchematic(location, name, options, blockLocations);
        MessageUtil.sendMessage(player, "&aPasting schematic &e" + name + " &aat your location");
    }


    @Execute(name = "paste gradual")
    public void pasteSchematicGradual(@Context Player player, @Arg("name") String name, @Arg("blocksPerTick") int blocksPerTick) {
        if (!schematicManager.schematicExists(name)) {
            MessageUtil.sendMessage(player, "&cSchematic with name &e" + name + " &cdoesn't exist!");
            return;
        }

        Location location = player.getLocation();
        SchematicPasteOptions options = SchematicPasteOptions.defaults()
                .setPasteGradually(true)
                .setBlocksPerTick(blocksPerTick);

        List<Location> blockLocations = new ArrayList<>();
        schematicManager.pasteSchematic(location, name, options, blockLocations);
        MessageUtil.sendMessage(player, "&aPasting schematic &e" + name + " &aat your location with &e" + blocksPerTick + " &ablocks per tick");
    }

    @Execute(name = "list")
    public void listSchematics(@Context CommandSender sender) {
        List<String> schematics = schematicManager.getSchematicNames();

        if (schematics.isEmpty()) {
            MessageUtil.sendMessage(sender, "&cNo schematics found!");
            return;
        }

        MessageUtil.sendMessage(sender, "&6Available schematics:");
        for (String schematicName : schematics) {
            MessageUtil.sendMessage(sender, "&7- &e" + schematicName);
        }
    }

    @Execute(name = "info")
    public void getSchematicInfo(@Context CommandSender sender, @Arg("name") String name) {
        if (!schematicManager.schematicExists(name)) {
            MessageUtil.sendMessage(sender, "&cSchematic with name &e" + name + " &cdoesn't exist!");
            return;
        }

        Optional<Schematic> schematicOpt = schematicManager.getSchematic(name);
        if (schematicOpt.isEmpty()) {
            MessageUtil.sendMessage(sender, "&cFailed to load schematic information for &e" + name);
            return;
        }

        Schematic schematic = schematicOpt.get();

        MessageUtil.sendMessage(sender, "&6Schematic information for &e" + name + "&6:");
        MessageUtil.sendMessage(sender, "&7Block count: &f" + schematic.getBlocks().size());
    }


    @Execute
    public void defaultCommand(@Context CommandSender sender) {
        MessageUtil.sendMessage(sender, "&6GetSchematic Commands:");
        MessageUtil.sendMessage(sender, "&e/gs create <name> &7- Create a new schematic");
        MessageUtil.sendMessage(sender, "&e/gs delete <name> &7- Delete a schematic");
        MessageUtil.sendMessage(sender, "&e/gs paste <name> &7- Paste a schematic at your location");
        MessageUtil.sendMessage(sender, "&e/gs paste gradual <name> <blocksPerTick> &7- Paste a schematic gradually");
        MessageUtil.sendMessage(sender, "&e/gs list &7- List all schematics");
        MessageUtil.sendMessage(sender, "&e/gs info <name> &7- Get information about a schematic");
        MessageUtil.sendMessage(sender, "&e/gs reload &7- Reload all schematics");
        MessageUtil.sendMessage(sender, "&e/gs selector &7- Get the region selector tool");
    }
}