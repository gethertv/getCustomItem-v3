package dev.gether.getcustomitem.item.customize.bow;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.utils.ParticlesUtil;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeName("ender_bow")
@SuperBuilder
@NoArgsConstructor
@Getter
public class EnderBowItem extends CustomItem {

    private ParticleConfig particleConfig;
    private double maxRange;
    private Material projectileMaterial;
    private boolean autoReload = true;

    public void runParticles(Entity arrow) {
        if (!particleConfig.isEnable()) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isOnGround() || !arrow.isValid()) {
                    this.cancel();
                    return;
                }
                ParticlesUtil.spawnParticles(arrow, particleConfig);
            }
        }.runTaskTimerAsynchronously(GetCustomItem.getInstance(), 0L, 0L);
    }

    public void teleportPlayer(Player player, Arrow arrow) {
        player.teleport(arrow.getLocation());
        arrow.remove();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{max_range}", String.valueOf(maxRange)
        );
    }

    @JsonIgnore
    public static EnderBowItem createDefaultItem() {
        return EnderBowItem.builder()
                .enabled(true)
                .itemID("ender_bow")
                .categoryName("bows")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.BOW)
                        .name("&5Ender Bow")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff6666× Teleports you to the arrow's landing spot",
                                        "#ff6666× Maximum range: &f{max_range} &7blocks",
                                        "&7",
                                        "&7• Usage: #ff6666{usage}",
                                        "&7"
                                )
                        ))
                        .enchantments(new HashMap<>(Map.of(
                                Enchantment.ARROW_INFINITE, 1
                        )))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.ENDER_BOW)
                .cooldown(10)
                .permissionBypass("getcustomitem.enderbow.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .particle(Particle.PORTAL)
                        .count(10)
                        .offSetX(0.1)
                        .offSetY(0.1)
                        .offSetZ(0.1)
                        .extra(0.1)
                        .build())
                .maxRange(50.0)
                .cooldownMessage(true)
                .build();
    }
}