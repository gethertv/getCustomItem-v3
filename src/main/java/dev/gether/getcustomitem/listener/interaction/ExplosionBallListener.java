package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ExplosionBallItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.gettempblock.GetTempBlock;
import dev.gether.getutils.utils.EntityUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.apache.logging.log4j.message.Message;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

public class ExplosionBallListener extends AbstractCustomItemListener<ExplosionBallItem> {

    private final ItemManager itemManager;

    public ExplosionBallListener(ItemManager itemManager,
                                 CooldownManager cooldownManager,
                                 FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.itemManager = itemManager;
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof ExplosionBallItem explosionBallItem)) return;
        if (!explosionBallItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, explosionBallItem, event.getItemStack(), event.getEquipmentSlot())) return;

        // particles and sound
        explosionBallItem.playSound(player.getLocation()); // play sound

        // clean cobweb
        explosionBallItem.throwExplosionBall(player);

    }

    @EventHandler
    public void onExplostion(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        Entity entity = event.getEntity();
        List<MetadataValue> metadata = entity.getMetadata(MetadataStorage.PROJECTILE_METADATA);
        if (metadata.isEmpty())
            return;

        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(metadata.get(0).asString());
        if (customItemByKey.isEmpty()) return;

        if (!(customItemByKey.get() instanceof ExplosionBallItem explosionBallItem)) return;


        List<Block> blocks = event.blockList();
        event.setCancelled(true);
        if (!explosionBallItem.isBreakBlocks())
            return;

        explosionBallItem.playExplodeSound(entity.getLocation());

        blocks.forEach(block -> {
            if(explosionBallItem.isHookTempBlocks()) {
                if (!GetTempBlock.getInstance().getBlockCleanManager().containsBlock(block.getLocation())) {
                    return;
                }
                GetTempBlock.getInstance().getBlockCleanManager().destoryBlock(block.getLocation());
                block.breakNaturally();
                return;
            }
            if (explosionBallItem.getBlockedMaterials().contains(block.getType()))
                return;

            if (WorldGuardUtil.isDeniedFlag(block.getLocation(), null, Flags.BLOCK_BREAK)) {
                return;
            }

            block.breakNaturally();
        });

    }

    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;

        if (!(event.getCustomItem() instanceof ExplosionBallItem explosionBallItem)) return;
        if (!explosionBallItem.isEnabled()) return;

        if (event.getHitEntity() instanceof Player hitPlayer && hitPlayer.hasPermission(explosionBallItem.getPermissionBypass()))
            return;

        Location hitLocation = event.getProjectile().getLocation().clone();
//        projectile.remove();

        explosionBallItem.playSound(hitLocation);

        List<Player> nearPlayers = EntityUtil.findNearbyEntities(hitLocation, (int) explosionBallItem.getExplosionPower(), Player.class);

        takeDurability(nearPlayers, explosionBallItem);

        explosionBallItem.notifyYourself(shooter);

    }

    private void takeDurability(List<Player> nearPlayers, ExplosionBallItem explosionBallItem) {
        if (explosionBallItem.getDestroyDurability() <= 0) {
            return;
        }
        for (Player player : nearPlayers) {
            for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                if (armorPiece != null && armorPiece.getType().getMaxDurability() > 0) {
                    ItemMeta meta = armorPiece.getItemMeta();
                    if (meta != null && meta.isUnbreakable()) {
                        continue;
                    }

                    if (meta instanceof Damageable) {
                        Damageable damageable = (Damageable) meta;
                        int currentDamage = damageable.getDamage();
                        int newDamage = currentDamage + explosionBallItem.getDestroyDurability();

                        damageable.setDamage(newDamage);
                        armorPiece.setItemMeta(meta);

                        if (newDamage >= armorPiece.getType().getMaxDurability()) {
                            player.getInventory().remove(armorPiece);
                        }
                    }
                }
            }
        }
    }



}