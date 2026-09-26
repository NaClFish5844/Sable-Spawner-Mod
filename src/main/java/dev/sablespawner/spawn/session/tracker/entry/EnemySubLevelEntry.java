package dev.sablespawner.spawn.session.tracker.entry;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class EnemySubLevelEntry extends SubLevelEntry {
    private final EnemyProperty property;
    private final UUID target;
    private long FTLChargeStartTime = -1;

    public EnemySubLevelEntry(EnemyProperty property, ServerSubLevel subLevel, UUID target) {
        super(subLevel);
        this.property = property;
        this.target = target;

        this.initialized = this.initialize();
    }

    public boolean initialize() {
        this.initialMass = this.sublevel.getMassTracker().getMass();
        if ( this.property == null ) { return false; }
        if ( this.target == null ) { return false; }

        return this.initialMass != -1;
    }

    public void setFTLCharge() {
        if ( !this.initialized ) { return; }

        if ( this.FTLChargeStartTime == -1 ){
            this.FTLChargeStartTime = getGameTime();
        }
    }
    public void resetFTLCharge() {
        if ( !this.initialized ) { return; }

        this.FTLChargeStartTime = -1;
    }

    public boolean isFTLCharging() {
        if ( !this.initialized ) { return false; }
        if ( isDestroyed() ) { return false; }

        int threshold = this.property.getFTLChargeThreshold();
        if ( threshold == -1 ) { return false; }
        long duration = this.property.getFTLChargeDuration();
        if ( duration == -1 ) { return false; }

        return getMassPercentage() <= this.property.getFTLChargeThreshold();
    }
    public boolean isFTLChargeCompleted() { // 5t
        if ( !this.initialized ) { return false; }
        if ( !isFTLCharging() ) { return false; }

        return ( getGameTime() - this.FTLChargeStartTime ) >= this.property.getFTLChargeDuration();
    }

    public boolean isDestroyed() { // tick
        if ( !this.initialized ) { return false; }

        int threshold = this.property.getDestroyThreshold();
        if ( threshold == -1 ) { return false; }

        return getMassPercentage() <= threshold;
    }
    public boolean isExpired() {
        if ( !this.initialized ) { return true; }
        long lifeTime = this.property.getLifeTime();
        if ( lifeTime == -1 ) { return false; }

        return getExistTime() >= lifeTime;
    }

}
