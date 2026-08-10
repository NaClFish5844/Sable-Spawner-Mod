package dev.sable.sablespawner.spawn;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import static dev.sable.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;

import java.util.UUID;



public class EnemySubLevelStatus {
    public UUID uuid;
    public ServerSubLevel sublevel;
    public EnemyProperty property;
    public double totalMass = -1;
    public double massPercentage = 100.0;
    public long spawnedGameTick;
    public long FTLChargeStartTime;
    public boolean isDebris;
    public boolean isInitialized;

    public EnemySubLevelStatus(EnemyProperty property, ServerSubLevel subLevel, long gameTick) {
        this.uuid = subLevel.getUniqueId();
        this.sublevel = subLevel;
        this.property = property;
        this.spawnedGameTick = gameTick;
        this.isDebris = ( subLevel.getSplitFromSubLevel() != null );

        this.isInitialized = this.init();
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
        return this.massPercentage <= this.property.destroyThreshold;
    }

    public boolean isFTLCharging() { // tick
        if ( this.massPercentage <= this.property.FTLChargeThreshold ){
            this.FTLChargeStartTime = getGameTime();
        }
        return this.massPercentage <= this.property.FTLChargeThreshold;
    }

    public boolean isFTLChargeCompleted() { // tick
        return ( getGameTime() - this.FTLChargeStartTime ) >= this.property.FTLChargeDuration;
    }

    public boolean isExpired() { // tick, SCAN_IV
        double existTime = getGameTime() - this.spawnedGameTick;
        if ( this.isDebris ) { return existTime >= DEBRIS_DESPAWN_TIME.getAsInt(); }
        else{ return existTime >= this.property.lifeTime; }
    }

    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
