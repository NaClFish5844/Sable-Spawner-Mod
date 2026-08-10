package dev.sable.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.SableSpawnerConfig;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.util.BoxUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnemyControl {
    static DatapackManager DATAPACK_MANAGER = SableSpawner.DATAPACK_MANAGER ;
    ServerLevel LEVEL;
    ServerSubLevelContainer CONTAINER;
    Spawner SPAWNER;

    // 可刷新的敌人列表
    // 由世界 玩家等级 事件（以后再说）生成
    // 多个不同队伍玩家出现时如何调度？？？ 跟玩家绑定吗？
    // Map+ArrayList? ArrayList?

    Map<UUID, EnemySubLevelStatus> ShipTracker = new HashMap<>();

    public EnemyControl(ServerLevel level){
        this.LEVEL = level;
        this.CONTAINER = SubLevelContainer.getContainer(level);
        this.SPAWNER = new Spawner(level);
    }

    public boolean isSpawnerActive(){
        return !LEVEL.getPlayers(p -> !p.isSpectator(), 1).isEmpty();
    }

    public boolean isEnemyNearby(ServerPlayer Player) {
        if (DATAPACK_MANAGER.worldConfig == null) { return false; }
        if (CONTAINER == null){ return false; }

        if ( !(Player.level() instanceof ServerLevel playerLevel)) { return false; }
        if ( playerLevel != LEVEL ) { return false; }
        Vec3 playerWorldPos = Player.position();

        String enemyPrefix = DATAPACK_MANAGER.worldConfig.enemyPrefix;
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



    public void callScan() {

    }

    public void callTick() {

    }

    public void spawn() {

    }

    public void scan() {

    }

    public void onDestroyed(UUID uuid) {

    }

    public void onFTLChargeComplete(UUID uuid) {

    }

    public void dropTrackerEntry(UUID uuid) {

    }

    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }

}
