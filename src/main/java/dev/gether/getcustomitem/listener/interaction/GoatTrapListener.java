package dev.gether.getcustomitem.listener.interaction;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemDamageEvent;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.GoatTrapItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.utils.EntityUtil;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GoatTrapListener extends AbstractCustomItemListener<GoatTrapItem> {

    private final Map<UUID, ActiveTrap> activeTraps = new HashMap<>();
    private final Set<UUID> trappedPlayers = new HashSet<>();
    private final Map<UUID, UUID> droppedItemsOwnership = new HashMap<>();

    @Data
    private static class ActiveTrap {
        private final UUID ownerUUID;
        private final Set<UUID> trappedPlayers;
        private final List<WorldBorder> borders;
        private final BukkitRunnable task;
        private final GoatTrapItem item;
        private final Location center;
        private final Map<UUID, Boolean> playerFlightStates = new HashMap<>();
    }

    public GoatTrapListener(ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (!(event.getCustomItem() instanceof GoatTrapItem goatTrapItem)) return;
        if (!goatTrapItem.isEnabled()) return;
        if (goatTrapItem.isOnHitMode()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, goatTrapItem, event.getItemStack(), event.getEquipmentSlot())) return;

        goatTrapItem.playSound(player.getLocation());
        goatTrapItem.notifyYourself(player);

        List<Player> targets = findValidTargets(player, goatTrapItem.getRadius());
        if (targets.isEmpty()) return;

        createTrap(player, targets, goatTrapItem);
    }

    @EventHandler
    public void onCustomItemDamage(CustomItemDamageEvent event) {
        if (!(event.getCustomItem() instanceof GoatTrapItem goatTrapItem)) return;
        if (!goatTrapItem.isEnabled()) return;
        if (!goatTrapItem.isOnHitMode()) return;

        Player damager = event.getDamager();
        Player victim = event.getVictim();

        if (!canUseItem(damager, goatTrapItem, event.getItemStack(), event.getEquipmentSlot())) return;
        if (WorldGuardUtil.isDeniedFlag(victim.getLocation(), victim, Flags.PVP)) return;

        goatTrapItem.playSound(damager.getLocation());
        goatTrapItem.notifyYourself(damager);

        List<Player> targets = Arrays.asList(victim, damager);
        createTrap(damager, targets, goatTrapItem);
    }

    private List<Player> findValidTargets(Player source, double radius) {
        return EntityUtil.findNearbyEntities(source.getLocation(), radius, Player.class).stream()
                .filter(target -> !target.hasMetadata("NPC"))
                .filter(target -> !WorldGuardUtil.isDeniedFlag(target.getLocation(), target, Flags.PVP))
                .toList();
    }

    private void createTrap(Player owner, List<Player> targets, GoatTrapItem item) {
        Location center = owner.getLocation();

        List<WorldBorder> borders = new ArrayList<>();
        for (Player target : targets) {
            WorldBorder playerBorder = Bukkit.createWorldBorder();
            playerBorder.setCenter(center);
            playerBorder.setSize(item.getRadius() * 2);
            playerBorder.setWarningDistance(0);

            target.setWorldBorder(playerBorder);
            borders.add(playerBorder);
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                removeTrap(owner.getUniqueId());
            }
        };
        task.runTaskLater(GetCustomItem.getInstance(), item.getDuration() * 20L);

        ActiveTrap trap = new ActiveTrap(
                owner.getUniqueId(),
                new HashSet<>(targets.stream().map(Player::getUniqueId).toList()),
                borders,
                task,
                item,
                center
        );

        for (Player target : targets) {
            trappedPlayers.add(target.getUniqueId());
            target.teleport(ensureInBorder(target.getLocation(), center, item.getRadius()));

            if (item.isBlockElytra()) {
                trap.getPlayerFlightStates().put(target.getUniqueId(), target.getAllowFlight());
                target.setGliding(false);
                target.setAllowFlight(false);
            }

            updateVisibility(target, targets);
            if (!target.getName().equalsIgnoreCase(owner.getName())) {
                item.notifyOpponents(target);
            }
        }

        activeTraps.put(owner.getUniqueId(), trap);
    }

    private Location ensureInBorder(Location playerLoc, Location center, double radius) {
        double distance = playerLoc.distance(center);
        if (distance > radius) {
            double ratio = radius / distance;
            double newX = center.getX() + (playerLoc.getX() - center.getX()) * ratio;
            double newZ = center.getZ() + (playerLoc.getZ() - center.getZ()) * ratio;
            return new Location(playerLoc.getWorld(), newX, playerLoc.getY(), newZ, playerLoc.getYaw(), playerLoc.getPitch());
        }
        return playerLoc;
    }

    private void updateVisibility(Player player, List<Player> visiblePlayers) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (visiblePlayers.contains(otherPlayer)) {
                player.showPlayer(GetCustomItem.getInstance(), otherPlayer);
                otherPlayer.showPlayer(GetCustomItem.getInstance(), player);
            } else {
                player.hidePlayer(GetCustomItem.getInstance(), otherPlayer);
                otherPlayer.hidePlayer(GetCustomItem.getInstance(), player);
            }
        }
    }

    private void removeTrap(UUID ownerUUID) {
        ActiveTrap trap = activeTraps.remove(ownerUUID);
        if (trap == null) return;

        trap.getCenter().getWorld().getEntitiesByClass(Item.class).forEach(item -> {
            if (droppedItemsOwnership.containsKey(item.getUniqueId())) {
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.showEntity(GetCustomItem.getInstance(), item)
                );
            }
        });

        droppedItemsOwnership.clear();

        for (UUID targetUUID : trap.getTrappedPlayers()) {
            trappedPlayers.remove(targetUUID);
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null && target.isOnline()) {
                target.setWorldBorder(target.getWorld().getWorldBorder());

                if (trap.getItem().isBlockElytra()) {
                    Boolean previousFlightState = trap.getPlayerFlightStates().get(targetUUID);
                    if (previousFlightState != null) {
                        target.setAllowFlight(previousFlightState);
                    }
                }

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showPlayer(GetCustomItem.getInstance(), target);
                    target.showPlayer(GetCustomItem.getInstance(), onlinePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!trappedPlayers.contains(player.getUniqueId())) return;

        ActiveTrap trap = findTrapContainingPlayer(player.getUniqueId());
        if (trap != null && trap.getItem().isBlockElytra() && event.isGliding()) {
            event.setCancelled(true);
            player.setGliding(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!trappedPlayers.contains(event.getPlayer().getUniqueId())) return;

        ActiveTrap trap = findTrapContainingPlayer(event.getPlayer().getUniqueId());
        if (trap != null && trap.getItem().isBlockElytra()) {
            event.setCancelled(true);
            event.getPlayer().setFlying(false);
            event.getPlayer().setAllowFlight(false);
        }
    }

    @EventHandler
    public void onItemPickup(org.bukkit.event.player.PlayerPickupItemEvent event) {
        Item item = event.getItem();
        UUID itemTrapOwner = droppedItemsOwnership.get(item.getUniqueId());

        if (itemTrapOwner != null) {
            ActiveTrap trap = activeTraps.get(itemTrapOwner);
            if (trap != null) {
                if (!trap   .getTrappedPlayers().contains(event.getPlayer().getUniqueId())) {
                    event.setCancelled(true);
                }
            } else {
                droppedItemsOwnership.remove(item.getUniqueId());
            }
        }
    }



    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!trappedPlayers.contains(event.getPlayer().getUniqueId())) return;

        ActiveTrap trap = findTrapContainingPlayer(event.getPlayer().getUniqueId());
        if (trap == null) return;

        if (event.getItem() != null && trap.getItem().getBlockedItems().contains(event.getItem().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (activeTraps.containsKey(playerUUID)) {
            removeTrap(playerUUID);
        }

        trappedPlayers.remove(playerUUID);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        UUID playerUUID = deadPlayer.getUniqueId();

        ActiveTrap trap = findTrapContainingPlayer(playerUUID);
        if (trap != null) {
            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            event.getDrops().clear();

            drops.forEach(itemStack -> {
                Item droppedItem = deadPlayer.getWorld().dropItemNaturally(deadPlayer.getLocation(), itemStack);
                droppedItemsOwnership.put(droppedItem.getUniqueId(), trap.getOwnerUUID()); // Track item ownership

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.hideEntity(GetCustomItem.getInstance(), droppedItem);
                }

                trap.getTrappedPlayers().forEach(uuid -> {
                    Player trapPlayer = Bukkit.getPlayer(uuid);
                    if (trapPlayer != null && trapPlayer.isOnline()) {
                        trapPlayer.showEntity(GetCustomItem.getInstance(), droppedItem);
                    }
                });
            });

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(GetCustomItem.getInstance(), deadPlayer);
                deadPlayer.showPlayer(GetCustomItem.getInstance(), onlinePlayer);
            }
        }

        if (activeTraps.containsKey(playerUUID)) {
            removeTrap(playerUUID);
        }

        trappedPlayers.remove(playerUUID);
    }

    private ActiveTrap findTrapContainingPlayer(UUID playerUUID) {
        return activeTraps.values().stream()
                .filter(trap -> trap.getTrappedPlayers().contains(playerUUID))
                .findFirst()
                .orElse(null);
    }
}