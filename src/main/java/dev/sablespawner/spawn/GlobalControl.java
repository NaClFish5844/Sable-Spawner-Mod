package dev.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static dev.sablespawner.config.GeneralServerConfig.SCAN_INTERVAL;
import static dev.sablespawner.util.AccessUtil.*;

public class GlobalControl {
    public static final GlobalControl INSTANCE = new GlobalControl();

    public Object2ObjectOpenHashMap<String, EnemyControl> CONTROLLERS = new Object2ObjectOpenHashMap<>();

    public void onContainerReady(ServerLevel serverLevel, SubLevelContainer container) {
        String dim = serverLevel.dimension().location().toString();
        EnemyControl controller = CONTROLLERS.get(dim);

        if ( controller != null ) { controller.rebind(serverLevel, container); }
        else {
            controller = new EnemyControl(serverLevel, container);
            CONTROLLERS.put(dim, controller);
        }

        container.addObserver(controller);
    }
    public void clearEnemyOnRestart() {
        for ( EnemyControl controller : CONTROLLERS.values() ) {
            if ( !controller.isLevelActive() ) { continue; }
            controller.clearEnemyOnServerStart();
        }
    }

    @SubscribeEvent public void onServerTick(ServerTickEvent.Post event){
        if ( getGameTime() % SCAN_INTERVAL.getAsInt() == 0) {
            for ( EnemyControl controller :CONTROLLERS.values() ){
                if ( !controller.isLevelActive() ) { continue; }
                controller.callScan();
            }
        }
        if ( getGameTime() % 5 == 0) {
            for ( EnemyControl controller :CONTROLLERS.values() ){
                if ( !controller.isLevelActive() || !controller.isSpawnerActive() ) { continue; }
                controller.callPer5Tick();
            }
        }

        for ( EnemyControl controller :CONTROLLERS.values() ){
            if ( !controller.isLevelActive() || !controller.isSpawnerActive() ) { continue; }
            controller.callPerTick();
        }
    }

    public void clearAllTrackers() {
        for (EnemyControl controller : CONTROLLERS.values()) {
            controller.ENEMY_TRACKER.clear();
            controller.ALLY_TRACKER.clear();
            controller.DEBRIS_TRACKER.clear();

            controller.SPAWN_QUEUE.clear();
        }
    }
}
