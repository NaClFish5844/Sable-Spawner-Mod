package dev.sable.sablespawner.spawn.session.entry;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import lombok.Getter;

import javax.annotation.Nullable;

import static dev.sable.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;

import java.util.UUID;


public class EnemySubLevelEntry {
    @Getter private final UUID uuid;
    private final ServerSubLevel sublevel;
    @Nullable public final EnemyProperty property;
    private double totalMass = -1;
    @Getter private double massPercentage = 100.0;
    private final long spawnedGameTick;
    @Getter private long FTLChargeStartTime = -1;
    private final boolean isDebris;
    @Getter private final boolean initialized;

    public EnemySubLevelEntry(@Nullable EnemyProperty property, ServerSubLevel subLevel, long gameTick) {
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

    public void updateMassPercentage() { // 5t
        if ( !this.initialized ) { return; }
        if ( this.totalMass <=0 ) { return; }
        if ( isDebris() ) { return; }

        this.massPercentage =  this.sublevel.getSelfMassTracker().getMass() / this.totalMass * 100.0 ;
    }

    public boolean isDestroyed() { // tick
        if ( isDebris() ) { return false; }

        return this.massPercentage <= this.property.getDestroyThreshold();
    }

    public boolean isFTLCharging() { // 5t
        if ( isDebris() ) { return false; }
        if ( isDestroyed() ) { return false; }

        if ( this.massPercentage <= this.property.getFTLChargeThreshold() && this.FTLChargeStartTime == -1 ){
            this.FTLChargeStartTime = getGameTime();
        }
        return this.massPercentage <= this.property.getFTLChargeThreshold();
    }

    public boolean isFTLChargeCompleted() { // 5t
        if ( isDebris() ) { return false; }
        if ( this.FTLChargeStartTime == -1 ) { return false; }

        return ( getGameTime() - this.FTLChargeStartTime ) >= this.property.getFTLChargeDuration();
    }

    public boolean isExpired() { // tick, scan
        double existTime = getGameTime() - this.spawnedGameTick;

        if ( isDebris() ) {
            return existTime >= DEBRIS_DESPAWN_TIME.getAsInt();
        }else{
            return existTime >= this.property.getLifeTime();
        }
    }

    private boolean isDebris() {
        return this.isDebris || this.property == null;
    }
    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
