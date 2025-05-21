package dev.gether.getcustomitem.listener.bow;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemBowShootEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.bow.EnderBowItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.builder.ItemStackBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class EnderBowListener extends AbstractCustomItemListener<EnderBowItem> {

    private final GetCustomItem plugin;

    public EnderBowListener(GetCustomItem plugin,
                            ItemManager itemManager,
                            CooldownManager cooldownManager,
                            FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EnderBowItem enderBowItem)) return;
        if (!enderBowItem.isEnabled()) return;

        ItemStack itemStack = event.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemStack.getType() == Material.CROSSBOW && itemMeta instanceof CrossbowMeta crossbowMeta) {
            if(enderBowItem.isAutoReload() && !crossbowMeta.hasChargedProjectiles()) {
                crossbowMeta.addChargedProjectile(ItemStackBuilder.of(enderBowItem.getProjectileMaterial())
                        .name("headshot")
                        .glow(true)
                        .build());

                event.getItemStack().setItemMeta(crossbowMeta);
                return;
            }
        }

        if (!canUseItem(event.getPlayer(), enderBowItem, event.getItemStack(), event.getEquipmentSlot())) return;

        event.setCancelEvent(false);
    }


    @EventHandler
    public void onEntityShootBow(CustomItemBowShootEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof EnderBowItem enderBowItem)) return;
        if (!enderBowItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, enderBowItem, event.getItemStack(), event.getEquipmentSlot())) return;

        Projectile projectile = event.getProjectile();

        projectile.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(plugin, enderBowItem.getItemID()));

        enderBowItem.runParticles(projectile);
        enderBowItem.playSound(projectile.getLocation());


        enderBowItem.maxRangeTask(projectile, enderBowItem.getMaxRange());
    }

    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;

        if (!(event.getCustomItem() instanceof EnderBowItem enderBowItem)) return;
        if (!enderBowItem.isEnabled()) return;

        // take a permission which ignore received effect
        if (event.getHitEntity() instanceof Player hitPlayer && hitPlayer.hasPermission(enderBowItem.getPermissionBypass()))
            return;

        Location originalLocation = projectile.getLocation();
        shooter.teleport(originalLocation);
        enderBowItem.notifyYourself(shooter);


    }

}