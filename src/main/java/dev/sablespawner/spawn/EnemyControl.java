package dev.sablespawner.spawn;

import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.SableSpawnerConfig;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sablespawner.player.PlayerManager;
import dev.sablespawner.player.PlayerStatus;
import dev.sablespawner.spawn.session.EnemySubLevelTracker;
import dev.sablespawner.spawn.session.SpawnQueue;
import dev.sablespawner.spawn.session.entry.EnemySubLevelEntry;
import dev.sablespawner.spawn.session.entry.SpawnTicket;
import dev.sablespawner.util.BoxUtil;
import dev.sablespawner.util.SpawnPatternUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.util.*;

@Getter
public class EnemyControl implements SubLevelObserver {
    ServerLevel LEVEL;
    ServerSubLevelContainer CONTAINER;
    Spawner SPAWNER;
    EnemySubLevelTracker ENEMY_TRACKER;
    SpawnQueue SPAWN_QUEUE;

    ObjectList<EnemySubLevelEntry> deferredEnemySubLevelEntryAppender = new ObjectArrayList<>();
    ObjectList<EnemySubLevelEntry> deferredEnemySubLevelEntryRemover = new ObjectArrayList<>();

    public EnemyControl(ServerLevel level, SubLevelContainer container){
        this.LEVEL = level;
        this.CONTAINER = (ServerSubLevelContainer) container;
        this.SPAWNER = new Spawner(level, this.CONTAINER);
        this.ENEMY_TRACKER = new EnemySubLevelTracker();
        this.SPAWN_QUEUE = new SpawnQueue(level);
    }
    public void rebind(ServerLevel level, SubLevelContainer container) {
        this.LEVEL = level;
        this.CONTAINER = (ServerSubLevelContainer) container;
        this.SPAWNER = new Spawner(level, this.CONTAINER);
        this.SPAWN_QUEUE = new SpawnQueue(level);

        ENEMY_TRACKER.getEntries().values().removeIf( entry -> !entry.rebind(CONTAINER) );

        clearEnemyIfRestart();
    }
    private void clearEnemyIfRestart() {
        if ( CONTAINER == null ) { return; }

        String prefix = getWorldConfig().getEnemyPrefix();
        for ( ServerSubLevel subLevel : CONTAINER.getAllSubLevels() ) {
            if ( subLevel.getSplitFromSubLevel() != null ) { continue; }
            if ( subLevel.getName() == null ) { continue; }
            if ( !subLevel.getName().contains(prefix) ) { continue; }
            if ( ENEMY_TRACKER.getEntries().containsKey(subLevel.getUniqueId()) ) { continue; }
            subLevel.markRemoved();
        }
    }

