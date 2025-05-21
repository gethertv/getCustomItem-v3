package dev.gether.getcustomitem.listener;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.event.CustomItemInteractEvent;
import dev.gether.getcustomitem.event.CustomProjectileHitEvent;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.SnowgunDyngusItem;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.utils.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SnowgunDyngusListener extends AbstractCustomItemListener<SnowgunDyngusItem> {

    private final Plugin plugin;
    private final Map<UUID, BukkitTask> stunnedPlayers = new HashMap<>();

    public SnowgunDyngusListener(Plugin plugin, ItemManager itemManager, CooldownManager cooldownManager, FileManager fileManager) {
        super(itemManager, cooldownManager, fileManager);
        this.plugin = plugin;
    }

    @EventHandler
    public void onCustomItemInteraction(CustomItemInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getCustomItem() instanceof SnowgunDyngusItem snowgunItem)) return;
        if (!snowgunItem.isEnabled()) return;

        Player player = event.getPlayer();

        if (!canUseItem(player, snowgunItem, event.getItemStack(), event.getEquipmentSlot())) return;

        snowgunItem.playSound(player.getLocation());
        Snowball snowball = snowgunItem.throwSnowball(player);

        snowball.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(plugin, snowgunItem.getItemID()));
    }

    @EventHandler
    public void onCustomProjectileHit(CustomProjectileHitEvent event) {
        if(event.isCancelled()) return;
        Projectile projectile = event.getProjectile();
        if (!(projectile.getShooter() instanceof Player shooter)) return;
        if (!(event.getHitEntity() instanceof Player hitPlayer)) return;

        if (!(event.getCustomItem() instanceof SnowgunDyngusItem snowgunItem)) return;
        if (!snowgunItem.isEnabled()) return;

        if (hitPlayer.hasPermission(snowgunItem.getPermissionBypass()))
            return;

        if (GetCustomItem.getInstance().getHookManager().getEssentialsXHook().hasFlyPermission(shooter) && !(shooter.hasPermission(snowgunItem.getPermissionBypass())))
            return;

        hitPlayer.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                snowgunItem.getBlindnessDuration() * 20,
                2,
                false,
                true,
                true
        ));

        hitPlayer.setMetadata("stunned", new FixedMetadataValue(plugin, true));

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (hitPlayer.isOnline()) {
                    hitPlayer.removeMetadata("stunned", plugin);
                    stunnedPlayers.remove(hitPlayer.getUniqueId());
                    MessageUtil.sendMessage(hitPlayer, "&aZnów możesz się poruszać!");
                }
            }
        }.runTaskLater(plugin, snowgunItem.getStunDuration() * 20L);

        stunnedPlayers.put(hitPlayer.getUniqueId(), task);

        snowgunItem.notifyYourself(shooter);
        snowgunItem.notifyOpponents(hitPlayer);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("stunned")) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (stunnedPlayers.containsKey(playerUUID)) {
            stunnedPlayers.get(playerUUID).cancel();
            stunnedPlayers.remove(playerUUID);
            player.removeMetadata("stunned", plugin);
        }
    }
}