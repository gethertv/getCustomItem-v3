package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.HitEffectItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getutils.utils.PotionConverUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class HitEffectListener extends AbstractCustomItemListener<HitEffectItem> {

    private final Random random = new Random();

    public HitEffectListener(ItemManager itemManager,
                             CooldownManager cooldownManager,
                             FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof HitEffectItem hitEffectItem)) return;
        if(!hitEffectItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, hitEffectItem, event.getItemStack(), event.getEquipmentSlot())) return;

        double winTicket = random.nextDouble() * 100;
        if(winTicket <= hitEffectItem.getChance()) {

            // particles and sound
            hitEffectItem.playSound(damager.getLocation()); // play sound

            // alerts
            hitEffectItem.notifyYourself(damager);
            hitEffectItem.notifyOpponents(victim); // alert opponent

            if(hitEffectItem.isOpponents()) {
                giveEffect(victim, hitEffectItem);
            }
            if(hitEffectItem.isYourSelf()) {
                giveEffect(damager, hitEffectItem);
            }
        }

    }

    private void giveEffect(Player player, HitEffectItem hitEffectItem) {
        if(hitEffectItem.isRemoveEffect()) {
            List<PotionEffectType> potionEffectTypes = PotionConverUtil.getPotionEffectByName(hitEffectItem.getPotionEffectNames());
            player.getActivePotionEffects().forEach(potionEffect -> {
                if(potionEffectTypes.contains(potionEffect.getType()))
                    player.removePotionEffect(potionEffect.getType());
            });
        }
        List<PotionEffect> activePotionEffect = PotionConverUtil.getPotionEffectFromConfig(hitEffectItem.getPotionEffectConfigs());
        activePotionEffect.forEach(player::addPotionEffect); // set new effect
    }


}