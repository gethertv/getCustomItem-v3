package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.EffectRadiusItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.PotionConverUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EffectRadiusListener extends AbstractCustomItemListener<EffectRadiusItem> {

    private final boolean hasKnockbackEvent;

    public EffectRadiusListener(ItemManager itemManager,
                                CooldownManager cooldownManager,
                                FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        boolean hasEvent = false;
        try {
            Class.forName("org.bukkit.event.entity.EntityKnockbackEvent");
            hasEvent = true;
        } catch (ClassNotFoundException e) {
            hasEvent = false;
        }
        this.hasKnockbackEvent = hasEvent;

        if (hasKnockbackEvent) {
            new Knockback1_20_4(fileManager);

        }
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EffectRadiusItem effectRadiusItem)) return;
        if (!effectRadiusItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, effectRadiusItem, event.getItemStack(), event.getEquipmentSlot())) return;

        List<Player> nearPlayers = EntityUtil.findNearbyEntities(player.getLocation(), effectRadiusItem.getRadius(), Player.class);
        List<Player> players = filterPlayers(nearPlayers);

        // alert
        effectRadiusItem.notifyYourself(player);
        players.forEach(p -> {
            if(p.getName().equalsIgnoreCase(player.getName()))
                return;

            effectRadiusItem.notifyOpponents(p);
        });


        // particles and sound
        effectRadiusItem.playSound(player.getLocation()); // play sound

        givePotionEffect(effectRadiusItem, player, players); // give potion effect
        removePotionEffect(effectRadiusItem, player, players); // remove potion effect

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
    private void givePotionEffect(EffectRadiusItem effectRadiusItem,
                                  Player player,
                                  List<Player> nearPlayers) {
        nearPlayers.forEach(p -> {

            if(!effectRadiusItem.isIncludingYou() && p.getName().equalsIgnoreCase(player.getName()))
                return;

            if(!effectRadiusItem.isOtherPlayers() && !p.getName().equalsIgnoreCase(player.getName()))
                return;


            List<PotionEffect> activePotionEffect = PotionConverUtil.getPotionEffectFromConfig(effectRadiusItem.getActiveEffects());
            activePotionEffect.forEach(p::addPotionEffect);
        });
    }

    private void removePotionEffect(EffectRadiusItem effectRadiusItem,
                                    Player player,
                                    List<Player> nearPlayers) {
        // check every player who can't be in the pvp region
        nearPlayers.forEach(p -> {
            // check is not the npc
            boolean isCitizensNPC = p.hasMetadata("NPC");
            if(isCitizensNPC) return;

            if(!effectRadiusItem.isIncludingYou() && p.getName().equalsIgnoreCase(player.getName()))
                return;

            if(!effectRadiusItem.isOtherPlayers() && !p.getName().equalsIgnoreCase(player.getName()))
                return;

            if(WorldGuardUtil.isInRegion(p) &&
                    WorldGuardUtil.isDeniedFlag(p.getLocation(), p, Flags.PVP)) {
                return;
            }
            // remove potion effect
            List<PotionEffectType> potionEffectTypes = PotionConverUtil.getPotionEffectByName(effectRadiusItem.getRemoveEffects());
            p.getActivePotionEffects().forEach(potionEffect -> {
                if(potionEffectTypes.contains(potionEffect.getType()))
                    p.removePotionEffect(potionEffect.getType());
            });
        });
    }




}