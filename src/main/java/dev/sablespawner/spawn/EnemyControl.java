package dev.sablespawner.spawn;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import dev.sablespawner.config.GeneralServerConfig;
import dev.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sablespawner.player.PlayerStatus;
import dev.sablespawner.spawn.session.tracker.AllySubLevelTracker;
import dev.sablespawner.spawn.session.tracker.DebrisSubLevelTracker;
import dev.sablespawner.spawn.session.tracker.EnemySubLevelTracker;
import dev.sablespawner.spawn.session.spawnqueue.SpawnQueue;
import dev.sablespawner.spawn.session.tracker.entry.AllySubLevelEntry;
import dev.sablespawner.spawn.session.tracker.entry.DebrisSubLevelEntry;
import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import dev.sablespawner.spawn.session.spawnqueue.SpawnTicket;
import dev.sablespawner.spawn.session.tracker.entry.SubLevelEntry;
import dev.sablespawner.util.BoxUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

import static dev.sablespawner.util.AccessUtil.*;

@Getter
public class EnemyControl implements SubLevelObserver {
    ServerLevel LEVEL;
    ServerSubLevelContainer CONTAINER;
    Spawner SPAWNER;
    SpawnQueue SPAWN_QUEUE;

    EnemySubLevelTracker ENEMY_TRACKER;
    AllySubLevelTracker ALLY_TRACKER;
    DebrisSubLevelTracker DEBRIS_TRACKER;

    private static final long DEBRIS_MATCH_DISTANCE = 1024;

    public EnemyControl(ServerLevel level, SubLevelContainer container){
        this.LEVEL = level;
        this.CONTAINER = (ServerSubLevelContainer) container;
        this.SPAWNER = new Spawner(level, this.CONTAINER);
        this.SPAWN_QUEUE = new SpawnQueue(level);

        this.ENEMY_TRACKER = new EnemySubLevelTracker();
        this.ALLY_TRACKER = new AllySubLevelTracker();
        //this.DEBRIS_TRACKER = new DebrisSubLevelTracker(this.CONTAINER, this::onDebrisCreated);
        this.DEBRIS_TRACKER = new DebrisSubLevelTracker(this.CONTAINER, this::onDebrisCreated, this::isTracked);
    }
    public void rebind(ServerLevel level, SubLevelContainer container) {
        this.LEVEL = level;
        this.CONTAINER = (ServerSubLevelContainer) container;
        this.SPAWNER = new Spawner(level, this.CONTAINER);
        this.SPAWN_QUEUE = new SpawnQueue(level);

        ENEMY_TRACKER.rebindAll(CONTAINER);
        ALLY_TRACKER.rebindAll(CONTAINER);
        DEBRIS_TRACKER.rebindAll(CONTAINER);
    }
    public void clearEnemyOnServerStart() {
        if ( CONTAINER == null ) { return; }

        String prefix = getWorldConfig().getEnemyPrefix();
        for ( ServerSubLevel subLevel : CONTAINER.getAllSubLevels() ) {
            if ( subLevel.getSplitFromSubLevel() != null ) { continue; }
            if ( subLevel.getName() == null ) { continue; }
            if ( !subLevel.getName().contains(prefix) ) { continue; }
            if ( ENEMY_TRACKER.contains( subLevel.getUniqueId() ) ) { continue; }

            subLevel.markRemoved();
        }
    }

