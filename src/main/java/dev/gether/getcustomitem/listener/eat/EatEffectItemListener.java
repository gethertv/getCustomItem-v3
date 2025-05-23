package dev.gether.getcustomitem.listener.eat;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemConsumeEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.EatEffectItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EatEffectItemListener extends AbstractCustomItemListener<EatEffectItem> {

    public EatEffectItemListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EatEffectItem eatEffectItem)) return;
        if (!eatEffectItem.isEnabled()) return;

        Player player = event.getPlayer();
        event.setCancelEvent(false);
    }


    @EventHandler
    public void onEatEffectConsume(CustomItemConsumeEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EatEffectItem eatEffectItem)) return;
        if (!eatEffectItem.isEnabled()) return;

        event.setCancelEvent(false);

        Player player = event.getPlayer();

        int usage = eatEffectItem.getUsage(event.getItemStack().getItemMeta());
        ItemStack itemStack = event.getItemStack();
        ItemStack secondItem = event.getItemStack().clone();
        if (!canUseItem(player, eatEffectItem, itemStack, event.getEquipmentSlot())) return;

        event.setItemStack(null);
        Bukkit.getScheduler().runTaskLater(GetCustomItem.getInstance(), () -> {
            player.getInventory().setItem(event.getEquipmentSlot(), usage == 1 ? null : itemStack);
            secondItem.setAmount(secondItem.getAmount() - 1);
            PlayerUtil.addItems(player, secondItem);
        }, 1L);

        eatEffectItem.notifyYourself(player);

        List<Player> nearbyEntities = EntityUtil.findNearbyEntities(player.getLocation(), eatEffectItem.getRadius(), Player.class);
        List<Player> validPlayers = filterPlayers(nearbyEntities);

        for (Player target : validPlayers) {
            if (!eatEffectItem.isAffectSelf() && target.equals(player)) continue;
            if (!eatEffectItem.isAffectOthers() && !target.equals(player)) continue;

            List<PotionEffect> potionEffectFromConfig = PotionConverUtil.getPotionEffectFromConfig(eatEffectItem.getPotionEffects());
            if (!potionEffectFromConfig.isEmpty()) {
                potionEffectFromConfig.forEach(target::addPotionEffect);
            }

            List<PotionEffectType> potionEffectTypes = PotionConverUtil.getPotionEffectByName(eatEffectItem.getRemoveEffects());
            player.getActivePotionEffects().forEach(potionEffect -> {
                if (potionEffectTypes.contains(potionEffect.getType()))
                    player.removePotionEffect(potionEffect.getType());
            });
        }
        eatEffectItem.playSound(player.getLocation());
    }


    private List<Player> filterPlayers(List<Player> listPlayers) {
        List<Player> players = new ArrayList<>();
        listPlayers.forEach(p -> {
            boolean isCitizensNPC = p.hasMetadata("NPC");
            if (isCitizensNPC) return;

            if (WorldGuardUtil.isInRegion(p) &&
                    WorldGuardUtil.isDeniedFlag(p.getLocation(), p, Flags.PVP)) {
                return;
            }

            players.add(p);
        });
        return players;
    }

}
