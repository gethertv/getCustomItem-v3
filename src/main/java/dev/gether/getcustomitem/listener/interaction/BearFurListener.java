package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.BearFurItem;
import dev.gether.getcustomitem.item.manager.BearFurReducedManager;
import dev.gether.getcustomitem.item.model.UseItemData;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

public class BearFurListener extends AbstractCustomItemListener<BearFurItem> {

    private final BearFurReducedManager bearFurReducedManager;

    public BearFurListener(ItemManager itemManager,
                           CooldownManager cooldownManager,
                           BearFurReducedManager bearFurReducedManager,
                           FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.bearFurReducedManager = bearFurReducedManager;
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof BearFurItem bearFurItem)) return;
        if(!bearFurItem.isEnabled()) return;
        Player player = event.getPlayer();

        if (!canUseItem(player, bearFurItem, event.getItemStack(), event.getEquipmentSlot())) return;

        bearFurItem.playSound(player.getLocation());
        bearFurReducedManager.reducedDamage(player, bearFurItem);
        bearFurItem.notifyYourself(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if(event.isCancelled()) return;

        // check the entity is the player, if not then return/ignore
        if(!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // find the user are using the item
        Optional<UseItemData> useItemDataByUUID = bearFurReducedManager.findUseItemDataByUUID(victim.getUniqueId());
        if(useItemDataByUUID.isEmpty())
            return;

        UseItemData useItemData = useItemDataByUUID.get();
        BearFurItem bearFurItem = (BearFurItem) useItemData.getCustomItem();

        // if he used the item, check if the time has passed
        double reducedTime = bearFurReducedManager.getReducedTime(victim);
        if(reducedTime > 0) {
            double damage = event.getDamage();
            double latestDamage = damage * (bearFurItem.getReducedDamage() / 100);
            event.setDamage(latestDamage);
        }

    }



}