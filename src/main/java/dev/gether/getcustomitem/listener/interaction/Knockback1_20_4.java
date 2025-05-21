package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class Knockback1_20_4 implements Listener {

    private final FileManager fileManager;

    public Knockback1_20_4(FileManager fileManager) {
        this.fileManager = fileManager;
        GetCustomItem.getInstance().getServer().getPluginManager().registerEvents(this, GetCustomItem.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        boolean hasEffectWithDisabledKnockback = false;
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().getName().equalsIgnoreCase("HERO_OF_THE_VILLAGE")) {
                hasEffectWithDisabledKnockback = true;
                break;
            }
        }

        if (hasEffectWithDisabledKnockback) {
            Vector knockback = event.getFinalKnockback();

            double scaleXZ = fileManager.getConfig().getKnockbackX();
            double scaleY = 1.0;

            Vector scaled = new Vector(
                    knockback.getX() * scaleXZ,
                    knockback.getY() * scaleY,
                    knockback.getZ() * scaleXZ
            );

            if (fileManager.getConfig().isDebug()) {
                MessageUtil.logMessage(ConsoleColor.RED, "knockbackX: " + scaled.getX());
                MessageUtil.logMessage(ConsoleColor.RED, "knockbackZ: " + scaled.getZ());
            }

            event.setFinalKnockback(scaled);
        }
    }



}
