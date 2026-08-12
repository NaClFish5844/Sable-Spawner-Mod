package dev.sable.sablespawner.spawn;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.player.PlayerManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.sable.sablespawner.SableSpawnerConfig.SCAN_INTERVAL;

public class GlobalControl {
    public static final GlobalControl INSTANCE = new GlobalControl();

    public ArrayList<String> LEVELS = new ArrayList<>(List.of("deepspace:space"));
    public Object2ObjectOpenHashMap<String, EnemyControl> CONTROLLERS = new Object2ObjectOpenHashMap<>();


    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event){
        if ( !( event.getLevel() instanceof ServerLevel level ) ) { return; }

        String dim = level.dimension().location().toString();
        if (LEVELS.contains(dim) && !CONTROLLERS.containsKey(dim) ) { CONTROLLERS.put(dim, new EnemyControl(level)); }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event){
        if ( getGameTime() % SCAN_INTERVAL.getAsInt() == 0) {
            for ( EnemyControl controller :CONTROLLERS.values() ){ controller.callScan(); }
        }
        if ( getGameTime() % 5 == 0) {
            for ( EnemyControl controller :CONTROLLERS.values() ){ controller.callPer5Tick(); }
        }

        for ( EnemyControl controller :CONTROLLERS.values() ){ controller.callPerTick(); }
    }

    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }


}
