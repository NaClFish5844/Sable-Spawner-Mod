package dev.sablespawner.spawn.session.entry;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

import static dev.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;
import static dev.sablespawner.SableSpawnerConfig.LONG_DEBRIS_DESPAWN_TIME;

import java.util.UUID;


public class EnemySubLevelEntry {
    @Getter private final UUID uuid;
    @Getter @Setter private ServerSubLevel sublevel;
    @Nullable @Getter private final EnemyProperty property;
    @Nullable @Getter private final UUID target;
    @Getter private double totalMass = -1;
    @Getter private double massPercentage = 100.0;
    @Getter private long spawnedGameTick;
    @Getter private long FTLChargeStartTime = -1;
    private boolean isDebris;
    @Getter @Setter private boolean isLongLivedDebris = false;
    @Getter private final boolean initialized;

    public EnemySubLevelEntry(@Nullable EnemyProperty property, ServerSubLevel subLevel, @Nullable UUID target) {
        this.uuid = subLevel.getUniqueId();
        this.sublevel = subLevel;
        this.property = property;
        this.target = target;
        this.spawnedGameTick = getGameTime();
        this.isDebris = ( subLevel.getSplitFromSubLevel() != null );

        this.initialized = this.init();
    }

    public boolean init() { // init only
        if ( this.isDebris ) { return true; }
        this.totalMass = this.sublevel.getMassTracker().getMass();

        return ( this.totalMass != -1);
    }

    public double getRemainingMass() {
        return this.sublevel.getSelfMassTracker().getMass();
    }
    public void updateMassPercentage() {
        if ( !this.initialized ) { return; }
        if ( this.totalMass <=0 ) { return; }
        if ( isDebris() ) { return; }

        this.massPercentage =  getRemainingMass() / this.totalMass * 100.0 ;
    }

    public void removeSubLevel() {
        this.sublevel.markRemoved();
    }
    public void toDebris() {
        this.isDebris = true;
        this.spawnedGameTick = getGameTime();
        resetFTLCharge();
    }
    public void setFTLCharge() {
        if ( this.FTLChargeStartTime == -1 ){
            this.FTLChargeStartTime = getGameTime();
        }
    }
    public void resetFTLCharge() {
        this.FTLChargeStartTime = -1;
    }
    public boolean rebind(@Nullable ServerSubLevelContainer container) {
        if ( container == null ) { return false; }

        ServerSubLevel fresh = (ServerSubLevel) container.getSubLevel(this.uuid);
        if ( fresh == null ) { return false; }

        this.sublevel = fresh;
        return true;
    }

    public boolean isFTLCharging() {
        if ( this.isDebris ) { return false; }
        if ( this.property == null ) { return false; }
        if ( isDestroyed() ) { return false; }

        int threshold = this.property.getFTLChargeThreshold();
        if ( threshold == -1 ) { return false; }
        long duration = this.property.getFTLChargeDuration();
        if ( duration == -1 ) { return false; }

        return this.massPercentage <= this.property.getFTLChargeThreshold();
    }
    public boolean isFTLChargeCompleted() { // 5t
        if ( this.isDebris ) { return false; }
        if ( this.property == null ) { return false; }
        if ( !isFTLCharging() ) { return false; }

        return ( getGameTime() - this.FTLChargeStartTime ) >= this.property.getFTLChargeDuration();
    }
    public boolean isDestroyed() { // tick
        if ( this.isDebris ) { return false; }
        if ( this.property == null ) { return false; }

        int threshold = this.property.getDestroyThreshold();
        if ( threshold == -1 ) { return false; }

        return this.massPercentage <= threshold;
    }
    public boolean isExpired() {
        if ( this.isDebris || this.property == null ) {
            // debris handler
            if ( this.isLongLivedDebris ) { return getExistTime() >= LONG_DEBRIS_DESPAWN_TIME.getAsInt(); }
            else { return getExistTime() >= DEBRIS_DESPAWN_TIME.getAsInt(); }
        } else {
            // enemy handler
            long lifeTime = this.property.getLifeTime();
            if ( lifeTime == -1 ) { return false; }

            return getExistTime() >= lifeTime;
        }
    }
    public boolean isDebris() {
        return this.isDebris || this.property == null;
    }

    private long getExistTime() {
        return getGameTime() - this.spawnedGameTick;
    }
    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
