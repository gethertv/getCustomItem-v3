package dev.gether.getcustomitem.event;

import dev.gether.getcustomitem.item.CustomItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class CustomProjectileHitEvent extends Event implements Cancellable {
    private final static HandlerList HANDLERS = new HandlerList();
    private final Projectile projectile;
    private final CustomItem customItem;
    private final Entity hitEntity;
    private final Block hitBlock;
    private boolean cancelled;
    private boolean cancelEvent;

    public CustomProjectileHitEvent(Projectile projectile, CustomItem customItem, Entity hitEntity, Block hitBlock) {
        this.projectile = projectile;
        this.customItem = customItem;
        this.hitEntity = hitEntity;
        this.hitBlock = hitBlock;
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
