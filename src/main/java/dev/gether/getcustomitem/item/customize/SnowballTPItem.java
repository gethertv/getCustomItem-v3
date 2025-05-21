package dev.gether.getcustomitem.item.customize;

import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("snowball_tp")
@SuperBuilder
@NoArgsConstructor
public class SnowballTPItem extends CustomItem {
    private float shootSpeed;
    private double shootRange;
    @JsonIgnore
    private transient ItemStack throwingItemStack;
    private Item snowball;
    private boolean safeTeleport;

    @Override
    public void init() {
        super.init();
        this.throwingItemStack = snowball.getItemStack();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{usage}", String.valueOf(usage)
        );
    }

    public Snowball throwSnowball(Player player) {
        final Vector direction = player.getEyeLocation().getDirection().multiply(shootSpeed);
        Snowball snowball = player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), Snowball.class);
        snowball.setItem(throwingItemStack);
        snowball.setShooter(player);
        snowball.setVelocity(direction);
        snowball.setInvulnerable(true);
        maxRangeTask(snowball, shootRange);
        return snowball;
    }

    @JsonIgnore
    public static SnowballTPItem createDefaultItem() {
        return SnowballTPItem.builder()
                .enabled(true)
                .itemID("snowball_tp")
                .categoryName("snowball_tp_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.SNOWBALL)
                        .name("#8c19ffSnowball TP")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#a74fff× Hit the player",
                                        "#a74fff  and swap locations",
                                        "#a74fff  with them",
                                        "&7",
                                        "&7• Usage: #a74fff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SNOWBALL_TP)
                .cooldown(10)
                .permissionBypass("getcustomitem.snowballtp.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .shootSpeed(2.5f)
                .shootRange(25)
                .snowball(Item.builder()
                        .amount(1)
                        .material(Material.SNOWBALL)
                        .name("&7")
                        .lore(new ArrayList<>())
                        .build())
                .cooldownMessage(true)
                .safeTeleport(true)
                .build();
    }
}
