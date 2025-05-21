package dev.gether.getcustomitem.utils;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;

public class DebugMode {

    public static void debug(CustomItem item) {
        if(GetCustomItem.getInstance().getFileManager().getConfig().isDebug()) {
            MessageUtil.logMessage(ConsoleColor.YELLOW, "[getCustomItem] Global: "+item.getClass().getName());
        }
    }
}
