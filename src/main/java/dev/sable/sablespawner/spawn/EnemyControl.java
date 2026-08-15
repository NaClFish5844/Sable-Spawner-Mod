package dev.sable.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.SableSpawnerConfig;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.WorldConfig;
import dev.sable.sablespawner.player.PlayerManager;
import dev.sable.sablespawner.spawn.session.EnemySubLevelTracker;
import dev.sable.sablespawner.spawn.session.SpawnQueue;
import dev.sable.sablespawner.util.BoxUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class EnemyControl {
    ServerLevel LEVEL;
    ServerSubLevelContainer CONTAINER;
    Spawner SPAWNER;
    EnemySubLevelTracker TRACKER;
    SpawnQueue SPAWN_QUEUE;

    public EnemyControl(ServerLevel level){
        this.LEVEL = level;
        this.CONTAINER = SubLevelContainer.getContainer(level);
        this.SPAWNER = new Spawner(level);
        this.TRACKER = new EnemySubLevelTracker();
        this.SPAWN_QUEUE = new SpawnQueue(level);
    }


    public void callScan() { // keep running

    }

    public void callPerTick() { // only isSpawnerActive==true

    }

    public void callPer5Tick() { // only isSpawnerActive==true
        SPAWN_QUEUE.updateQueue();
    }

    public void spawn() { // ???

    }

    public void scan() { // scan

    }

    public void onSpawned() { // call in spawn()
        // 新船先构造EnemySubLevelStatus再append
    }

    public boolean isSpawnerActive(){
        return !LEVEL.getPlayers(p -> !p.isSpectator(), 1).isEmpty();
    }

    // tick
    public boolean isEnemyNearby(ServerPlayer Player) {
        if ( CONTAINER == null ){ return false; }

        if ( !(Player.level() instanceof ServerLevel playerLevel)) { return false; }
        if ( playerLevel != LEVEL ) { return false; }
        Vec3 playerWorldPos = Player.position();

        String enemyPrefix = getWorldConfig().getEnemyPrefix();
        int enemyDetectionDistance = SableSpawnerConfig.ENEMY_DETECTION_DISTANCE.getAsInt();
        AABB detectionBox = AABB.ofSize(playerWorldPos,enemyDetectionDistance,enemyDetectionDistance,enemyDetectionDistance);
        List<ServerSubLevel> sublevelList = CONTAINER.getAllSubLevels();

        for (ServerSubLevel sublevel: sublevelList) {
            if ( sublevel.getSplitFromSubLevel() != null ) { continue; }
            if ( sublevel.getName() == null ) { continue; }

            if (
                    BoxUtil.intersects(sublevel.boundingBox(), detectionBox) &&
                            sublevel.getName().contains(enemyPrefix)
            )
            {
                return true;
            }
        }

        return false;
    }

    // 5t
    public boolean isDebrisOfEnemy(ServerSubLevel subLevel) {
        if ( subLevel.getSplitFromSubLevel() == null ) { return false; }
        while (true){
            UUID fatherUUID = subLevel.getSplitFromSubLevel();
            ServerSubLevel father = (ServerSubLevel) CONTAINER.getSubLevel( fatherUUID );
            if ( father == null || father.getName() == null ) { return false; }
            if ( father.getSplitFromSubLevel() == null ) {
                return father.getName().contains( getWorldConfig().getEnemyPrefix() );
            }
        }
    }

    // 5t
    public void onDestroyed(UUID uuid) {

    }

    // 5t
    public void onFTLChargeComplete(UUID uuid) {

    }

    // 5t
    public void onExpired(){

    }


    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private WorldConfig getWorldConfig() { return SableSpawner.DATAPACK_MANAGER.getWorldConfig(); }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }

}
