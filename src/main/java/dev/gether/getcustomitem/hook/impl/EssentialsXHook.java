package dev.gether.getcustomitem.hook.impl;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;

public class EssentialsXHook {

    private boolean enabled = false;
    private final Object essentialsX;

    public EssentialsXHook(GetCustomItem plugin) {
        essentialsX = plugin.getServer().getPluginManager().getPlugin("Essentials");
        if(essentialsX != null) {
            enabled = true;
            MessageUtil.logMessage(ConsoleColor.GREEN, "[getCustomItem-v3] Successfully hooked into EssentialsX");
        }
    }

    public boolean hasFlyPermission(Player player) {
        if(!enabled)
            return false;

        try {
            if (essentialsX == null) {
                return false;
            }
            Method getUserMethod = essentialsX.getClass().getMethod("getUser", Player.class);
            Object user = getUserMethod.invoke(essentialsX, player);

            if (user != null) {
                Method getBaseMethod = user.getClass().getMethod("getBase");
                Object base = getBaseMethod.invoke(user);

                if (base != null) {
                    Method getAllowFlightMethod = base.getClass().getMethod("getAllowFlight");
                    return (boolean) getAllowFlightMethod.invoke(base) && player.isFlying();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}