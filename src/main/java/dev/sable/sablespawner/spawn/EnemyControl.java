package dev.sable.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.SableSpawnerConfig;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.player.PlayerStatus;
import dev.sable.sablespawner.util.BoxUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class EnemyControl {
    static DatapackManager DATAPACK_MANAGER = SableSpawner.DATAPACK_MANAGER ;
    ServerLevel LEVEL;
    ServerSubLevelContainer CONTAINER;
    Spawner SPAWNER;

    Map<ServerPlayer, PlayerStatus> PlayerLevelTracker = new HashMap<>();
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
        if (DATAPACK_MANAGER.getWorldConfig() == null) { return false; }
        if (CONTAINER == null){ return false; }

        if ( !(Player.level() instanceof ServerLevel playerLevel)) { return false; }
        if ( playerLevel != LEVEL ) { return false; }
        Vec3 playerWorldPos = Player.position();

        String enemyPrefix = DATAPACK_MANAGER.getWorldConfig().getEnemyPrefix();
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

    public void callPerTick() {

    }

    public void callPer5Tick() {

    }

    public void spawn() {

    }

    public void scan() {

    }

    public void onDestroyed(UUID uuid) {

    }

    public void onFTLChargeComplete(UUID uuid) {

    }

    public void onPlayerJoinLevel(ServerPlayer player) {

    }

    public void onPlayerLeaveLevel(ServerPlayer player) {

    }

    public void appendTrackerEntry ( @Nullable EnemyProperty property, UUID uuid ) {
        ServerSubLevel subLevel = (ServerSubLevel) CONTAINER.getSubLevel(uuid);

        if ( subLevel != null ) {
            EnemySubLevelStatus stat = new EnemySubLevelStatus(property, subLevel, getGameTime());
            if ( stat.isInitialized() ) { this.ShipTracker.put( subLevel.getUniqueId(), stat ); }
        }
    }
    public void appendTrackerEntry( @Nullable EnemyProperty property, ServerSubLevel subLevel ) {
        appendTrackerEntry(property, subLevel.getUniqueId());
    }

    public void removeTrackerEntry( UUID uuid ) {
        this.ShipTracker.remove(uuid);
    }
    public void removeTrackerEntry( ServerSubLevel subLevel ) {
        removeTrackerEntry( subLevel.getUniqueId() );
    }

    public boolean isDebrisOfEnemy(ServerSubLevel subLevel) {
        if ( subLevel.getSplitFromSubLevel() == null ) { return false; }
        while (true){
            UUID fatherUUID = subLevel.getSplitFromSubLevel();
            ServerSubLevel father = (ServerSubLevel) CONTAINER.getSubLevel( fatherUUID );
            if ( father == null || father.getName() == null || DATAPACK_MANAGER.getWorldConfig() == null ) { return false; }
            if ( father.getSplitFromSubLevel() == null ) {
                return father.getName().contains( DATAPACK_MANAGER.getWorldConfig().getEnemyPrefix() );
            }
        }
    }

    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }

}
