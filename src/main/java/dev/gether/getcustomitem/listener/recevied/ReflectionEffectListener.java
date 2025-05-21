package dev.gether.getcustomitem.listener.recevied;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomItemReceivedDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.PokeballItem;
import dev.gether.getcustomitem.item.customize.ReflectionEffectItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.PotionConverUtil;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class ReflectionEffectListener extends AbstractCustomItemListener<ReflectionEffectItem> {

    public ReflectionEffectListener(ItemManager itemManager,
                                    CooldownManager cooldownManager,
                                    FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ReflectionEffectItem reflectionEffectItem)) return;
        if (!reflectionEffectItem.isEnabled()) return;

        event.setCancelEvent(reflectionEffectItem.isCancelInteract());
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemReceivedDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ReflectionEffectItem reflectionEffectItem)) return;
        if (!reflectionEffectItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, reflectionEffectItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        reflectionEffectItem.playSound(damager.getLocation()); // play sound

        // alerts
        reflectionEffectItem.notifyYourself(damager);
        reflectionEffectItem.notifyOpponents(victim); // alert opponent

        if (reflectionEffectItem.isOpponents()) {
            giveEffect(damager, reflectionEffectItem);
        }
        if (reflectionEffectItem.isYourSelf()) {
            giveEffect(victim, reflectionEffectItem);
        }

    }


    private void giveEffect(Player player, ReflectionEffectItem reflectionEffectItem) {
        List<PotionEffect> activePotionEffect = PotionConverUtil.getPotionEffectFromConfig(reflectionEffectItem.getPotionEffectConfigs());
        activePotionEffect.forEach(player::addPotionEffect); // set new effect
    }


}