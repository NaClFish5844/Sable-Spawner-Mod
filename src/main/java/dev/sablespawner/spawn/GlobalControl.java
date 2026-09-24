package dev.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.player.PlayerManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static dev.sablespawner.SableSpawnerConfig.SCAN_INTERVAL;

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

        controller.clearEnemyIfRestart();
        container.addObserver(controller);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event){
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


    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }



}
