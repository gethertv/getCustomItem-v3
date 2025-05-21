package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.FrozenSword;
import dev.gether.getcustomitem.item.manager.FrozenManager;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class FrozenSwordListener extends AbstractCustomItemListener<FrozenSword> {


    private final FrozenManager frozenManager;

    public FrozenSwordListener(ItemManager itemManager,
                               CooldownManager cooldownManager,
                               FileManager fileManager,
                               FrozenManager frozenManager) {
        super(itemManager, cooldownManager, fileManager);
        this.frozenManager = frozenManager;
    }


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        double frozenTime = frozenManager.getFrozenTime(player);
        if(frozenTime > 0) {
            event.setCancelled(true);
        } else {
            frozenManager.cleanCache(player);
        }

    }
    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof FrozenSword frozenSword)) return;
        if(!frozenSword.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, frozenSword, event.getItemStack(), event.getEquipmentSlot())) return;

        double winTicket = GetCustomItem.getRandom().nextDouble() * 100;
        if(winTicket <= frozenSword.getChanceToFrozen()) {

            // particles and sound
            frozenSword.playSound(damager.getLocation()); // play sound

            // alerts
            frozenSword.notifyYourself(damager);
            frozenSword.notifyOpponents(victim);

            // freeze the player
            frozenManager.freeze(victim, frozenSword);

        }

    }


}