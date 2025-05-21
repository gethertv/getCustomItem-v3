package dev.gether.getcustomitem.file;

import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemVariant;
import dev.gether.getcustomitem.item.SnowgunDyngusItem;
import dev.gether.getcustomitem.item.customize.*;
import dev.gether.getcustomitem.item.customize.bow.CrossBowItem;
import dev.gether.getcustomitem.item.customize.bow.CupidBowItem;
import dev.gether.getcustomitem.item.customize.bow.EnderBowItem;
import dev.gether.getcustomitem.item.customize.itemtier.ItemTier;
import dev.gether.getcustomitem.item.customize.resurrect.ConfusionTotemItem;
import dev.gether.getcustomitem.item.customize.resurrect.MagicTotemItem;
import dev.gether.getcustomitem.item.customize.resurrect.ResurrectGearItem;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;


public class DefaultItem {

    @Getter
    private Set<CustomItem> customItems = new HashSet<>(
            Set.of(
                    AntiElytraHookItem.createDefaultItem(),
                    AntiCobweb.createDefaultItem(),
                    BearFurItem.createDefaultItem(),
                    BubbleTrapItem.createDefaultItem(),
                    ChangeYawItem.createDefaultItem(),
                    CobwebGrenade.createDefaultItem(),
                    CrossBowItem.createDefaultItem(),
                    CupidBowItem.createDefaultItem(),
                    CustomSphereItem.createDefaultItem(),
                    DeathTotemItem.createDefaultItem(),
                    DropToInventoryItem.createDefaultItem(),
                    DynamicAreaPickaxeItem.createDefaultItem(),
                    DropToInventoryItem.createDefaultItem(),
                    EffectRadiusItem.createDefaultItem(),
                    EffectRadiusItem.createItem(ItemVariant.EXAMPLE_1),
                    EffectRadiusItem.createItem(ItemVariant.EXAMPLE_2),
                    EffectRadiusItem.createItem(ItemVariant.EXAMPLE_3),
                    EggThrowItItem.createDefaultItem(),
                    ExplosionBallItem.createDefaultItem(),
                    ExplosionBallItem.createItem(ItemVariant.EXAMPLE_1),
                    FrozenSword.createDefaultItem(),
                    GoatLauncherItem.createDefaultItem(),
                    GoatTrapItem.createDefaultItem(),
                    HitEffectItem.createDefaultItem(),
                    HookItem.createDefaultItem(),
                    InstaHealItem.createDefaultItem(),
                    ItemEffect.createDefaultItem(),
                    ItemEffect.createItem(ItemVariant.EXAMPLE_1),
                    ItemEffect.createItem(ItemVariant.EXAMPLE_2),
                    ItemsBag.createDefaultItem(),
                    LightningItem.createDefaultItem(),
                    MagicTotemItem.createDefaultItem(),
                    PokeballItem.createDefaultItem(),
                    ForcedPumpkinMask.createDefaultItem(),
                    PushItem.createDefaultItem(),
                    ReflectionEffectItem.createDefaultItem(),
                    ShieldItem.createDefaultItem(),
                    ShuffleInventoryItem.createDefaultItem(),
                    SnowballTPItem.createDefaultItem(),
                    StopFlyingItem.createDefaultItem(),
                    TentacleEffectItem.createDefaultItem(),
                    ThrowingEnderPearlsItem.createDefaultItem(),
                    ThrowUpItem.createDefaultItem(),
                    EnderBowItem.createDefaultItem(),
                    ItemTier.createDefaultItem(),
                    ConfusionTotemItem.createDefaultItem(),
                    ValentineTNTItem.createDefaultItem(),
                    LassoTrapItem.createDefaultItem(),
                    AttributeBuffItem.createDefaultItem(),
                    ResurrectGearItem.createDefaultItem(),
                    CustomStealthTrailItem.createDefaultItem(),
                    CustomTrackingStunItem.createDefaultItem(),
                    SnakeRevengeItem.createDefaultItem(),
                    PoliceBatonItem.createDefaultItem(),
                    EasterBasketItem.createDefaultItem(),
                    SnowgunDyngusItem.createDefaultItem(),
                    InfinityFireworkItem.createDefaultItem(),
                    SchematicPlacerItem.createDefaultItem(),
                    EatEffectItem.createDefaultItem()

            )
    );

}
