package dev.gether.getcustomitem.event;

import dev.gether.getcustomitem.item.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class CustomItemDamageEvent extends Event implements Cancellable {
    private final static HandlerList HANDLERS = new HandlerList();
    private final Player damager;
    private final Player victim;
    private final CustomItem customItem;
    private final ItemStack itemStack;
    private final EquipmentSlot equipmentSlot;
    private boolean cancelled;
    private boolean cancelEvent;

    public CustomItemDamageEvent(Player damager, Player victim, CustomItem customItem, ItemStack itemStack, EquipmentSlot equipmentSlot) {
        this.damager = damager;
        this.victim = victim;
        this.customItem = customItem;
        this.itemStack = itemStack;
        this.equipmentSlot = equipmentSlot;
        this.cancelled = false;
        this.cancelEvent = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