    public void callScan() { // keep running
        SPAWN_QUEUE.updateQueue();

        ENEMY_TRACKER.updateMass();
        ENEMY_TRACKER.deferredRemoverAddAll(
                ENEMY_TRACKER.query().isExpired().collect()
        );


        // ALLY_TRACKER.updateMass();
        // ALLY_TRACKER.deferredRemoverAddAll(
        //         ALLY_TRACKER.query().isExpired().collect()
        // );

        ENEMY_TRACKER.executeTrackerUpdate();
        // ALLY_TRACKER.executeTrackerUpdate();
        DEBRIS_TRACKER.executeTrackerUpdate();
    }
    public void callPerTick() { // only isSpawnerActive==true
        for ( EnemySubLevelEntry enemy : ENEMY_TRACKER.query().isDestroyed().collect() ) {
            onEnemyShipDestroyed(enemy);
        }
        // for ( AllySubLevelEntry ally : ALLY_TRACKER.query().isDestroyed().collect() ) {
        // onEnemyShipDestroyed(ally);
        // }

        for ( DebrisSubLevelEntry debris : DEBRIS_TRACKER.query().isExpired().collect() ) {
            onDebrisExpired(debris);
        }

        DEBRIS_TRACKER.collectPendingDebris();

        ENEMY_TRACKER.executeTrackerUpdate();
        // ALLY_TRACKER.executeTrackerUpdate();
        DEBRIS_TRACKER.executeTrackerUpdate();
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
                for ( int i = 0; i < 3 ;i++ ) {
                    if ( spawnEnemy(ticket) ) {
                        this.SPAWN_QUEUE.pop(playerUUID);
                        // getLogger().debug("为 {} 刷新了{}:{}", Objects.requireNonNull(LEVEL.getPlayerByUUID(playerUUID)).getDisplayName(),ticket.property().getPackName(),ticket.property().getSchematicName());
                        break;
                    }
                }
            }
        }


        ENEMY_TRACKER.updateMass();
        for ( EnemySubLevelEntry entry : ENEMY_TRACKER.query().collect() ) {
            if ( entry.isFTLCharging() ) {
                entry.setFTLCharge();
                onEnemyFTLCharging(entry);

                if ( entry.isFTLChargeCompleted() ) { onEnemyFTLChargeComplete(entry); }
            } else {
                entry.resetFTLCharge();
            }

            if ( entry.isExpired() ) { onEnemyShipExpired(entry); }
        }

        // ALLY_TRACKER.updateMass();
        // for ( AllySubLevelEntry entry : ALLY_TRACKER.query().collect() ) {
        //     ObjectList<AllySubLevelEntry> inFTL = ALLY_TRACKER.query().isFTLCharging().collect();
        //
        //     if ( inFTL.contains(entry) ) {
        //         entry.setFTLCharge();
        //         onEnemyFTLCharging(entry);
        //
        //         if ( entry.isFTLChargeCompleted() ) { onAllyFTLChargeComplete(entry); }
        //     } else {
        //         entry.resetFTLCharge();
        //     }
        //
        //     if ( entry.isExpired() ) { onAllyShipExpired(entry); }
        // }

        ENEMY_TRACKER.executeTrackerUpdate();
        // ALLY_TRACKER.executeTrackerUpdate();
        DEBRIS_TRACKER.executeTrackerUpdate();
    }
    @Override public void onSubLevelAdded(SubLevel subLevel) {}
    @Override public void onSubLevelRemoved(SubLevel subLevel, SubLevelRemovalReason reason) {
        if ( reason == SubLevelRemovalReason.UNLOADED ) { return; }

        UUID uuid = subLevel.getUniqueId();
        ENEMY_TRACKER.pop(uuid);
        ALLY_TRACKER.pop(uuid);
        DEBRIS_TRACKER.pop(uuid);
    }
    public void onSplitDetected(Collection<BlockPos> blocks) {
        if ( blocks.isEmpty() ) { return; }
        BlockPos first = blocks.iterator().next();

        SubLevel parentSubLevel = getParentSubLevelByPlot(first);
        if ( parentSubLevel == null ) { return; }

        Pair<Class<? extends SubLevelEntry>, SubLevelEntry> parent = getTrackedEntry(parentSubLevel.getUniqueId());
        if ( parent == null ) { return; }

        DEBRIS_TRACKER.pushPendingSplit(new DebrisSubLevelTracker.PendingSplit(
                parent.right(),
                parentSubLevel,
                getGameTime(),
                blocks,
                new BoundingBox3d(parentSubLevel.boundingBox())
        ));
    }

    @Nullable private SubLevel getParentSubLevelByPlot(BlockPos pos) {
        SubLevel nearest = null;
        long nearestDistance = DEBRIS_MATCH_DISTANCE;

        for ( ServerSubLevel subLevel : CONTAINER.getAllSubLevels() ) {
            BlockPos center = subLevel.getPlot().getCenterBlock();
            long distance = Math.abs(center.getX() - pos.getX()) + Math.abs(center.getZ() - pos.getZ());

            if ( distance > nearestDistance ) { continue; }
            nearestDistance = distance;
            nearest = subLevel;
        }
        return nearest;
    }

    public boolean spawnEnemy(SpawnTicket ticket) {
        ObjectList<ServerSubLevel> spawned = SPAWNER.spawnEnemyOfTicket(ticket);
        if ( spawned.isEmpty() ) { return false; }

        onEnemyShipSpawned(ticket, spawned);
        return true;
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
        int enemyDetectionRadius = GeneralServerConfig.ENEMY_DETECTION_DISTANCE.getAsInt();
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
    public boolean isTracked(UUID uuid) {
        return ENEMY_TRACKER.contains(uuid)
                || DEBRIS_TRACKER.contains(uuid)
                || ALLY_TRACKER.contains(uuid);
    }

    @Nullable public Pair<Class<? extends SubLevelEntry>, SubLevelEntry> getTrackedEntry(UUID uuid) {
        if ( !isTracked(uuid) ) { return null; }

        SubLevelEntry entry = ENEMY_TRACKER.get(uuid);
        if ( entry != null ) { return Pair.of(EnemySubLevelEntry.class, entry); }
        entry = ALLY_TRACKER.get(uuid);
        if ( entry != null ) { return Pair.of(AllySubLevelEntry.class, entry); }
        entry = DEBRIS_TRACKER.get(uuid);
        if ( entry != null ) { return Pair.of(DebrisSubLevelEntry.class, entry); }

        return null;
    }

    public void onEnemyShipSpawned(SpawnTicket ticket, ObjectList<ServerSubLevel> spawned) {
        for ( ServerSubLevel subLevel : spawned ) {
            EnemySubLevelEntry entry = new EnemySubLevelEntry( ticket.property(), subLevel, ticket.targetPlayer() );
            ENEMY_TRACKER.deferredAppenderAdd(entry);
        }
    }
    public void onEnemyShipDestroyed(EnemySubLevelEntry enemy) {
        ENEMY_TRACKER.deferredTransferAdd(enemy);
        DEBRIS_TRACKER.deferredAppenderAdd(
                DebrisSubLevelEntry.ofWreck(
                        DebrisSubLevelEntry.SourceSubLevelType.enemy,
                        enemy.getSublevel(),
                        enemy.getSublevel().getName()
                )
        );

        Object2ObjectOpenHashMap<UUID, PlayerStatus> hashMap =
                getPlayerManager().query().ofUUID( enemy.getTarget() ).collect();
        if ( hashMap.isEmpty() ) { return; }

        PlayerStatus playerStatus = hashMap.values().iterator().next();
        playerStatus.addScore( Objects.requireNonNull(enemy.getProperty()).getValue() );
        playerStatus.protect();
    }
    public void onEnemyFTLCharging(EnemySubLevelEntry enemy) {
    }
    public void onEnemyFTLChargeComplete(EnemySubLevelEntry enemy) {
        ENEMY_TRACKER.deferredRemoverAdd(enemy);
    }
    public void onEnemyShipExpired(EnemySubLevelEntry enemy){
        ENEMY_TRACKER.deferredRemoverAdd(enemy);
    }

    public void onAllyShipSpawned() {
        // WIP
    }
    public void onAllyShipDestroyed(AllySubLevelEntry ally) {
        // WIP
    }
    public void onAllyFTLCharging(AllySubLevelEntry ally) {
        // WIP
    }
    public void onAllyFTLChargeComplete(AllySubLevelEntry ally) {
        // WIP
    }
    public void onAllyShipExpired(AllySubLevelEntry ally){
        // WIP
    }

    public void onDebrisCreated(DebrisSubLevelEntry debris) {
        DEBRIS_TRACKER.deferredAppenderAdd(debris);
    }
    public void onDebrisExpired(DebrisSubLevelEntry debris){
        DEBRIS_TRACKER.deferredRemoverAdd(debris);
    }


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
}
