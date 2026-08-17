package dev.sable.sablespawner.spawn;

import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.SableSpawnerConfig;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.WorldConfig;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.player.PlayerManager;
import dev.sable.sablespawner.player.PlayerStatus;
import dev.sable.sablespawner.spawn.session.EnemySubLevelTracker;
import dev.sable.sablespawner.spawn.session.SpawnQueue;
import dev.sable.sablespawner.spawn.session.entry.EnemySubLevelEntry;
import dev.sable.sablespawner.spawn.session.entry.SpawnTicket;
import dev.sable.sablespawner.util.BoxUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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

    ObjectList<EnemySubLevelEntry> deferredEnemySubLevelEntryAppender = new ObjectArrayList<>();
    ObjectList<EnemySubLevelEntry> deferredEnemySubLevelEntryRemover = new ObjectArrayList<>();

    public EnemyControl(ServerLevel level){
        this.LEVEL = level;
        this.CONTAINER = SubLevelContainer.getContainer(level);
        this.SPAWNER = new Spawner(level);
        this.TRACKER = new EnemySubLevelTracker();
        this.SPAWN_QUEUE = new SpawnQueue(level);
    }


    public void callScan() { // keep running
        SPAWN_QUEUE.updateQueue();
        scanDebris();

        for ( EnemySubLevelEntry entry : TRACKER.getEntries().values() ) {
            entry.updateMassPercentage();
            if ( entry.isExpired() ) { deferredEnemySubLevelEntryRemover.add(entry); }
        }

        executeAppend();
        executeRemove();
    }

    public void callPerTick() { // only isSpawnerActive==true
        for ( EnemySubLevelEntry entry : TRACKER.getEntries().values() ) {
            if ( entry.isDestroyed() ) { onDestroyed( entry ); }
            if ( entry.isDebris() && entry.isExpired() ) { onDebrisExpired(entry); }
        }
        executeRemove();
    }

    public void callPer5Tick() { // only isSpawnerActive==true
        SPAWN_QUEUE.updateQueue();
        Object2ObjectOpenHashMap<UUID, PlayerStatus> needNewEnemyPlayers = getPlayerManager().query()
                .inLevel(LEVEL)
                .notProtected()
                .collect();

        for ( PlayerStatus playerStatus : needNewEnemyPlayers.values() ) {

            ServerPlayer player = playerStatus.getPlayer();
            UUID playerUUID = player.getUUID();
            if ( isEnemyNearby(player) ) { continue; }

            SpawnTicket ticket = this.SPAWN_QUEUE.getQueue().get(playerUUID);
            if ( ticket == null ) { continue; }

            if ( ticket.getScheduledSpawnTime( playerStatus ) <= getGameTime() ) {
                for ( int i = 0; i<5 ;i++ ) {
                    if ( spawn(ticket) ) {
                        this.SPAWN_QUEUE.pop(playerUUID);
                        break;
                    }
                    ticket.flushOrientation();
                }

            }
        }

        for ( EnemySubLevelEntry entry : TRACKER.getEntries().values() ) {
            entry.updateMassPercentage();

            if ( entry.isFTLCharging() ) {
                onFTLCharging(entry);

                if ( entry.isFTLChargeCompleted() ) { onFTLChargeComplete(entry); }
            }

            if ( entry.isExpired() ) { onShipExpired(entry); }
        }

    }

    public boolean spawn(SpawnTicket ticket) {
        UUID targetUUID = ticket.getTarget();
        ServerPlayer target = (ServerPlayer) LEVEL.getPlayerByUUID(targetUUID);
        if ( target == null ) { return false; }

        Vec3 targetPos = target.position();
        ArrayList<BlueprintPlacementPlan> placementPlans = ticket.getBlueprintPlacementPlans( targetPos );
        for ( BlueprintPlacementPlan plan : placementPlans ) {
            if( !SPAWNER.BoundBoxVacantDetection(LEVEL, plan) ) { return false; }
        }

        EnemyProperty property = ticket.getProperty();

        for ( BlueprintPlacementPlan plan : placementPlans ) {
            ServerSubLevel spawnedSubLevel = SPAWNER.spawnSublevelAsEnemy( property, LEVEL, plan );
            if ( spawnedSubLevel == null ) { continue; }

            EnemySubLevelEntry entry = new EnemySubLevelEntry( property, spawnedSubLevel, targetUUID );

            this.deferredEnemySubLevelEntryAppender.add(entry);
        }
        executeAppend();
        return true;
    }

    public void scanDebris() {
        List<ServerSubLevel> allSubLevels = CONTAINER.getAllSubLevels();
        for ( ServerSubLevel subLevel : allSubLevels ) {
            if ( isDebrisOfEnemy(subLevel) ) {
                if ( TRACKER.getEntries().containsKey( subLevel.getUniqueId() ) ) { continue; }

                this.deferredEnemySubLevelEntryAppender.add(new EnemySubLevelEntry(null, subLevel, null ));
            }
        }
    }
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

    public boolean isSpawnerActive(){
        return !LEVEL.getPlayers(p -> !p.isSpectator(), 1).isEmpty();
    }
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

    public void onDestroyed(EnemySubLevelEntry enemy) {
        scanDebris();
        if ( enemy.isDebris() ) { return; }
        if ( !enemy.isDestroyed() ) { return; }

        UUID targetUUID = enemy.getTarget();
        int enemyValue = Objects.requireNonNull(enemy.getProperty()).getValue();

        Object2ObjectOpenHashMap<UUID, PlayerStatus> hashMap = getPlayerManager().query().ofUUID(targetUUID).collect();
        if ( hashMap.isEmpty() ) { return; }

        PlayerStatus playerStatus = hashMap.values().iterator().next();

        playerStatus.addScore(enemyValue);
        playerStatus.protect();

        deferredEnemySubLevelEntryRemover.add(enemy);
    }

    public void onFTLCharging(EnemySubLevelEntry enemy) {
    }
    public void onFTLChargeComplete(EnemySubLevelEntry enemy) {
        deferredEnemySubLevelEntryRemover.add(enemy);
    }
    public void onShipExpired(EnemySubLevelEntry enemy){
        deferredEnemySubLevelEntryRemover.add(enemy);
    }
    public void onDebrisExpired(EnemySubLevelEntry debris){
        deferredEnemySubLevelEntryRemover.add(debris);
    }


    private void executeAppend() {
        for ( EnemySubLevelEntry entry : deferredEnemySubLevelEntryAppender ) {
            TRACKER.push(entry);
        }
        deferredEnemySubLevelEntryAppender.clear();
    }
    private void executeRemove() {
        for ( EnemySubLevelEntry entry : deferredEnemySubLevelEntryRemover ) {
            entry.removeSubLevel();
            TRACKER.pop(entry);
        }
        deferredEnemySubLevelEntryRemover.clear();
    }


    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private WorldConfig getWorldConfig() { return SableSpawner.DATAPACK_MANAGER.getWorldConfig(); }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }

}
