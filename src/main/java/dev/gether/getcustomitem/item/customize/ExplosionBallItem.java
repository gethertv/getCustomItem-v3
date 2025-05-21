package dev.gether.getcustomitem.item.customize;


import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.ItemVariant;
import dev.gether.getcustomitem.metadata.MetadataStorage;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

@Getter
@Setter
@JsonTypeName("explosion_ball")
@SuperBuilder
@NoArgsConstructor
public class ExplosionBallItem extends CustomItem {
    private SoundConfig explosionSound;
    private float speed;
    private double maxRange;
    private Set<Material> blockedMaterials;
    private float explosionPower;
    private boolean setFire;
    private boolean breakBlocks;
    private boolean igniteFireball;
    private int destroyDurability;
    private int blockExplosionSize;
    private boolean hookTempBlocks;
    private float yield = 1.0f;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{usage}", String.valueOf(usage),
                "{explosion-power}", String.format(Locale.US, "%.2f", explosionPower),
                "{destroy-durability}", String.format(Locale.US, "%d", destroyDurability)
        );
    }

    public void throwExplosionBall(Player player) {
        final Vector direction = player.getEyeLocation().getDirection().multiply(speed);
        Fireball fireball = player.getWorld().spawn(
                player.getEyeLocation()
                        .add(
                                direction.getX(),
                                direction.getY(),
                                direction.getZ()
                        ),
                Fireball.class
        );

        fireball.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(GetCustomItem.getInstance(), getItemID()));
        fireball.setShooter(player);
        fireball.setVelocity(direction);
        fireball.setInvulnerable(true);
        fireball.setIsIncendiary(false);
        if (!igniteFireball)
            fireball.setFireTicks(0);

        fireball.setYield(yield);

        maxRangeTask(fireball, maxRange);
    }

    @JsonIgnore
    public void playExplodeSound(Location location) {
        // check sound is enabled
        if(!explosionSound.isEnable())
            return;

        location.getWorld().playSound(location, explosionSound.getSound(), 1F, 1F);
    }

    @JsonIgnore
    public static ExplosionBallItem createItem(ItemVariant variant) {
        return switch (variant) {
            case EXAMPLE_1 -> ExplosionBallItem.builder()
                    .enabled(true)
                    .itemID("explosion_durability")
                    .categoryName("explosion_durability_category")
                    .usage(-1)
                    .item(Item.builder()
                            .amount(1)
                            .material(Material.FIRE_CHARGE)
                            .name("#f2ff69Explosion Ball of Durability")
                            .lore(new ArrayList<>(
                                    List.of(
                                            "&7",
                                            "#beff69Explosion Power: {explosion-power}",
                                            "#beff69Durability Cost: {destroy-durability}",
                                            "&7",
                                            "&7• Usage: #beff69{usage}",
                                            "&7"
                                    )
                            ))
                            .unbreakable(true)
                            .glow(true)
                            .build())
                    .itemType(ItemType.EXPLOSION_BALL)
                    .cooldown(5)
                    .permissionBypass("getcustomitem.explosionitemdurability.bypass")
                    .soundConfig(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .cooldownMessage(true)
                    .explosionSound(SoundConfig.builder()
                            .enable(true)
                            .sound(Sound.BLOCK_ANVIL_BREAK)
                            .build())
                    .speed(1.5f)
                    .maxRange(25)
                    .blockedMaterials(new HashSet<>(
                            Set.of(Material.BEDROCK)
                    ))
                    .explosionPower(5f)
                    .setFire(false)
                    .breakBlocks(false)
                    .igniteFireball(false)
                    .destroyDurability(10)
                    .build();

            default -> createDefaultItem();
        };
    }

    @JsonIgnore
    public static ExplosionBallItem createDefaultItem() {
        return ExplosionBallItem.builder()
                .enabled(true)
                .itemID("explosion_durability")
                .categoryName("explosion_durability_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.FIRE_CHARGE)
                        .name("#f2ff69Explosion Ball of Durability")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#beff69Explosion Power: {explosion-power}",
                                        "&7",
                                        "&7• Usage: #beff69{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.EXPLOSION_BALL)
                .cooldown(5)
                .permissionBypass("getcustomitem.explosionitemdurability.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
//                .defaultNotifications()
                .speed(1.5f)
                .maxRange(25)
                .blockedMaterials(new HashSet<>(
                        Set.of(Material.BEDROCK)
                ))
                .explosionPower(5f)
                .setFire(false)
                .breakBlocks(true)
                .igniteFireball(true)
                .destroyDurability(0)
                .build();
    }
}