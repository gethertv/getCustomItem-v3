package dev.gether.getcustomitem.listener.damage;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.ValentineTNTItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.utils.MessageUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ValentineTNTListener extends AbstractCustomItemListener<ValentineTNTItem> {

    @Getter
    private final Map<UUID, Player> tntTargets = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    public ValentineTNTListener(ItemManager itemManager,
                                CooldownManager cooldownManager,
                                FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if(event.isCancelled()) return;
        if(!(event.getCustomItem() instanceof ValentineTNTItem valentineTNTItem)) return;
        if(!valentineTNTItem.isEnabled()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, valentineTNTItem, event.getItemStack(), event.getEquipmentSlot())) return;

        double winTicket = GetCustomItem.getRandom().nextDouble() * 100;
        if(winTicket <= valentineTNTItem.getChance()) {
            spawnValentineTNT(victim, valentineTNTItem);

            valentineTNTItem.playSound(damager.getLocation());
            valentineTNTItem.notifyYourself(damager);
            valentineTNTItem.notifyOpponents(victim);
        }
    }

    private void spawnValentineTNT(Player victim, ValentineTNTItem item) {
        Location spawnLoc = victim.getLocation().clone().add(0, item.getFollowHeight(), 0);
        TNTPrimed tnt = victim.getWorld().spawn(spawnLoc, TNTPrimed.class);

        tnt.setFuseTicks(item.getExplosionDelay());
        tnt.setGravity(false);
        tnt.setVelocity(new Vector(0, 0, 0));

        tnt.setMetadata(MetadataStorage.CUSTOM_ITEM, new FixedMetadataValue(GetCustomItem.getInstance(), item.getItemID()));

        tntTargets.put(tnt.getUniqueId(), victim);

        TNTFollowTask task = new TNTFollowTask(tnt, victim, item, this);
        task.runTaskTimer(GetCustomItem.getInstance(), 0L, item.getUpdateRate());
        activeTasks.put(tnt.getUniqueId(), task);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof TNTPrimed tnt)) return;
        if (!tnt.hasMetadata(MetadataStorage.CUSTOM_ITEM)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, Player> entry : tntTargets.entrySet()) {
            if (entry.getValue().equals(player)) {
                UUID tntUUID = entry.getKey();
                toRemove.add(tntUUID);

                BukkitRunnable task = activeTasks.remove(tntUUID);
                if (task != null) {
                    task.cancel();
                }
            }
        }

        toRemove.forEach(uuid -> {
            tntTargets.remove(uuid);
            for (org.bukkit.World world : GetCustomItem.getInstance().getServer().getWorlds()) {
                for (TNTPrimed tnt : world.getEntitiesByClass(TNTPrimed.class)) {
                    if (tnt.getUniqueId().equals(uuid)) {
                        tnt.remove();
                        break;
                    }
                }
            }
        });
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof TNTPrimed tnt)) return;

        if (!tnt.hasMetadata(MetadataStorage.CUSTOM_ITEM)) return;

        UUID tntUUID = tnt.getUniqueId();
        if (!tntTargets.containsKey(tntUUID)) return;

        event.setCancelled(true);

        BukkitRunnable task = activeTasks.remove(tntUUID);
        if (task != null) {
            task.cancel();
        }

        Player target = tntTargets.remove(tntUUID);
        if (target == null || !target.isOnline()) return;

        List<MetadataValue> metadata = tnt.getMetadata(MetadataStorage.CUSTOM_ITEM);
        if (metadata.isEmpty()) return;

        String itemId = metadata.get(0).asString();
        Optional<CustomItem> customItemByKey = itemManager.findCustomItemByKey(itemId);
        if(customItemByKey.isEmpty()) return;

        if(!(customItemByKey.get() instanceof ValentineTNTItem valentineTNTItem)) return;
        if (!valentineTNTItem.isEnabled()) return;

        double currentHealth = target.getHealth();

        double remainingHealthPercent = (100.0 - valentineTNTItem.getDamagePercent()) / 100.0;
        double newHealth = currentHealth * remainingHealthPercent;
        target.setHealth(newHealth);
    }

    void cleanupTask(UUID tntUUID) {
        activeTasks.remove(tntUUID);
    }
}

class TNTFollowTask extends BukkitRunnable {
    private final TNTPrimed tnt;
    private final Player target;
    private final ValentineTNTItem item;
    private final ValentineTNTListener listener;

    public TNTFollowTask(TNTPrimed tnt, Player target, ValentineTNTItem item, ValentineTNTListener listener) {
        this.tnt = tnt;
        this.target = target;
        this.item = item;
        this.listener = listener;
    }

    @Override
    public void run() {
        if (!isValid()) {
            cleanup();
            return;
        }

        updateTNTPosition();
        createHeartParticles();
    }

    private boolean isValid() {
        return tnt.isValid() && !tnt.isDead() && target.isOnline();
    }

    private void cleanup() {
        cancel();
        listener.cleanupTask(tnt.getUniqueId());
    }

    private void updateTNTPosition() {
        Location targetLoc = target.getLocation().clone().add(0, item.getFollowHeight(), 0);
        tnt.teleport(targetLoc);
    }

    private void createHeartParticles() {
        Location center = tnt.getLocation();

        for (int i = 0; i < item.getParticlePoints(); i++) {
            double angle = 2 * Math.PI * i / item.getParticlePoints();
            double x = item.getParticleRadius() * Math.cos(angle);
            double z = item.getParticleRadius() * Math.sin(angle);

            Location particleLoc = center.clone().add(x, 0, z);

            var config = item.getParticleConfig();
            if (!config.isEnable()) return;

            center.getWorld().spawnParticle(
                    config.getParticle(),
                    particleLoc,
                    config.getCount(),
                    config.getOffSetX(),
                    config.getOffSetY(),
                    config.getOffSetZ(),
                    config.getExtra()
            );
        }
    }
}