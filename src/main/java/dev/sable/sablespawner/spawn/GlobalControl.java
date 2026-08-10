package dev.sable.sablespawner.spawn;

import dev.sable.sablespawner.SableSpawner;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.sable.sablespawner.SableSpawnerConfig.SCAN_INTERVAL;

public class GlobalControl {
    public static final GlobalControl INSTANCE = new GlobalControl();

    public ArrayList<String> LEVELS = new ArrayList<>(List.of("deepspace:space"));
    public Map<String, EnemyControl> CONTROLLERS = new HashMap<>();

    public void onLevelLoad(ServerLevel level){
        String dim = level.dimension().location().toString();
        if (LEVELS.contains(dim) && !CONTROLLERS.containsKey(dim) ){
            CONTROLLERS.put(dim, new EnemyControl(level));
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event){
        long gt = SableSpawner.SERVER.overworld().getGameTime();

        if ( gt % SCAN_INTERVAL.getAsInt() == 0) {
            for ( EnemyControl controller :CONTROLLERS.values() ){ controller.callScan(); }
        }

        for ( EnemyControl controller :CONTROLLERS.values() ){ controller.callTick(); }

    }

}
