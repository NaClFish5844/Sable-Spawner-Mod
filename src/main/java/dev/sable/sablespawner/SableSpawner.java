package dev.sable.sablespawner;

import dev.sable.sablespawner.manager.DataManager;
import dev.sable.sablespawner.manager.blueprint.BlueprintManager;
import dev.sable.sablespawner.player.PlayerManager;
import dev.sable.sablespawner.spawn.GlobalControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import dev.sable.sablespawner.manager.datapack.DatapackManager;
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

    public static ResourceManager RESOURCE_MANAGER;
    public static final DatapackManager DATAPACK_MANAGER = DatapackManager.INSTANCE;
    public static final BlueprintManager BLUEPRINT_MANAGER = BlueprintManager.INSTANCE;
    public static final PlayerManager PLAYER_MANAGER = PlayerManager.INSTANCE;
    public static final GlobalControl GLOBAL_CONTROLLER = GlobalControl.INSTANCE;



    public SableSpawner(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.SERVER, SableSpawnerConfig.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        SERVER = event.getServer();
        RESOURCE_MANAGER = SERVER.getResourceManager();

        NeoForge.EVENT_BUS.register(PLAYER_MANAGER);
        NeoForge.EVENT_BUS.register(GLOBAL_CONTROLLER);

        DataManager.reloadAll();

        LOGGER.info("SableSpawner server starting");
    }


}
