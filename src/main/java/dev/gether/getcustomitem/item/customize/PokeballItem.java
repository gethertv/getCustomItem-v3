package dev.gether.getcustomitem.item.customize;

import dev.gether.getcustomitem.GetCustomItem;
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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonTypeName("poke_ball")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class PokeballItem extends CustomItem {
    private double speedPokeball;
    private double maxRange;
    private double chance;
    private String permissionByPass;
    private boolean safeTeleport;

    @Override
    protected Map<String, String> replacementValues() {
        return Map.of("{chance}",
                String.valueOf(this.chance));
    }

    public Arrow throwEntity(Player player) {
        Vector direction = player.getEyeLocation().getDirection().multiply(this.speedPokeball);
        Arrow arrow = player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), Arrow.class);
        arrow.setShooter(player);
        arrow.setMetadata(getItemID(), new FixedMetadataValue(GetCustomItem.getInstance(), "true"));
        arrow.setVelocity(direction);
        arrow.setInvulnerable(true);
        maxRangeTask(arrow, this.maxRange);
        return arrow;
    }

    @JsonIgnore
    public static PokeballItem createDefaultItem() {
        return PokeballItem.builder()
                .enabled(true)
                .itemID("poke_ball")
                .categoryName("poke_ball")
                .usage(-1)
                .item(Item.builder()
                        .amount(1)
                        .material(Material.HEART_OF_THE_SEA)
                        .name("#8c19ffPokeball")
                        .lore(new ArrayList<>(
                                List.of(
                                        "&7",
                                        "#a74fff× Hit the player and get",
                                        "#a74fff  a chance to teleport them to you",
                                        "#a74fff  Chance: &f{chance}",
                                        "&7",
                                        "&7• Usage: #a74fff{usage}",
                                        "&7"
                                )
                        ))
                        .unbreakable(true)
                        .glow(true)
                        .build())
                .itemType(ItemType.POKE_BALL)
                .cooldown(10)
                .permissionBypass("getcustomitem.pokeball.bypass")
                .soundConfig(SoundConfig.builder()
                        .enable(true)
                        .sound(Sound.BLOCK_ANVIL_BREAK)
                        .build())
                .cooldownMessage(true)
                .speedPokeball(3)
                .chance(50)
                .safeTeleport(true)
                .maxRange(25)
                .permissionByPass("pokeball.bypass")
                .build();
    }

}
