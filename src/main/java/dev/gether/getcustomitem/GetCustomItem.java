package dev.gether.getcustomitem;

import dev.gether.getcustomitem.bstats.Metrics;
import dev.gether.getcustomitem.cmd.CustomItemCommand;
import dev.gether.getcustomitem.cmd.GetRegionCommand;
import dev.gether.getcustomitem.cmd.GetSchematicsCommand;
import dev.gether.getcustomitem.cmd.arg.CustomItemArg;
import dev.gether.getcustomitem.cmd.arg.CustomRegionArg;
import dev.gether.getcustomitem.cmd.handler.NoPermissionHandler;
import dev.gether.getcustomitem.cmd.handler.UsageCmdHandler;
import dev.gether.getcustomitem.cooldown.CooldownManager;
import dev.gether.getcustomitem.file.FileManager;
import dev.gether.getcustomitem.hook.HookManager;
import dev.gether.getcustomitem.item.CustomItem;
import dev.gether.getcustomitem.item.ItemManager;
import dev.gether.getcustomitem.item.ItemType;
import dev.gether.getcustomitem.item.manager.BearFurReducedManager;
import dev.gether.getcustomitem.item.manager.FrozenManager;
import dev.gether.getcustomitem.item.manager.itembag.ItemBagManager;
import dev.gether.getcustomitem.item.manager.itembag.ItemBagService;
import dev.gether.getcustomitem.listener.EasterBasketListener;
import dev.gether.getcustomitem.listener.SnowgunDyngusListener;
import dev.gether.getcustomitem.listener.bbreak.DynamicAreaPickaxeListener;
import dev.gether.getcustomitem.listener.bbreak.ItemTierListener;
import dev.gether.getcustomitem.listener.bow.CrossbowListener;
import dev.gether.getcustomitem.listener.bow.CubidBowListener;
import dev.gether.getcustomitem.listener.bow.EnderBowListener;
import dev.gether.getcustomitem.listener.damage.*;
import dev.gether.getcustomitem.listener.eat.EatEffectItemListener;
import dev.gether.getcustomitem.listener.fishrod.AntiElytraHookListener;
import dev.gether.getcustomitem.listener.fishrod.HookListener;
import dev.gether.getcustomitem.listener.global.*;
import dev.gether.getcustomitem.listener.interaction.*;
import dev.gether.getcustomitem.listener.recevied.ReflectionEffectListener;
import dev.gether.getcustomitem.listener.recevied.ShieldItemListener;
import dev.gether.getcustomitem.listener.damage.ShuffleInvListener;
import dev.gether.getcustomitem.listener.resurrect.ConfusionTotemListener;
import dev.gether.getcustomitem.listener.resurrect.DeathTotemListener;
import dev.gether.getcustomitem.listener.resurrect.MagicTotemListener;
import dev.gether.getcustomitem.listener.resurrect.ResurrectGearListener;
import dev.gether.getcustomitem.region.CustomRegion;
import dev.gether.getcustomitem.region.RegionManager;
import dev.gether.getcustomitem.schematic.SchematicManager;
import dev.gether.getcustomitem.storage.MySQL;
import dev.gether.getcustomitem.task.EffectTask;
import dev.gether.getutils.builder.ItemStackBuilder;
import dev.gether.getutils.models.inventory.GetInventory;
import dev.gether.getutils.selector.SelectorManager;
import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public final class GetCustomItem extends JavaPlugin {

    @Getter
    private static GetCustomItem instance;
    private LiteCommands<CommandSender> liteCommands;

    @Getter
    private static final Random random = new Random(System.currentTimeMillis());

    @Getter
    private FileManager fileManager;

    @Getter
    private ItemManager itemManager;
    private MySQL mySQL;
    private ItemBagManager itemBagManager;

    @Getter
    private HookManager hookManager;

    @Getter
    private SelectorManager selectorManager;
    @Getter
    private RegionManager regionManager;

    @Getter
    private CooldownManager cooldownManager;

    @Getter
    private SchematicManager schematicManager;

    @Override
    public void onEnable() {
        instance = this;
        // config
        fileManager = new FileManager();
        fileManager.loadItems();

        // managers
        itemManager = new ItemManager(fileManager);
        hookManager = new HookManager(this);
        schematicManager = new SchematicManager(this);

        new GetInventory(this); // getUtils - register custom gui

        // region manager
        this.selectorManager = new SelectorManager(
                this,
                ItemStackBuilder.of(Material.BLAZE_ROD)
                        .name("&e&lRegion Selector &7(getCustomItem-v3)")
                        .lore(new ArrayList<>(List.of(
                                "&7Left-click to set the first point",
                                "&7Right-click to set the second point"
                        )))
                        .glow(true)
                        .build()
        );

        regionManager = new RegionManager(this, fileManager);

        List<CustomItem> allCustomItemByType = itemManager.findAllCustomItemByType(ItemType.ITEMS_BAG);
        if (!allCustomItemByType.isEmpty()) {
            mySQL = new MySQL(this, fileManager);
            if (!mySQL.isConnected()) {
                getLogger().severe("Cannot connect to the database!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }


            // services
            ItemBagService itemBagService = new ItemBagService(mySQL, fileManager);
            itemBagManager = new ItemBagManager(this, fileManager, itemBagService, itemManager);

        }
//        itemManager.initItems();

        cooldownManager = new CooldownManager(fileManager, this);
        FrozenManager frozenManager = new FrozenManager();
        BearFurReducedManager bearFurReducedManager = new BearFurReducedManager();

        CustomStealthTrailListener customStealthTrailListener = new CustomStealthTrailListener(itemManager, cooldownManager, fileManager);

        // listeners
        Stream.of(
                new GlobalEntityMakeDamageListener(itemManager, regionManager),
                new GlobalEntityReceivedDamageListener(itemManager, regionManager),
                new GlobalInteractListener(itemManager, regionManager),
//                new GlobalPlayerMoveListener(itemManager),
                new GlobalConsumeListener(itemManager, regionManager),
                new GlobalBlockBreakListener(itemManager, regionManager),
                new GlobalEntityResurrectListener(itemManager, regionManager),
                new GlobalEntityBowShootListener(itemManager, regionManager),
                new GlobalProjectileHitListener(itemManager, regionManager),
                new GlobalFishListener(itemManager, regionManager),
                new CobwebGrenadeListener(itemManager, cooldownManager, fileManager),
                new HookListener(itemManager, cooldownManager, fileManager),
                new CrossbowListener(this, itemManager, cooldownManager, fileManager),
                new EffectRadiusListener(itemManager, cooldownManager, fileManager),
                new FrozenSwordListener(itemManager, cooldownManager, fileManager, frozenManager),
                new AntiCobwebListener(itemManager, cooldownManager, fileManager),
                new MagicTotemListener(itemManager, cooldownManager, fileManager),
                new BearFurListener(itemManager, cooldownManager, bearFurReducedManager, fileManager),
                new HitEffectListener(itemManager, cooldownManager, fileManager),
                new PlayerQuitListener(bearFurReducedManager, cooldownManager, frozenManager),
                new SnowballTeleport(this, itemManager, cooldownManager, fileManager),
                new EggThrowItListener(itemManager, cooldownManager, fileManager),
                new InstaHealListener(itemManager, cooldownManager, fileManager),
                new LightningItemListener(itemManager, cooldownManager, fileManager),
                new PushItemListener(itemManager, cooldownManager, fileManager), //
                new ShieldItemListener(itemManager, cooldownManager, fileManager),
                new ShuffleInvListener(itemManager, cooldownManager, fileManager),
                new StopFlyingListener(itemManager, cooldownManager, fileManager, this),
                new ThrowingEnderPearlsListener(itemManager, cooldownManager, fileManager),
                new ThrowUpListener(itemManager, cooldownManager, fileManager),
                new CubidBowListener(this, itemManager, cooldownManager, fileManager),
                new ItemBagListener(itemManager, cooldownManager, fileManager, itemBagManager, regionManager),
                new ItemTierListener(itemManager, cooldownManager, fileManager),
                new ExplosionBallListener(itemManager, cooldownManager, fileManager),
                new DropToInventoryItemListener(itemManager, cooldownManager, fileManager),
                new ReflectionEffectListener(itemManager, cooldownManager, fileManager),
                new CustomSphereListener(itemManager, cooldownManager, fileManager),
                new PokeballListener(itemManager, cooldownManager, fileManager),
                new GoatLauncherListener(itemManager, cooldownManager, fileManager),
                new AntiElytraHookListener(itemManager, cooldownManager, fileManager, this),
                new BubbleTrapListener(itemManager, cooldownManager, fileManager),
                new ChangeYawListener(itemManager, cooldownManager, fileManager),
                new ConfusionTotemListener(itemManager, cooldownManager, fileManager),
                new DeathTotemListener(itemManager, cooldownManager, fileManager),
                new DynamicAreaPickaxeListener(itemManager, cooldownManager, fileManager),
                new EnderBowListener(this, itemManager, cooldownManager, fileManager),
                new ForcedPumpkinMaskListener(itemManager, cooldownManager, fileManager),
                new GoatTrapListener(itemManager, cooldownManager, fileManager),
                new ValentineTNTListener(itemManager, cooldownManager, fileManager),
                new LassoTrapListener(itemManager, cooldownManager, fileManager),
                new AttributeBuffListener(itemManager, cooldownManager, fileManager),
                new EatEffectItemListener(itemManager, cooldownManager, fileManager),
                customStealthTrailListener,
                new CustomTrackingStunListener(itemManager, cooldownManager, fileManager),
                new ResurrectGearListener(itemManager, cooldownManager, fileManager),
                new ArmorDegraderListener(itemManager, cooldownManager, fileManager),
                new EasterBasketListener(itemManager, cooldownManager, fileManager),
                new SnowgunDyngusListener(this, itemManager, cooldownManager, fileManager),
                new PoliceBatonListener(itemManager, cooldownManager, fileManager),
                new TentacleEffectListener(itemManager, cooldownManager, fileManager),
                new SnakeRevengeListener(itemManager, cooldownManager, fileManager, customStealthTrailListener),
                new InfinityFireworkListener(itemManager, cooldownManager, fileManager),
                new SchematicPlacerListener(itemManager, cooldownManager, fileManager, schematicManager, this),
                new PrepareAnvilListener(itemManager, fileManager)
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        // register command
        registerCommand(itemManager);

        new EffectTask(itemManager).runTaskTimer(this, 20L, 20L);

        // register bstats
        new Metrics(this, 21420);

    }

    private void registerCommand(ItemManager itemManager) {
        this.liteCommands = LiteBukkitFactory.builder("getCustomItem", this)
                .commands(
                        new CustomItemCommand(this),
                        new GetSchematicsCommand(this, schematicManager),
                        new GetRegionCommand(this)
                )
                .invalidUsage(new UsageCmdHandler())
                .missingPermission(new NoPermissionHandler())
                .argument(CustomItem.class, new CustomItemArg(itemManager))
                .argument(CustomRegion.class, new CustomRegionArg(this))
                .build();
    }

    @Override
    public void onDisable() {

        SchematicPlacerListener.cancelAllStructures();

        Bukkit.getScheduler().cancelTasks(this);

        // unregister cmd
        if (this.liteCommands != null) {
            this.liteCommands.unregister();
        }

        if (mySQL != null) {
            itemBagManager.saveAllBackpacks();
            mySQL.disconnect();
        }

        HandlerList.unregisterAll(this);

    }


}
