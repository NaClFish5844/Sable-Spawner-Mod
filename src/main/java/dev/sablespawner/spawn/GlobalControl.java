package dev.sablespawner.spawn;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.player.PlayerManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static dev.sablespawner.SableSpawnerConfig.SCAN_INTERVAL;

public class GlobalControl {
    public static final GlobalControl INSTANCE = new GlobalControl();

    public Object2ObjectOpenHashMap<String, EnemyControl> CONTROLLERS = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent
    public void onLevelLoad(LevelEvent.Load event){
        if ( !( event.getLevel() instanceof ServerLevel level ) ) { return; }

        String dim = level.dimension().location().toString();
        EnemyControl controller = CONTROLLERS.get(dim);
        if ( controller != null ) { controller.rebind(level); }
        else { CONTROLLERS.put(dim, new EnemyControl(level)); }
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
