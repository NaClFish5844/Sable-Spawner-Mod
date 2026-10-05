package dev.sablespawner;

import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.ryanhcode.sable.sublevel.plot.heat.SubLevelHeatMapManager;
import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import dev.sablespawner.blockentity.tracker.FleetBaseCoreBlockEntityTracker;
import dev.sablespawner.config.FleetBaseCoreConfig;
import dev.sablespawner.config.GeneralServerConfig;
import dev.sablespawner.manager.DataManager;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.player.PlayerDataAttachment;
import dev.sablespawner.player.PlayerManager;
import dev.sablespawner.registry.SableSpawnerBlockEntities;
import dev.sablespawner.registry.SableSpawnerBlocks;
import dev.sablespawner.registry.SableSpawnerCreativeTabs;
import dev.sablespawner.registry.SableSpawnerItems;
import dev.sablespawner.spawn.EnemyControl;
import dev.sablespawner.spawn.GlobalControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.sablespawner.manager.datapack.DatapackManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(SableSpawner.MODID)
public class SableSpawner {
    public static final String MODID = "sablespawner";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer SERVER;

    public static final DatapackManager DATAPACK_MANAGER = DatapackManager.INSTANCE;
    public static final BlueprintManager BLUEPRINT_MANAGER = BlueprintManager.INSTANCE;
    public static final PlayerManager PLAYER_MANAGER = PlayerManager.INSTANCE;
    public static final GlobalControl GLOBAL_CONTROLLER = GlobalControl.INSTANCE;

    public static final FleetBaseCoreBlockEntityTracker FLEET_BASE_CORE_TRACKER = FleetBaseCoreBlockEntityTracker.INSTANCE;

    public SableSpawner(IEventBus modEventBus, ModContainer modContainer) {
        PlayerDataAttachment.ATTACHMENT_TYPES.register(modEventBus);

        SableSpawnerItems.ITEMS.register(modEventBus);
        SableSpawnerBlocks.BLOCKS.register(modEventBus);
        SableSpawnerBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        SableSpawnerCreativeTabs.CREATIVE_TABS.register(modEventBus);

        FleetBaseCoreBlockEntity.initTicketTypes();

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(PLAYER_MANAGER);
        NeoForge.EVENT_BUS.register(GLOBAL_CONTROLLER);

        initSableListener();
        registerConfig(modContainer);
    }
    private void initSableListener() {
        SableEventPlatform.INSTANCE.onSubLevelContainerReady(
                (level, container) -> {
                    if ( level instanceof ServerLevel serverLevel ) {
                        GLOBAL_CONTROLLER.onContainerReady(serverLevel, container);
                    }
                });
        SubLevelHeatMapManager.addSplitListener(
                (level, bounds, blocks) -> {
                    if ( !(level instanceof ServerLevel serverLevel) ) { return; }

                    EnemyControl controller = GLOBAL_CONTROLLER.CONTROLLERS.get(
                            serverLevel.dimension().location().toString());
                    if ( controller == null ) { return; }

                    controller.onSplitDetected(blocks);
                });
    }
    private void registerConfig(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, GeneralServerConfig.SPEC,
                "sablespawner/general-server.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, FleetBaseCoreConfig.SPEC,
                "sablespawner/fleet-base-core-server.toml");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        DataManager.initSideLoadedFiles();
    }

    @SubscribeEvent public void onServerStarting(ServerStartingEvent event) {
        SERVER = event.getServer();

        DataManager.reloadAll();
        GLOBAL_CONTROLLER.clearEnemyOnRestart();

        LOGGER.info("SableSpawner 服务器启动中 | SableSpawner server starting");
    }
    @SubscribeEvent public void onServerStopped(ServerStoppedEvent event) {
        GLOBAL_CONTROLLER.clearAllTrackers();
    }




}
