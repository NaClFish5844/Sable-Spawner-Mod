package dev.sablespawner.util;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.blockentity.tracker.FleetBaseCoreBlockEntityTracker;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.player.PlayerManager;
import dev.sablespawner.spawn.GlobalControl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;

public final class AccessUtil {
    private AccessUtil() {}

    public static DatapackManager getDatapackManager()   { return SableSpawner.DATAPACK_MANAGER; }
    public static BlueprintManager getBlueprintManager() { return SableSpawner.BLUEPRINT_MANAGER; }
    public static PlayerManager getPlayerManager()       { return SableSpawner.PLAYER_MANAGER; }
    public static GlobalControl getGlobalControl()       { return SableSpawner.GLOBAL_CONTROLLER; }

    public static FleetBaseCoreBlockEntityTracker getFleetBaseCoreTracker() { return SableSpawner.FLEET_BASE_CORE_TRACKER; }


    public static Logger getLogger() { return SableSpawner.LOGGER; }
    public static ModList getModList() { return ModList.get(); }


    public static String getModId() { return SableSpawner.MODID; }
    @Nullable public static MinecraftServer getServer() { return SableSpawner.SERVER; }
    public static boolean isServerReady() { return SableSpawner.SERVER != null; }
    public static long getGameTime() { return requireServer().overworld().getGameTime(); }
    public static ServerLevel getOverworld() { return requireServer().overworld(); }


    public static Path getGameDir() { return FMLPaths.GAMEDIR.get(); }
    public static Path getSableSpawnerDir() { return getGameDir().resolve("sablespawner"); }
    public static Path getSableSchematicApiFolder() { return getGameDir().resolve("Sable-Schematics"); }

    private static MinecraftServer requireServer() {
        MinecraftServer server = SableSpawner.SERVER;
        if ( server == null ) {
            throw new IllegalStateException("服务器未就绪：在 ServerStarting 之前访问了服务器 / 时间");
        }
        return server;
    }
}
