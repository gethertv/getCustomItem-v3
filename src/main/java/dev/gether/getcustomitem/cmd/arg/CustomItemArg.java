package dev.gether.getcustomitem.cmd.arg;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.bukkit.command.CommandSender;

import java.util.*;

public class CustomItemArg extends ArgumentResolver<CommandSender, CustomItem> {

    private final ItemManager itemManager;
    private static final String ALL_ITEMS = "ALL";

    public CustomItemArg(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    protected ParseResult<CustomItem> parse(Invocation<CommandSender> invocation, Argument<CustomItem> context, String argument) {
        if (argument.equalsIgnoreCase(ALL_ITEMS)) {
            return ParseResult.success(createAllItemsPlaceholder());
        }

        Optional<CustomItem> customItemByKey = this.itemManager.findCustomItemByKey(argument);
        return customItemByKey.map(ParseResult::success).orElseGet(() -> ParseResult.failure("Item not found!"));
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<CustomItem> argument, SuggestionContext context) {
        List<String> suggestions = new ArrayList<>();

        String commandLabel = invocation.label().toLowerCase();

        if (commandLabel.startsWith("getregion") || commandLabel.startsWith("gr")) {
            suggestions.add(ALL_ITEMS);
        }

        suggestions.addAll(itemManager.getAllItemKey().asMultiLevelList().stream().toList());

        return SuggestionResult.of(suggestions);
    }

    private CustomItem createAllItemsPlaceholder() {
        return new CustomItem() {
            @Override
            public String getItemID() {
                return ALL_ITEMS;
            }

            @Override
            protected Map<String, String> replacementValues() {
                return new HashMap<>();
            }
        };
    }
}