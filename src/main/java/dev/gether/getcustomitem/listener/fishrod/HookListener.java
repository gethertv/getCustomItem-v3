package dev.gether.getcustomitem.listener.fishrod;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomFishEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.AntiElytraHookItem;
import dev.gether.getcustomitem.item.customize.HookItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

public class HookListener extends AbstractCustomItemListener<HookItem> {

    public HookListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteract(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof HookItem)) return;

        event.setCancelEvent(false);
    }


    @EventHandler
    public void onCustomFish(CustomFishEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof HookItem hookItem)) return;
        if (!hookItem.isEnabled()) return;

        Player player = event.getPlayer();
        PlayerFishEvent.State state = event.getState();

        if (!isValidState(state)) return;

        FishHook hook = event.getHook();
        if (state == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = hook.getHookedEntity();
            if (caught instanceof Player targetPlayer) {
                if (isPvpDenied(player, targetPlayer)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if (!canUseItem(player, hookItem, event.getItemStack(), event.getEquipmentSlot())) return;

        Location hookLocation = hook.getLocation();
        Location playerLocation = player.getLocation();

        Vector vector = playerLocation.toVector();
        Vector direction = hookLocation.toVector().subtract(vector).normalize();

        double multiply = hookItem.getMultiply();
        if (player.isGliding() && hookItem.getDivideGliding() != 0) {
            multiply /= hookItem.getDivideGliding();
        }

        Vector velocity = direction.multiply(multiply);
        velocity.setY(velocity.getY() / hookItem.getDivideHeight());

        player.setVelocity(velocity);

        hookItem.notifyYourself(player);
    }

    private boolean isValidState(PlayerFishEvent.State state) {
        return state == PlayerFishEvent.State.IN_GROUND ||
                state == PlayerFishEvent.State.CAUGHT_ENTITY ||
                state == PlayerFishEvent.State.REEL_IN;
    }


    private boolean isPvpDenied(Player hooker, Player target) {
        if (WorldGuardUtil.isDeniedFlag(hooker.getLocation(), hooker, Flags.PVP)) {
            return true;
        }
        if (WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP)) {
            return true;
        }
        return false;
    }
}