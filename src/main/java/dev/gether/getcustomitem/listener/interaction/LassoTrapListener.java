package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.LassoTrapItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class LassoTrapListener extends AbstractCustomItemListener<LassoTrapItem> {

    private final Map<UUID, Set<LassoConnection>> activeTargetConnections = new HashMap<>();
    private final Map<UUID, Set<LassoConnection>> activeUserConnections = new HashMap<>();

    private static class LassoConnection {
        final UUID userUUID;
        final UUID targetUUID;
        final Chicken chicken;
        final LassoTrapItem item;
        BukkitRunnable task;

        LassoConnection(UUID userUUID, UUID targetUUID, Chicken chicken, LassoTrapItem item) {
            this.userUUID = userUUID;
            this.targetUUID = targetUUID;
            this.chicken = chicken;
            this.item = item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LassoConnection that = (LassoConnection) o;
            return Objects.equals(userUUID, that.userUUID) && Objects.equals(targetUUID, that.targetUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userUUID, targetUUID);
        }
    }

    public LassoTrapListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if(event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof LassoTrapItem lassoTrapItem)) return;
        if (!lassoTrapItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, lassoTrapItem, event.getItemStack(), event.getEquipmentSlot())) return;

        Player target = findPlayer(player, (int) lassoTrapItem.getCatchRadius());
        if (target == null) {
            MessageUtil.sendMessage(player, lassoTrapItem.getNoRange());
            return;
        }

        if(WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP)) return;

        cleanupAllConnections(player.getUniqueId(), target.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                Location spawnLocation = target.getLocation().clone().add(0, 0.8, 0);
                Chicken chicken = target.getWorld().spawn(spawnLocation, Chicken.class);
                setupChicken(chicken);
                chicken.setLeashHolder(player);

                LassoConnection connection = new LassoConnection(
                        player.getUniqueId(),
                        target.getUniqueId(),
                        chicken,
                        lassoTrapItem
                );

                synchronized (activeTargetConnections) {
                    activeTargetConnections.computeIfAbsent(target.getUniqueId(), k -> new HashSet<>()).add(connection);
                }
                synchronized (activeUserConnections) {
                    activeUserConnections.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(connection);
                }

                startLassoTask(player, target, chicken, connection);
            }
        }.runTask(GetCustomItem.getInstance());

        if (lassoTrapItem.getSoundConfig().isEnable()) {
            lassoTrapItem.playSound(player.getLocation());
        }
    }

    private void setupChicken(Chicken chicken) {
        chicken.setInvisible(true);
        chicken.setAI(false);
        chicken.setSilent(true);
        chicken.setInvulnerable(true);
        chicken.setCollidable(false);
        chicken.setCanPickupItems(false);
        chicken.setGravity(false);
        chicken.setAdult();
        chicken.setAgeLock(true);
        chicken.setBreed(false);
    }

    private void startLassoTask(Player player, Player target, Chicken chicken, LassoConnection connection) {
        BukkitRunnable runnable = new BukkitRunnable() {
            private int ticksLeft = connection.item.getTeleportDelay() * 20;

            @Override
            public void run() {
                if (!player.isOnline() || !target.isOnline() || !chicken.isValid() ||
                        player.getGameMode() == GameMode.SPECTATOR || target.getGameMode() == GameMode.SPECTATOR) {
                    cancel();
                    cleanupConnection(connection);
                    return;
                }

                if (player.getLocation().distance(target.getLocation()) > connection.item.getBreakDistance()) {
                    MessageUtil.sendMessage(player, connection.item.getBreakMessage());
                    cancel();
                    cleanupConnection(connection);
                    return;
                }

                chicken.teleport(target.getLocation().clone().add(0, 0.8, 0));

                if (ticksLeft <= 0) {
                    if (Math.random() * 100 <= connection.item.getTeleportChance()) {
                        if (player.isOnline() && target.isOnline()) {
                            target.teleport(player.getLocation());

                            if (connection.item.getPullSound().isEnable()) {
                                player.playSound(player.getLocation(), connection.item.getPullSound().getSound(), 1.0f, 1.0f);
                                target.playSound(target.getLocation(), connection.item.getPullSound().getSound(), 1.0f, 1.0f);
                            }

                            MessageUtil.sendMessage(player, connection.item.getSuccessMessage());
                            MessageUtil.sendMessage(target, connection.item.getSuccessMessage());
                        }
                    } else {
                        if (connection.item.getFailSound().isEnable()) {
                            player.playSound(player.getLocation(), connection.item.getFailSound().getSound(), 1.0f, 1.0f);
                            target.playSound(target.getLocation(), connection.item.getFailSound().getSound(), 1.0f, 1.0f);
                        }

                        MessageUtil.sendMessage(player, connection.item.getFailMessage());
                        MessageUtil.sendMessage(target, connection.item.getFailMessage());
                    }
                    cancel();
                    cleanupConnection(connection);
                    return;
                }

                ticksLeft--;
            }
        };

        connection.task = runnable;
        runnable.runTaskTimer(GetCustomItem.getInstance(), 0L, 1L);
    }

    private void cleanupConnection(LassoConnection connection) {
        if (connection.task != null) {
            connection.task.cancel();
            connection.task = null;
        }

        Optional.ofNullable(activeTargetConnections.get(connection.targetUUID))
                .ifPresent(connections -> {
                    connections.remove(connection);
                    if (connections.isEmpty()) {
                        activeTargetConnections.remove(connection.targetUUID);
                    }
                });

        Optional.ofNullable(activeUserConnections.get(connection.userUUID))
                .ifPresent(connections -> {
                    connections.remove(connection);
                    if (connections.isEmpty()) {
                        activeUserConnections.remove(connection.userUUID);
                    }
                });

        if (connection.chicken != null && connection.chicken.isValid()) {
            connection.chicken.setLeashHolder(null);
            connection.chicken.remove();
        }
    }

    private void handleLassoEnd(Player player, Player target, LassoConnection connection) {
        if (Math.random() * 100 <= connection.item.getTeleportChance()) {
            target.teleport(player.getLocation());

            if (connection.item.getPullSound().isEnable()) {
                player.playSound(player.getLocation(), connection.item.getPullSound().getSound(), 1.0f, 1.0f);
                target.playSound(target.getLocation(), connection.item.getPullSound().getSound(), 1.0f, 1.0f);
            }

            MessageUtil.sendMessage(player, connection.item.getSuccessMessage());
            MessageUtil.sendMessage(target, connection.item.getSuccessMessage());
        } else {
            if (connection.item.getFailSound().isEnable()) {
                player.playSound(player.getLocation(), connection.item.getFailSound().getSound(), 1.0f, 1.0f);
                target.playSound(target.getLocation(), connection.item.getFailSound().getSound(), 1.0f, 1.0f);
            }

            MessageUtil.sendMessage(player, connection.item.getFailMessage());
            MessageUtil.sendMessage(target, connection.item.getFailMessage());
        }
    }

    private boolean isLassoChicken(Chicken chicken) {
        return activeTargetConnections.values().stream()
                .flatMap(Set::stream)
                .anyMatch(conn -> conn.chicken.equals(chicken));
    }

    public Player findPlayer(Player source, int maxDistance) {
        Location start = source.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        RayTraceResult result = source.getWorld().rayTraceEntities(
                start,
                direction,
                maxDistance,
                entity -> entity != source && entity instanceof Player
        );

        if (result != null && result.getHitEntity() instanceof Player targetPlayer) {
            return targetPlayer;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (activeUserConnections.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDropItem(EntityDropItemEvent event) {
        if (event.getEntity() instanceof Chicken chicken && isLassoChicken(chicken)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Chicken chicken && isLassoChicken(chicken)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Chicken chicken && isLassoChicken(chicken)) {
            event.setCancelled(true);
        }
    }

    private void cleanupAllConnections(UUID userUUID, UUID targetUUID) {
        Optional.ofNullable(activeUserConnections.get(userUUID))
                .ifPresent(connections -> {
                    List<LassoConnection> toRemove = connections.stream()
                            .filter(conn -> conn.targetUUID.equals(targetUUID))
                            .toList();
                    toRemove.forEach(this::cleanupConnection);
                });

        Optional.ofNullable(activeTargetConnections.get(targetUUID))
                .ifPresent(connections -> {
                    List<LassoConnection> toRemove = connections.stream()
                            .filter(conn -> conn.userUUID.equals(userUUID))
                            .toList();
                    toRemove.forEach(this::cleanupConnection);
                });
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Optional.ofNullable(activeUserConnections.get(player.getUniqueId()))
                .ifPresent(connections -> new HashSet<>(connections)
                        .forEach(this::cleanupConnection));
        Optional.ofNullable(activeTargetConnections.get(player.getUniqueId()))
                .ifPresent(connections -> new HashSet<>(connections)
                        .forEach(this::cleanupConnection));

        activeUserConnections.remove(player.getUniqueId());
        activeTargetConnections.remove(player.getUniqueId());
    }
}