    public void callScan() { // keep running
        SPAWN_QUEUE.updateQueue();
        scanDebris();

        for ( EnemySubLevelEntry entry : ENEMY_TRACKER.getEntries().values() ) {
            entry.updateMassPercentage();
            if ( entry.isExpired() ) { deferredEnemySubLevelEntryRemover.add(entry); }
        }

        executeAppend();
        executeRemove();
    }
    public void callPerTick() { // only isSpawnerActive==true
        for ( EnemySubLevelEntry entry : ENEMY_TRACKER.getEntries().values() ) {
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

                        getLogger().debug("为 {} 刷新了{}:{}", Objects.requireNonNull(LEVEL.getPlayerByUUID(playerUUID)).getDisplayName(),ticket.property().getPackName(),ticket.property().getSchematicName());
                        break;
                    }
                }

            }
        }

        for ( EnemySubLevelEntry entry : ENEMY_TRACKER.getEntries().values() ) {
            entry.updateMassPercentage();

            if ( entry.isFTLCharging() ) {
                onFTLCharging(entry);

                if ( entry.isFTLChargeCompleted() ) { onFTLChargeComplete(entry); }
            }

            if ( entry.isExpired() ) { onShipExpired(entry); }
        }

    }
    @Override public void onSubLevelAdded(SubLevel subLevel) {
        ServerSubLevel subLevel1 = (ServerSubLevel) subLevel;
        if ( ENEMY_TRACKER.getEntries().containsKey(subLevel1.getUniqueId()) ) { return; }

        if ( isDebrisOfEnemy(subLevel1) ) {
            this.deferredEnemySubLevelEntryAppender.add(new EnemySubLevelEntry(null, subLevel1, null ));
        }

        executeAppend();
    }
    @Override public void onSubLevelRemoved(SubLevel subLevel, SubLevelRemovalReason reason) {
        if ( reason == SubLevelRemovalReason.UNLOADED ) { return; }

        UUID uuid = subLevel.getUniqueId();
        ENEMY_TRACKER.getEntries().remove(uuid);
    }

    public boolean spawn(SpawnTicket ticket) {
        UUID targetUUID = ticket.targetPlayer();
        ServerPlayer target = (ServerPlayer) LEVEL.getPlayerByUUID(targetUUID);
        if ( target == null ) { return false; }

        Vector3d targetPos = new Vector3d(
                target.position().x,
                target.position().y,
                target.position().z
                );

        // 此处需要加蓝图源mod判断
        if ( !ModList.get().isLoaded("sable_schematic_api") ) { return false; }
        ObjectList<BlueprintPlacementPlan> placementPlans = SpawnPatternUtil.newSableBlueprintPlacementPlan(ticket,targetPos);

        if ( placementPlans.isEmpty() ) { return false; }

        for ( BlueprintPlacementPlan plan : placementPlans ) {
            if( !SPAWNER.BoundBoxVacantDetection(LEVEL, plan) ) { return false; }
        }

        PropertyKey propertyKey = ticket.propertyKey();

        ObjectList<ServerSubLevel> buffer = new ObjectArrayList<>();

        for ( BlueprintPlacementPlan plan : placementPlans ) {
            ServerSubLevel spawnedSubLevel = SPAWNER.spawnSublevelWithName( propertyKey, LEVEL, plan );
            buffer.add(spawnedSubLevel);

            if ( spawnedSubLevel == null ) {
                for ( ServerSubLevel s : buffer ) {
                    if ( s != null ) { s.markRemoved(); }
                }

                this.deferredEnemySubLevelEntryAppender.clear();
                return false;
            }

            EnemySubLevelEntry entry = new EnemySubLevelEntry( ticket.property(), spawnedSubLevel, targetUUID );

            this.deferredEnemySubLevelEntryAppender.add(entry);
        }

        executeAppend();
        return true;
    }

    public void scanDebris() {
        if ( CONTAINER == null ){ return; }

        List<ServerSubLevel> allSubLevels = CONTAINER.getAllSubLevels();
        for ( ServerSubLevel subLevel : allSubLevels ) {
            if ( isDebrisOfEnemy(subLevel) ) {
                if ( ENEMY_TRACKER.getEntries().containsKey( subLevel.getUniqueId() ) ) { continue; }

                this.deferredEnemySubLevelEntryAppender.add(new EnemySubLevelEntry(null, subLevel, null ));
            }
        }
    }
    public boolean isDebrisOfEnemy(ServerSubLevel subLevel) {
        if ( subLevel.getSplitFromSubLevel() == null ) { return false; }
        UUID fatherUUID = subLevel.getSplitFromSubLevel();

        if ( ENEMY_TRACKER.getEntries().containsKey(subLevel.getUniqueId()) ) { return true; }
        if ( ENEMY_TRACKER.getEntries().containsKey(fatherUUID) ) { return true; }

        while (true){
            ServerSubLevel father = (ServerSubLevel) CONTAINER.getSubLevel( fatherUUID );
            if ( father == null || father.getName() == null ) { return false; }
            if ( father.getSplitFromSubLevel() == null ) {
                return father.getName().contains( getWorldConfig().getEnemyPrefix() );
            }
            fatherUUID = father.getSplitFromSubLevel();
        }
    }

    public boolean isLevelActive() {
        return getServer().getLevel(LEVEL.dimension()) == LEVEL;
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
        int enemyDetectionRadius = SableSpawnerConfig.ENEMY_DETECTION_DISTANCE.getAsInt();
        AABB detectionBox = AABB.ofSize(
                playerWorldPos,
                enemyDetectionRadius * 2,
                enemyDetectionRadius * 2,
                enemyDetectionRadius * 2
        );
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
            ENEMY_TRACKER.push(entry);
        }
        deferredEnemySubLevelEntryAppender.clear();
    }
    private void executeRemove() {
        for ( EnemySubLevelEntry entry : deferredEnemySubLevelEntryRemover ) {
            entry.removeSubLevel();
            ENEMY_TRACKER.pop(entry);
        }
        deferredEnemySubLevelEntryRemover.clear();
    }


    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private MinecraftServer getServer() { return SableSpawner.SERVER; }
    private DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private WorldConfig getWorldConfig() {
        WorldConfig config = getDatapackManager()
                .worldConfigQuery()
                .ofDimension(LEVEL)
                .collect();

        if ( config == null ) {
            config = WorldConfig.of( getDatapackManager().getDEFAULT_CONFIG(), null, WorldConfig.Pattern.invalid );
        }

        return config;
    }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }

}
