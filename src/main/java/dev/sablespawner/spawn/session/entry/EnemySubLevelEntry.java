package dev.sablespawner.spawn.session.entry;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

import static dev.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;

import java.util.Objects;
import java.util.UUID;


public class EnemySubLevelEntry {
    @Getter private final UUID uuid;
    @Getter @Setter private ServerSubLevel sublevel;
    @Nullable @Getter private final EnemyProperty property;
    @Nullable @Getter private final UUID target;
    @Getter private double totalMass = -1;
    @Getter private double massPercentage = 100.0;
    @Getter private final long spawnedGameTick;
    @Getter private long FTLChargeStartTime = -1;
    private final boolean isDebris;
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
    public boolean rebind(@Nullable ServerSubLevelContainer container) {
        if ( container == null ) { return false; }

        ServerSubLevel fresh = (ServerSubLevel) container.getSubLevel(this.uuid);
        if ( fresh == null ) { return false; }

        this.sublevel = fresh;
        return true;
    }

    public boolean isFTLCharging() {
        if ( isDebris() ) { return false; }
        if ( isDestroyed() ) { return false; }

        if ( this.massPercentage <= Objects.requireNonNull(this.property).getFTLChargeThreshold() && this.FTLChargeStartTime == -1 ){
            this.FTLChargeStartTime = getGameTime();
        }
        return this.massPercentage <= this.property.getFTLChargeThreshold();
    }
    public boolean isFTLChargeCompleted() { // 5t
        if ( isDebris() ) { return false; }
        if ( this.FTLChargeStartTime == -1 ) { return false; }

        return ( getGameTime() - this.FTLChargeStartTime ) >= Objects.requireNonNull(this.property).getFTLChargeDuration();
    }
    public boolean isDestroyed() { // tick
        if ( isDebris() ) { return false; }

        return this.massPercentage <= Objects.requireNonNull(this.property).getDestroyThreshold();
    }
    public boolean isExpired() { // tick, scan
        if ( isDebris() ) {
            return getExistTime() >= DEBRIS_DESPAWN_TIME.getAsInt();
        }else{
            return getExistTime() >= Objects.requireNonNull(this.property).getLifeTime();
        }
    }
    public boolean isDebris() {
        return (this.isDebris) || (this.property == null);
    }

    private long getExistTime() {
        return (getGameTime() - this.spawnedGameTick);
    }
    private long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
