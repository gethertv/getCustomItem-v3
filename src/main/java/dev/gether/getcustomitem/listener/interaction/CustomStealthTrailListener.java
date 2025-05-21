package dev.gether.getcustomitem.listener.interaction;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.customize.CustomStealthTrailItem;
import dev.gether.getcustomitem.listener.AbstractCustomItemListener;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.utils.EntityUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CustomStealthTrailListener extends AbstractCustomItemListener<CustomStealthTrailItem> {

    private final Map<UUID, BukkitTask> activeStealthTasks = new HashMap<>();
    private final Map<UUID, BukkitTask> activeParticleTasks = new HashMap<>();
    private final Map<UUID, LinkedList<Location>> playerTrails = new HashMap<>();
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public CustomStealthTrailListener(ItemManager itemManager,
                                      CooldownManager cooldownManager,
                                      FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof CustomStealthTrailItem stealthItem)) return;
        if (!stealthItem.isEnabled()) return;

        Player player = event.getPlayer();
        if (!canUseItem(player, stealthItem, event.getItemStack(), event.getEquipmentSlot())) return;

        removeStealthEffect(player);

        applyStealthEffect(player, stealthItem);
    }

    private void applyStealthEffect(Player player, CustomStealthTrailItem item) {
        GetCustomItem.getInstance().getServer().getOnlinePlayers().forEach(p -> {
            if (p != player) {
                p.hidePlayer(GetCustomItem.getInstance(), player);
            }
        });

        hiddenPlayers.add(player.getUniqueId());
        player.setMetadata(MetadataStorage.CUSTOM_ITEM,
                new FixedMetadataValue(GetCustomItem.getInstance(), item.getItemID()));

        playerTrails.put(player.getUniqueId(), new LinkedList<>());

        BukkitTask particleTask = item.runTrailParticles(playerTrails.get(player.getUniqueId()));
        if (particleTask != null) {
            activeParticleTasks.put(player.getUniqueId(), particleTask);
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                removeStealthEffect(player);
            }
        }.runTaskLater(GetCustomItem.getInstance(), item.getDuration() * 20L);

        activeStealthTasks.put(player.getUniqueId(), task);

        item.playSound(player.getLocation());
        item.notifyYourself(player);

        EntityUtil.findNearbyEntities(player.getLocation(), 5, Player.class).stream()
                .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                .forEach(item::notifyOpponents);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!hiddenPlayers.contains(player.getUniqueId())) return;
        if (!player.hasMetadata(MetadataStorage.CUSTOM_ITEM)) return;

        if (event.getFrom().distance(event.getTo()) > 0.2) {
            LinkedList<Location> trail = playerTrails.get(player.getUniqueId());
            if (trail != null) {
                trail.add(event.getFrom().clone());

                String itemKey = player.getMetadata(MetadataStorage.CUSTOM_ITEM).get(0).asString();
                Optional<CustomStealthTrailItem> itemOpt = itemManager.findCustomItemByKey(itemKey)
                        .filter(i -> i instanceof CustomStealthTrailItem)
                        .map(i -> (CustomStealthTrailItem) i);

                if (itemOpt.isPresent()) {
                    CustomStealthTrailItem item = itemOpt.get();
                    while (!trail.isEmpty() &&
                            trail.getFirst().distance(trail.getLast()) > item.getTrailLength()) {
                        trail.removeFirst();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (hiddenPlayers.contains(attacker.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    private void removeStealthEffect(Player player) {
        BukkitTask stealthTask = activeStealthTasks.remove(player.getUniqueId());
        if (stealthTask != null) {
            stealthTask.cancel();
        }

        BukkitTask particleTask = activeParticleTasks.remove(player.getUniqueId());
        if (particleTask != null) {
            particleTask.cancel();
        }

        playerTrails.remove(player.getUniqueId());

        if (hiddenPlayers.remove(player.getUniqueId())) {
            GetCustomItem.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                if (p != player) {
                    p.showPlayer(GetCustomItem.getInstance(), player);
                }
            });
        }

        if (player.hasMetadata(MetadataStorage.CUSTOM_ITEM)) {
            player.removeMetadata(MetadataStorage.CUSTOM_ITEM, GetCustomItem.getInstance());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();
        hiddenPlayers.forEach(uuid -> {
            Player stealthedPlayer = GetCustomItem.getInstance().getServer().getPlayer(uuid);
            if (stealthedPlayer != null && stealthedPlayer != joiningPlayer) {
                joiningPlayer.hidePlayer(GetCustomItem.getInstance(), stealthedPlayer);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeStealthEffect(event.getPlayer());
    }
}
