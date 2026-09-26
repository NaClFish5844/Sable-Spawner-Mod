package dev.sablespawner.spawn.session.tracker.entry;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.SableSpawner;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.UUID;

@Getter
public abstract class SubLevelEntry {
    protected final UUID uuid;
    @Setter protected ServerSubLevel sublevel;
    protected double initialMass = -1;
    protected double mass;
    protected long spawnedGameTick;
    protected boolean initialized;

    protected SubLevelEntry(ServerSubLevel subLevel) {
        this.uuid = subLevel.getUniqueId();
        this.sublevel = subLevel;
        this.spawnedGameTick = getGameTime();
    }

    public abstract boolean initialize();
    public void updateMass() {
        if ( !this.initialized ) { return; }
        if ( this.initialMass <= 0 ) { return; }

        this.mass = this.sublevel.getSelfMassTracker().getMass();
    }
    public double getMassPercentage() {
        return this.mass / this.initialMass * 100.0;
    }

    public void resetExistTime() {
        this.spawnedGameTick = getGameTime();
    }
    public long getExistTime() {
        return getGameTime() - this.spawnedGameTick;
    }
    public abstract boolean isExpired();

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

    protected long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
}
