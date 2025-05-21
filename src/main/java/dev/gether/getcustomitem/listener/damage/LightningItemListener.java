package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.LightningItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

public class LightningItemListener extends AbstractCustomItemListener<LightningItem> {


    public LightningItemListener(ItemManager itemManager,
                                 CooldownManager cooldownManager,
                                 FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof LightningItem lightningItem)) return;
        if (!lightningItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, lightningItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        lightningItem.playSound(damager.getLocation()); // play sound

        // alerts
        lightningItem.notifyYourself(damager);
        lightningItem.notifyOpponents(victim); // alert opponent

        LightningStrike lightning = (LightningStrike) victim.getWorld().spawnEntity(victim.getLocation(), EntityType.LIGHTNING);
        lightning.setMetadata("lightning", new FixedMetadataValue(GetCustomItem.getInstance(), true));
        lightning.setFireTicks(0);

//                    victim.getWorld().strikeLightningEffect(victim.getLocation());

        if(lightningItem.isTakeHeart()) {
            double heartPercentage = lightningItem.getHeartPercentage();

            double absorption = victim.getAbsorptionAmount();
            double health = victim.getHealth();


            double healthToTake = health * heartPercentage;


            double absorptionDamage = Math.min(absorption, healthToTake);
            double remainingAbsorption = absorption - absorptionDamage;
            victim.setAbsorptionAmount(remainingAbsorption);

            double remainingDamage = healthToTake - absorptionDamage;
            if (remainingDamage > 0) {
                victim.setHealth(Math.max(health - remainingDamage, 0));
            }
        } else {
            double maxHealth = victim.getMaxHealth();
            double damageAmount = maxHealth * lightningItem.getMultiplyDamage();

            victim.damage(damageAmount);
        }

    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof LightningStrike lightningStrike) {
            if(lightningStrike.hasMetadata("lightning")) {
                event.setCancelled(true);
            }
        }
    }

}