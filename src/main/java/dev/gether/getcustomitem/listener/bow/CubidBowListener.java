package dev.gether.getcustomitem.listener.bow;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemBowShootEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.bow.CrossBowItem;
import dev.gether.getcustomitem.item.customize.bow.CupidBowItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.builder.ItemStackBuilder;
import dev.gether.getutils.utils.MessageUtil;
import dev.gether.getutils.utils.PotionConverUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Optional;

public class CubidBowListener extends AbstractCustomItemListener<CupidBowItem> {

    private final GetCustomItem plugin;

    public CubidBowListener(GetCustomItem plugin,
                            ItemManager itemManager,
                            CooldownManager cooldownManager,
                            FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CupidBowItem cupidBowItem)) return;
        if (!cupidBowItem.isEnabled()) return;

        if(event.getItemStack().getItemMeta() instanceof CrossbowMeta crossbowMeta) {
            if(cupidBowItem.isAutoReload() && !crossbowMeta.hasChargedProjectiles()) {
                crossbowMeta.addChargedProjectile(ItemStackBuilder.of(cupidBowItem.getProjectileMaterial())
                        .build());

                event.getItemStack().setItemMeta(crossbowMeta);
                return;
            }
        }


//        if (!canUseItem(event.getPlayer(), cupidBowItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile)) return;
        if (!projectile.hasMetadata(MetadataStorage.PROJECTILE_METADATA)) return;

        List<MetadataValue> metadata = projectile.getMetadata(MetadataStorage.PROJECTILE_METADATA);
        if(metadata.isEmpty()) return;

        MetadataValue metadataValue = metadata.get(0);
        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(metadataValue.asString());
        if(customItemByKey.isEmpty()) return;

        CustomItem customItem = customItemByKey.get();
        if(customItem instanceof CupidBowItem cupidBowItem) {
            event.setCancelled(cupidBowItem.isDisableDamage());
        }
    }


    @EventHandler
    public void onCustomItemBowShoot(CustomItemBowShootEvent event) {
        if(event.isCancelled()) return;
        Player player = event.getPlayer();
        Projectile projectile = event.getProjectile();
        ItemStack itemStack = event.getItemStack();

        if(!(event.getCustomItem() instanceof CupidBowItem cupidBowItem)) return;
        if (!cupidBowItem.isEnabled()) return;

        if (!canUseItem(player, cupidBowItem, event.getItemStack(), event.getEquipmentSlot())) {
            event.setCancelEvent(true);
            return;
        }


        cupidBowItem.runParticles(projectile); // particles
        cupidBowItem.playSound(projectile.getLocation()); // play sound

        cupidBowItem.maxRangeTask(projectile, cupidBowItem.getMaxRange());
        // add custom meta to arrow for help with verify custom arrow
        projectile.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(plugin, cupidBowItem.getItemID()));

    }


    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if(!(projectile.getShooter() instanceof Player shooter)) return;
        if(!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if(!(event.getCustomItem() instanceof CupidBowItem cupidBowItem)) return;
        if (!cupidBowItem.isEnabled()) return;

        // take a permission which ignore received effect
        if (hitPlayer.hasPermission(cupidBowItem.getIgnorePermission()))
            return;

        double winTicket = GetCustomItem.getRandom().nextDouble() * 100;
        if (winTicket <= cupidBowItem.getChance()) {

            // alert
            cupidBowItem.notifyYourself(shooter);
            cupidBowItem.notifyOpponents(hitPlayer);

            List<PotionEffect> activePotionEffect = PotionConverUtil.getPotionEffectFromConfig(cupidBowItem.getPotionEffectConfigs());
            activePotionEffect.forEach(hitPlayer::addPotionEffect); // set new effect
        }

    }


}
