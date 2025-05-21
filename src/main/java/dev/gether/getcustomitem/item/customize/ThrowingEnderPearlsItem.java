package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getutils.models.Item;
import dev.gether.getutils.models.sound.SoundConfig;
import dev.gether.shaded.jackson.annotation.JsonIgnore;
import dev.gether.shaded.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("throwing_ender_pearls")
@SuperBuilder
@NoArgsConstructor
public class ThrowingEnderPearlsItem extends CustomItem {
    private float shootSpeed;
    private double maxRange;

    private Item throwingItem;

    @JsonIgnore
    private transient ItemStack throwingItemStack;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{usage}", String.valueOf(usage)
        );
    }

    @Override
    public void init() {
        super.init();
        throwingItemStack = throwingItem.getItemStack();
    }

    public void throwEnderPearls(Player player) {
        final Vector direction = player.getEyeLocation().getDirection().multiply(shootSpeed);
        EnderPearl enderPearl = player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), EnderPearl.class);
        enderPearl.setItem(throwingItemStack);
        enderPearl.setShooter(player);
        enderPearl.setVelocity(direction);
        enderPearl.setInvulnerable(true);
        maxRangeTask(enderPearl, maxRange);
    }

    @JsonIgnore
    public static ThrowingEnderPearlsItem createDefaultItem() {
        return ThrowingEnderPearlsItem.builder()
                .enabled(true)
                .itemID("throwing_ender_pearls")
                .categoryName("throwing_ender_pearls_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.DIAMOND_SWORD)
                        .name("#9e9e9eDragon sword")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Right click to throw",
                                        "#666666× ender pearls",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .enchantments(new HashMap<>(Map.of(
                                Enchantment.FIRE_ASPECT, 2,
                                Enchantment.DAMAGE_ALL, 6
                        )))
                        .unbreakable(true)
                        .glow(false)
                        .build())
                .itemType(ItemType.THROWING_ENDER_PEARLS)
                .cooldown(10)
                .permissionBypass("getcustomitem.throwingenderpearls.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .shootSpeed(1.5f)
                .maxRange(25)
                .throwingItem(Item.builder()
                        .amount(1)
                        .material(Material.ENDER_PEARL)
                        .name("&7")
                        .lore(new ArrayList<>())
                        .enchantments(new HashMap<>())
                        .build())
                .build();
    }
}
