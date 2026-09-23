package dev.sablespawner;

import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.sablespawner.manager.DataManager;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.player.PlayerDataAttachment;
import dev.sablespawner.player.PlayerManager;
import dev.sablespawner.spawn.GlobalControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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


    public SableSpawner(IEventBus modEventBus, ModContainer modContainer) {
        PlayerDataAttachment.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(PLAYER_MANAGER);
        NeoForge.EVENT_BUS.register(GLOBAL_CONTROLLER);

        SableEventPlatform.INSTANCE.onSubLevelContainerReady((level, container) -> {
            if ( level instanceof ServerLevel serverLevel ) {
                SableSpawner.GLOBAL_CONTROLLER.onContainerReady(serverLevel, container);
            }
        });

        modContainer.registerConfig(ModConfig.Type.SERVER, SableSpawnerConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        DataManager.initSideLoadedFiles();
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SERVER = event.getServer();

        DataManager.reloadAll();

        LOGGER.info("SableSpawner 服务器启动中 | SableSpawner server starting");
    }




}
