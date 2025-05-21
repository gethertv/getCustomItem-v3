package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.GetCustomItem;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.metadata.MetadataStorage;
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
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("egg_throw_it")
@SuperBuilder
@NoArgsConstructor
public class EggThrowItItem extends CustomItem {

    private double shootPower;
    private double chance;
    private String permissionByPass;
    private Item throwingItem;
    private boolean fly;

    @JsonIgnore
    private transient ItemStack throwingItemStack;

    private double maxRange;


    @Override
    public void init() {
        super.init();
        throwingItemStack = throwingItem.getItemStack();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{power-push}", String.valueOf(shootPower),
                "{chance}", String.valueOf(chance)
        );
    }

    public void throwEgg(Player player) {
        final Vector direction = player.getEyeLocation().getDirection().multiply(shootPower);
        Egg egg = player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), Egg.class);
        egg.setItem(throwingItemStack);
        egg.setShooter(player);
        egg.setVelocity(direction);
        egg.setInvulnerable(true);
        egg.setMetadata(MetadataStorage.PROJECTILE_METADATA, new FixedMetadataValue(GetCustomItem.getInstance(), getItemID()));
        maxRangeTask(egg, maxRange);
    }

    @JsonIgnore
    public static EggThrowItItem createDefaultItem() {
        return EggThrowItItem.builder()
                .enabled(true)
                .itemID("egg_throw_it")
                .categoryName("egg_throw_it_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.EGG)
                        .name("#9e9e9eJump egg")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#666666× Throw egg thrue",
                                        "#666666  the player hit him and",
                                        "#666666  throw up it",
                                        "#666666  chance: &f{chance}",
                                        "#666666  power: &f{power-push}",
                                        "&7",
                                        "&7• Usage: #666666{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.EGG_THROW_UP)
                .cooldown(10)
                .permissionBypass("getcustomitem.eggthrowup.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .shootPower(1.5)
                .chance(100)
                .maxRange(25.0)
                .cooldownMessage(true)
                .permissionByPass("egg.throw.bypass")
                .fly(true)
                .throwingItem(Item.builder()
                        .amount(1)
                        .material(Material.EGG)
                        .name("Egg")
                        .lore(new ArrayList<>())
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .build();
    }
}
