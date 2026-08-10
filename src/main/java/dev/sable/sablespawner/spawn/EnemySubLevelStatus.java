package dev.sable.sablespawner.spawn;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import lombok.Getter;

import javax.annotation.Nullable;

import static dev.sable.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;

import java.util.UUID;


public class EnemySubLevelStatus {
    private final UUID uuid;
    private final ServerSubLevel sublevel;
    @Nullable public final EnemyProperty property;
    private double totalMass = -1;
    @Getter private double massPercentage = 100.0;
    private final long spawnedGameTick;
    @Getter private long FTLChargeStartTime;
    private final boolean isDebris;
    @Getter private final boolean initialized;

    public EnemySubLevelStatus(@Nullable EnemyProperty property, ServerSubLevel subLevel, long gameTick) {
        this.uuid = subLevel.getUniqueId();
        this.sublevel = subLevel;
        this.property = property;
        this.spawnedGameTick = gameTick;
        this.isDebris = ( subLevel.getSplitFromSubLevel() != null );

        this.initialized = this.init();
    }

    public boolean init() { // init only
        if ( this.isDebris ) { return true; }
        this.totalMass = this.sublevel.getMassTracker().getMass();

        return ( this.totalMass != -1);
    }

    public void updateMassPercentage() { // 谨慎使用 最差200us
        this.massPercentage =  this.sublevel.getSelfMassTracker().getMass() / this.totalMass;
    }

    public boolean isDestroyed() { // tick
        if ( isDebris() ) { return false; }

        return this.massPercentage <= this.property.destroyThreshold;
    }

    public boolean isFTLCharging() { // tick
        if ( isDebris() ) { return false; }

        if ( this.massPercentage <= this.property.FTLChargeThreshold ){
            this.FTLChargeStartTime = getGameTime();
        }
        return this.massPercentage <= this.property.FTLChargeThreshold;
    }

    public boolean isFTLChargeCompleted() { // tick
        if ( isDebris() ) { return false; }

        return ( getGameTime() - this.FTLChargeStartTime ) >= this.property.FTLChargeDuration;
    }

    public boolean isExpired() { // tick, SCAN_IV
        if ( isDebris() ) { return false; }

        double existTime = getGameTime() - this.spawnedGameTick;
        return existTime >= DEBRIS_DESPAWN_TIME.getAsInt();
    }

    private boolean isDebris() {
        return this.isDebris || this.property == null;
    }

    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
