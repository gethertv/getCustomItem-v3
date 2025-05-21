package dev.gether.getcustomitem.item;

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
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonTypeName("snowgun_dyngus")
@SuperBuilder
@NoArgsConstructor
public class SnowgunDyngusItem extends CustomItem {
    private float shootSpeed;
    private double shootRange;
    private int stunDuration;
    private int blindnessDuration;
    
    @JsonIgnore
    private transient ItemStack throwingItemStack;
    private Item snowball;

    @Override
    public void init() {
        super.init();
        this.throwingItemStack = snowball.getItemStack();
    }

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of(
                "{usage}", String.valueOf(usage),
                "{stun}", String.valueOf(stunDuration),
                "{blind}", String.valueOf(blindnessDuration)
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
    public static SnowgunDyngusItem createDefaultItem() {
        return SnowgunDyngusItem.builder()
                .enabled(true)
                .itemID("snowgun_dyngus")
                .categoryName("easter_category")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.STICK)
                        .name("#00aaffPistolet na Śnieżki §f(Śmingus Dyngus)")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#00aaffTrafienie gracza:",
                                        "#00aaffᐅ Unieruchomienie: &f{stun}s",
                                        "#00aaffᐅ Oślepienie: &f{blind}s",
                                        "&7",
                                        "&dŚmigus-dyngus to ludowy zwyczaj",
                                        "&doblewania wodą innych osób w",
                                        "&dPoniedziałek Wielkanocny",
                                        "&7",
                                        "&7• Użycia: #00aafff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.SNOWGUN_DYNGUS)
                .cooldown(15)
                .permissionBypass("getcustomitem.snowgundyngus.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.ENTITY_PLAYER_SPLASH)
                        .build())
                .shootSpeed(2.0f)
                .shootRange(20)
                .stunDuration(3) // 3 sekundy stunu
                .blindnessDuration(5) // 5 sekund oślepienia
                .snowball(Item.builder()
                        .amount(1)
                        .material(Material.SNOWBALL)
                        .name("#00aaffMokra Śnieżka")
                        .lore(new ArrayList<>())
                        .build())
                .cooldownMessage(true)
                .build();
    }
}