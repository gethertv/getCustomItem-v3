package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.GoatLauncherItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GoatLauncherListener extends AbstractCustomItemListener<GoatLauncherItem> {

    private final ConcurrentHashMap<UUID, ActiveLauncher> activeLaunchers = new ConcurrentHashMap<>();

    @Data
    private static class ActiveLauncher {
        private final List<Entity> entities;
        private final BukkitRunnable task;
    }

    public GoatLauncherListener(ItemManager itemManager,
                                CooldownManager cooldownManager,
                                FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }


    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof GoatLauncherItem goatLauncherItem)) return;
        if (!goatLauncherItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, goatLauncherItem, event.getItemStack(), event.getEquipmentSlot())) return;

        goatLauncherItem.playSound(player.getLocation());
        goatLauncherItem.notifyYourself(player);

        removeActiveLauncher(player.getUniqueId());

        createLauncher(player, goatLauncherItem);
    }

    private void createLauncher(Player player, GoatLauncherItem item) {
        List<Entity> entities = spawnEntities(player, item);
        BukkitRunnable task = createLauncherTask(player, entities, item);

        activeLaunchers.put(player.getUniqueId(), new ActiveLauncher(entities, task));
        task.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
    }

    private List<Entity> spawnEntities(Player player, GoatLauncherItem item) {
        List<Entity> entities = new ArrayList<>();
        Location playerLoc = player.getLocation();
        Vector lookDir = playerLoc.getDirection().normalize();
        Vector perpendicular = new Vector(-lookDir.getZ(), 0, lookDir.getX()).normalize();

        int offset = (item.getGoatCount() - 1) * item.getGoatSpacing() / 2;

        for (int i = 0; i < item.getGoatCount(); i++) {
            Location spawnLoc = playerLoc.clone().add(
                    perpendicular.clone().multiply(offset - (i * item.getGoatSpacing()))
            );

            LivingEntity entity = (LivingEntity) playerLoc.getWorld().spawnEntity(spawnLoc, item.getEntityType());
            setupEntity(entity, player);
            entities.add(entity);
        }

        return entities;
    }

    private void setupEntity(LivingEntity entity, Player player) {
        entity.setAI(false);
        entity.setInvulnerable(true);
        entity.setGravity(false);
        entity.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
    }

    private BukkitRunnable createLauncherTask(Player player, List<Entity> entities, GoatLauncherItem item) {
        return new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = item.getGoatLifetime() * 20;
            final Vector moveDirection = player.getLocation().getDirection().normalize().multiply(0.2);

            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isOnline()) {
                    cleanup();
                    return;
                }

                moveAndCheckEntities(entities, player, moveDirection, item);
                ticks++;
            }

            private void moveAndCheckEntities(List<Entity> entities, Player player, Vector moveDirection, GoatLauncherItem item) {
                entities.forEach(entity -> {
                    entity.teleport(entity.getLocation().add(moveDirection));
                    entity.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());

                    EntityUtil.findNearbyEntities(entity.getLocation(), 1, Player.class)
                                    .stream()
                                    .filter(target -> target != player)
                                    .filter(target -> !target.hasMetadata("NPC"))
                                    .filter(target -> !WorldGuardUtil.isDeniedFlag(target.getLocation(), (Player)target, Flags.PVP))
                                    .forEach(target -> handleTarget((Player)target, player, item));

                });
            }

            private void handleTarget(Player target, Player source, GoatLauncherItem item) {
                Vector knockback = source.getLocation().getDirection().normalize()
                        .multiply(item.getPushPower());
                target.setVelocity(knockback);
                item.notifyOpponents(target);
            }

            private void cleanup() {
                entities.forEach(entity -> {
                    entity.setInvulnerable(false);
                    entity.remove();
                });
                cancel();
                activeLaunchers.remove(player.getUniqueId());
            }
        };
    }

    private void removeActiveLauncher(UUID playerUUID) {
        ActiveLauncher launcher = activeLaunchers.remove(playerUUID);
        if (launcher != null) {
            launcher.getTask().cancel();
            launcher.getEntities().forEach(entity -> {
                entity.setInvulnerable(false);
                entity.remove();
            });
        }
    }
}