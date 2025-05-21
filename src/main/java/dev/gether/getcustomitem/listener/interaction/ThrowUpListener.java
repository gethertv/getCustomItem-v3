package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ThrowUpItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ThrowUpListener extends AbstractCustomItemListener<ThrowUpItem> {

    public ThrowUpListener(ItemManager itemManager,
                           CooldownManager cooldownManager,
                           FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ThrowUpItem throwUpItem)) return;
        if (!throwUpItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, throwUpItem, event.getItemStack(), event.getEquipmentSlot())) return;

        List<Player> nearPlayers = EntityUtil.findNearbyEntities(player.getLocation(), throwUpItem.getRadius(), Player.class);
        List<Player> validPlayers = filterPlayers(nearPlayers);

        throwUpItem.playSound(player.getLocation());

        throwUpItem.notifyYourself(player);
        validPlayers.forEach(p -> {
            if(!p.getName().equalsIgnoreCase(player.getName())) {
                throwUpItem.notifyOpponents(p);
            }
        });

        tossUpward(throwUpItem, player, validPlayers);
    }

    private List<Player> filterPlayers(List<Player> listPlayers) {
        List<Player> players = new ArrayList<>();
        listPlayers.forEach(p -> {
            boolean isCitizensNPC = p.hasMetadata("NPC");
            if (isCitizensNPC) return;

            if (WorldGuardUtil.isDeniedFlag(p.getLocation(), p, Flags.PVP)) {
                return;
            }

            players.add(p);
        });
        return players;
    }

    private void tossUpward(ThrowUpItem throwUpItem, Player sourcePlayer, List<Player> nearPlayers) {
        for (Player targetPlayer : nearPlayers) {
            if (throwUpItem.isIncludingYou() && targetPlayer.equals(sourcePlayer)) {
                Vector selfVector = sourcePlayer.getLocation().getDirection()
                        .normalize().multiply(-throwUpItem.getPushPowerSelf())
                        .setY(throwUpItem.getUpwardPowerSelf());

                if (canVelocity(targetPlayer, selfVector, true)) {
                    targetPlayer.setVelocity(selfVector);
                }
                continue;
            }

            if (!throwUpItem.isOtherPlayers()) return;
            if (targetPlayer.equals(sourcePlayer)) return;

            Vector direction = targetPlayer.getLocation()
                    .toVector()
                    .subtract(sourcePlayer.getLocation().toVector())
                    .normalize()
                    .multiply(throwUpItem.getPushPowerOpponents());

            direction.setY(throwUpItem.getUpwardPowerOpponents());

            if (canVelocity(targetPlayer, direction, false)) {
                targetPlayer.setVelocity(direction);
            }
        }
    }

    private boolean canVelocity(Player player, Vector vector, boolean alert) {
        Location currentLocation = player.getLocation().clone();

        double stepSize = 0.8D;
        double distance = vector.length() * 10.0D;

        Vector stepVector = vector.clone().normalize().multiply(stepSize);

        for (double i = 0.0D; i <= distance; i += stepSize) {
            currentLocation.add(stepVector);

            if (WorldGuardUtil.isDeniedFlag(currentLocation, player, Flags.PVP)) {
                return false;
            }
        }
        return true;
    }
}
