package dev.gether.getcustomitem.event;

import dev.gether.getcustomitem.item.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class CustomFishEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final CustomItem customItem;
    private final ItemStack itemStack;
    private final EquipmentSlot equipmentSlot;
    private final PlayerFishEvent.State state;
    private final Entity caught;
    private final FishHook hook;
    private boolean cancelEvent;

    public CustomFishEvent(Player player,
                           CustomItem customItem,
                           ItemStack itemStack,
                           EquipmentSlot equipmentSlot,
                           PlayerFishEvent.State state,
                           Entity caught,
                           FishHook hook) {
        super(player);
        this.customItem = customItem;
        this.itemStack = itemStack;
        this.equipmentSlot = equipmentSlot;
        this.state = state;
        this.caught = caught;
        this.hook = hook;
        this.cancelEvent = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}