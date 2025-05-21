package dev.gether.getcustomitem.item;


import dev.gether.getcustomitem.item.customize.*;
import dev.gether.getcustomitem.item.customize.CustomStealthTrailItem;
import dev.gether.getcustomitem.item.customize.armor.ArmorDegraderItem;
import dev.gether.getcustomitem.item.customize.bow.CrossBowItem;
import dev.gether.getcustomitem.item.customize.bow.CupidBowItem;
import dev.gether.getcustomitem.item.customize.bow.EnderBowItem;
import dev.gether.getcustomitem.item.customize.itemtier.ItemTier;
import dev.gether.getcustomitem.item.customize.resurrect.ConfusionTotemItem;
import dev.gether.getcustomitem.item.customize.resurrect.MagicTotemItem;
import dev.gether.getcustomitem.item.customize.resurrect.ResurrectGearItem;

public enum ItemType {
    HOOK(HookItem.class),
    CROSSBOW(CrossBowItem.class),
    COBWEB_GRENADE(CobwebGrenade.class),
    EFFECT_RADIUS(EffectRadiusItem.class),
    FROZEN_SWORD(FrozenSword.class),
    ANTY_COBWEB(AntiCobweb.class),
    BEAR_FUR(BearFurItem.class),
    MAGIC_TOTEM(MagicTotemItem.class),
    HIT_EFFECT(HitEffectItem.class),
    SNOWBALL_TP(SnowballTPItem.class),
    INSTA_HEAL(InstaHealItem.class),
    PUSH_ITEM(PushItem.class),
    THROW_UP(ThrowUpItem.class),
    LIGHTNING_ITEM(LightningItem.class),
    SHIELD_ITEM(ShieldItem.class),
    EGG_THROW_UP(EggThrowItItem.class),
    THROWING_ENDER_PEARLS(ThrowingEnderPearlsItem.class),
    ITEM_EFFECT(ItemEffect.class),
    SHUFFLE_ITEM(ShuffleInventoryItem.class),
    CUPIDS_BOW(CupidBowItem.class),
    STOP_FLYING(StopFlyingItem.class),
    ITEMS_BAG(ItemsBag.class),
    ITEM_TIER(ItemTier.class),
    EXPLOSION_BALL(ExplosionBallItem.class),
    DROP_TO_INV(DropToInventoryItem.class),
    POKE_BALL(PokeballItem.class),
    CUSTOM_SPHERE(CustomSphereItem.class),
    TENTACLE_EFFECT(TentacleEffectItem.class),
    GOAT_LAUNCHER(GoatLauncherItem.class),
    ANTI_ELYTRA_HOOK(AntiElytraHookItem.class),
    BUBBLE_TRAP(BubbleTrapItem.class),
    REFLECTION_EFFECT(ReflectionEffectItem.class),
    CONFUSION_TOTEM(ConfusionTotemItem.class),
    DEATH_TOTEM(DeathTotemItem.class),
    DYNAMIC_AREA_PICKAXE(DynamicAreaPickaxeItem.class),
    ENDER_BOW(EnderBowItem.class),
    CHANGE_YAW(ChangeYawItem.class),
    GOAT_TRAP(GoatTrapItem.class),
    FORCE_PUMPKIN(ForcedPumpkinMask.class),
    VALENTINE_TNT(ValentineTNTItem.class),
    ATTRIBUTE_BUFF(AttributeBuffItem.class),
    LASSO_TRAP(LassoTrapItem.class),
    SNAKE_REVENGE(SnakeRevengeItem.class),
    POLICE_BATON(PoliceBatonItem.class),
    EAT_EFFECT(EatEffectItem.class),
    RESURRECT_GEAR(ResurrectGearItem.class),
    TRACKING_STUN(CustomTrackingStunItem.class),
    ARMOR_DEGRADER(ArmorDegraderItem.class),
    EASTER_BASKET(EasterBasketItem.class),
    INFINITY_FIREWORK(InfinityFireworkItem.class),
    SNOWGUN_DYNGUS(SnowgunDyngusItem.class),
    SCHEMATIC_PLACER(SchematicPlacerItem.class),
    CUSTOM_STEALTH_TRAIL(CustomStealthTrailItem.class);
    private final Class<? extends CustomItem> itemClass;

    ItemType(Class<? extends CustomItem> itemClass) {
        this.itemClass = itemClass;
    }

    public Class<? extends CustomItem> getItemClass() {
        return itemClass;
    }
}
