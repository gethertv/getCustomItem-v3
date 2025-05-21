package dev.gether.getcustomitem.listener.resurrect;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemResurrectEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.resurrect.ConfusionTotemItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.List;
import java.util.stream.Collectors;

public class ConfusionTotemListener extends AbstractCustomItemListener<ConfusionTotemItem> {

    public ConfusionTotemListener(ItemManager itemManager,
                                  CooldownManager cooldownManager,
                                  FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemDamage(CustomItemResurrectEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ConfusionTotemItem confusionTotemItem)) return;
        if (!confusionTotemItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, confusionTotemItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);

        List<Player> nearbyPlayers = EntityUtil.findNearbyEntities(player.getLocation(), confusionTotemItem.getRadius(), Player.class)
                .stream()
                .filter(p -> !p.hasMetadata("NPC") &&
                        (!WorldGuardUtil.isInRegion(p) || !WorldGuardUtil.isDeniedFlag(p.getLocation(), p, Flags.PVP)))
                .collect(Collectors.toList());

        if (confusionTotemItem.isAffectYourself() && !nearbyPlayers.contains(player)) {
            nearbyPlayers.add(player);
        }

        confusionTotemItem.swapPlayersAndApplyEffects(player, nearbyPlayers);
        confusionTotemItem.playSound(player.getLocation());
        confusionTotemItem.notifyYourself(player);

        for (Player affectedPlayer : nearbyPlayers) {
            if (affectedPlayer != player) {
                confusionTotemItem.notifyOpponents(affectedPlayer);
            }
        }

    }
}
