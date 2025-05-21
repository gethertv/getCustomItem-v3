package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.CustomTrackingStunItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class CustomTrackingStunListener extends AbstractCustomItemListener<CustomTrackingStunItem> {

    public CustomTrackingStunListener(ItemManager itemManager,
                                      CooldownManager cooldownManager,
                                      FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CustomTrackingStunItem stunItem)) return;
        if (!stunItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, stunItem, event.getItemStack(), event.getEquipmentSlot())) return;

        EntityUtil.findNearbyEntities(player.getLocation(), stunItem.getSearchRadius(), Player.class).stream()
                .filter(target -> target != player)
                .filter(target -> !target.hasMetadata("NPC"))
                .filter(target -> !WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP))
                .forEach(target -> {
                    stunItem.shootTrackingParticles(player, target);
                });

        stunItem.playSound(player.getLocation());
        stunItem.notifyYourself(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata(MetadataStorage.STUNNED)) return;

        if (event.getTo().getX() != event.getFrom().getX() ||
                event.getTo().getY() != event.getFrom().getY() ||
                event.getTo().getZ() != event.getFrom().getZ()) {

            event.setTo(event.getFrom());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(MetadataStorage.STUNNED)) {
            String itemKey = player.getMetadata(MetadataStorage.CUSTOM_ITEM).get(0).asString();
            Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemKey);

            if (customItemByKey.isPresent() && customItemByKey.get() instanceof CustomTrackingStunItem stunItem) {
                stunItem.removeStun(player);
            }
        }
    }
}