package dev.gether.getcustomitem.item.customize;

import com.sk89q.worldguard.protection.flags.Flags;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.utils.WorldGuardUtil;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.particles.DustOptions;
import dev.gether.getutils.models.particles.ParticleConfig;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.getutils.utils.ParticlesUtil;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("cobweb_grenade")
@SuperBuilder
@NoArgsConstructor
public class CobwebGrenade extends CustomItem {
    private ParticleConfig particleConfig;
    private int radiusX;
    private int radiusY;
    private double multiply;
    private double heightVelocity;

    public void spawnCobweb(Location location) {
        for (int x = -radiusX + 1; x < radiusX; x++) {
            for (int y = -radiusY + 1; y < radiusY; y++) {
                for (int z = -radiusX + 1; z < radiusX; z++) {
                    Location tempLoc = location.clone().add(x, y, z);
                    if (WorldGuardUtil.isDeniedFlag(tempLoc, null, Flags.BLOCK_PLACE)) {
                        continue;
                    }
                    Block block = tempLoc.getBlock();
                    if (block.getType() == Material.AIR) {
                        block.setType(Material.COBWEB);
                        GetCustomItem.getInstance().getHookManager().placeBlock(block.getLocation());
                    }
                }
            }
        }
    }
    public void runParticles(ThrownPotion thrownPotion) {

        // check the particles is enabled
        if (!particleConfig.isEnable())
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                // check if the potion has landed or is removed
                if (thrownPotion.isOnGround() || !thrownPotion.isValid()) {
                    this.cancel();
                    return;
                }

                ParticlesUtil.spawnParticles(thrownPotion, particleConfig);

            }
        }.runTaskTimerAsynchronously(GetCustomItem.getInstance(), 0L, 0L);
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{radius-x}", String.valueOf(radiusX),
                "{radius-y}", String.valueOf(radiusY)
        );
    }

    @JsonIgnore
    public static CobwebGrenade createDefaultItem() {
        return CobwebGrenade.builder()
                .enabled(true)
                .itemID("cobweb_grenade")
                .categoryName("cobweb_category")
                .usage(5)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.SPLASH_POTION)
                        .name("#ff004cCobweb grenade")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#ff175c× Throw the grande to create",
                                        "#ff175c  a trap with cobweb &7(&f{radius-x}&7x&f{radius-y}&8) ",
                                        "&7",
                                        "&7• Usage: #ff004c{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.COBWEB_GRENADE)
                .cooldown(10)
                .permissionBypass("getcustomitem.grenadecobweb.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .particleConfig(ParticleConfig.builder()
                        .enable(true)
                        .dustOptions(new DustOptions(210, 255, 97, 5))
                        .particle(Particle.REDSTONE)
                        .build())
                .radiusX(2)
                .radiusY(2)
                .multiply(0.95)
                .heightVelocity(3)
                .build();
    }


}
