package dev.gether.getcustomitem.cmd.arg;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.region.CustomRegion;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;

import java.util.Optional;

public class CustomRegionArg extends ArgumentResolver<CommandSender, CustomRegion> {

    private final GetCustomItem plugin;

    public CustomRegionArg(GetCustomItem plugin) {
        this.plugin = plugin;
    }

    @Override
    protected ParseResult<CustomRegion> parse(Invocation<CommandSender> invocation, Argument<CustomRegion> context, String argument) {
        Optional<CustomRegion> region = plugin.getFileManager().getRegionsConfig().getRegions()
                .stream()
                .filter(r -> r.getName().equalsIgnoreCase(argument))
                .findFirst();

        return region.map(ParseResult::success).orElseGet(() -> ParseResult.failure("Region not found!"));
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<CustomRegion> argument, SuggestionContext context) {
        return plugin.getFileManager().getRegionsConfig().getRegions()
                .stream()
                .map(CustomRegion::getName)
                .collect(SuggestionResult.collector());
    }
}