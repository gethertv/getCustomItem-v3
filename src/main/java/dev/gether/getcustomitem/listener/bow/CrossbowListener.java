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
import dev.gether.getcustomitem.utils.TeleportUtil;
import dev.gether.getutils.builder.ItemStackBuilder;
import dev.gether.getutils.utils.ConsoleColor;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

public class CrossbowListener extends AbstractCustomItemListener<CrossBowItem> implements Listener {

    private final GetCustomItem plugin;

    public CrossbowListener(GetCustomItem plugin,
                            ItemManager itemManager,
                            CooldownManager cooldownManager,
                            FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CrossBowItem crossBowItem)) return;
        if (!crossBowItem.isEnabled()) return;

        Player player = event.getPlayer();

        CrossbowMeta crossbowMeta = (CrossbowMeta) event.getItemStack().getItemMeta();
        if(crossbowMeta == null) return;

        if(crossBowItem.isAutoReload() && !crossbowMeta.hasChargedProjectiles()) {
            crossbowMeta.addChargedProjectile(ItemStackBuilder.of(crossBowItem.getProjectileMaterial())
                    .build());

            event.getItemStack().setItemMeta(crossbowMeta);
            return;
        }

//        if (!canUseItem(player, crossBowItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);
    }

    @EventHandler
    public void onCustomItemBowShoot(CustomItemBowShootEvent event) {
        if(event.isCancelled()) return;

        Player player = event.getPlayer();
        Projectile projectile = event.getProjectile();
        ItemStack itemStack = event.getItemStack();


        if(!(event.getCustomItem() instanceof CrossBowItem crossBowItem)) return;
        if (!crossBowItem.isEnabled()) return;
        if (!canUseItem(player, crossBowItem, event.getItemStack(), event.getEquipmentSlot())) {
            event.setCancelEvent(true);
            return;
        }


        crossBowItem.runParticles(projectile);
        crossBowItem.playSound(projectile.getLocation());

        projectile.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(plugin, crossBowItem.getItemID()));
        projectile.setMetadata(MetadataStorage.PROJECTILE_LOCATION, new FixedMetadataValue(plugin, player.getLocation()));

        crossBowItem.maxRangeTask(projectile, crossBowItem.getMaxRange());

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
        if(customItem instanceof CrossBowItem crossBowItem) {
            event.setCancelled(crossBowItem.isDisableDamage());
        }
    }



    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if (!(event.getCustomItem() instanceof CrossBowItem crossBowItem)) return;
        if (!crossBowItem.isEnabled()) return;

        // take a permission which ignore received effect
        if (hitPlayer.hasPermission(crossBowItem.getIgnorePermission()))
            return;

        // hook essentialsX fly
        if(GetCustomItem.getInstance().getHookManager().getEssentialsXHook().hasFlyPermission(shooter))
            return;

        double winTicket = GetCustomItem.getRandom().nextDouble() * 100;
        if (winTicket <= crossBowItem.getChance()) {
            crossBowItem.notifyYourself(shooter);
            crossBowItem.notifyOpponents(hitPlayer);

            List<MetadataValue> locationMetadata = projectile.getMetadata(MetadataStorage.PROJECTILE_LOCATION);
            if (!locationMetadata.isEmpty()) {
                Location originalLocation = (Location) locationMetadata.get(0).value();
                if (crossBowItem.isSafeTeleport()) {
                    TeleportUtil.safeTp(hitPlayer, originalLocation);
                } else {
                    hitPlayer.teleport(originalLocation);
                }
            }
        }
    }
}