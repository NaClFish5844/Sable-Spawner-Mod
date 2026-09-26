package dev.sablespawner.spawn.session.tracker.entry;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.UUID;

import static dev.sablespawner.SableSpawnerConfig.DEBRIS_DESPAWN_TIME;
import static dev.sablespawner.SableSpawnerConfig.LONG_DEBRIS_DESPAWN_TIME;

@Getter
public class DebrisSubLevelEntry extends SubLevelEntry {
    private final Cause cause;
    private final SourceSubLevelType type;
    @Nullable private final UUID splitFrom;
    @Nullable private final String sourceName;

    public enum Cause {
        wreck,
        split,
    }
    public enum SourceSubLevelType {
        enemy,
        ally,
        player,
    }

    private DebrisSubLevelEntry(Cause cause, SourceSubLevelType type, ServerSubLevel subLevel, @Nullable UUID splitFrom, @Nullable String sourceName) {
        super(subLevel);
        this.cause = cause;
        this.type = type;
        this.splitFrom = splitFrom;
        this.sourceName = sourceName;
        this.initialized = this.initialize();
    }

    public static DebrisSubLevelEntry ofWreck(SourceSubLevelType type, ServerSubLevel subLevel, String sourceName) {
        return new DebrisSubLevelEntry(Cause.wreck, type, subLevel, null, sourceName );
    }
    public static DebrisSubLevelEntry ofSplit(SourceSubLevelType type, ServerSubLevel subLevel, UUID sourceUUID, String sourceName) {
        return new DebrisSubLevelEntry(Cause.split, type, subLevel, sourceUUID, sourceName);
    }
    public static DebrisSubLevelEntry ofSplit(DebrisSubLevelEntry parent, ServerSubLevel subLevel) {
        return new DebrisSubLevelEntry(parent.cause, parent.type, subLevel, parent.splitFrom, parent.sourceName);
    }

    public boolean initialize() {
        return true;
    }

    @Override public void updateMass() {
        return;
    }
    @Override public double getMassPercentage() {
        return -1;
    }

    public boolean isExpired() {
        if ( this.type == SourceSubLevelType.player ) { return false; }

        if ( this.cause == Cause.wreck ) {
            return isTimedOut(LONG_DEBRIS_DESPAWN_TIME.getAsInt());
        }

        return isTimedOut(DEBRIS_DESPAWN_TIME.getAsInt());
    }

    private boolean isTimedOut(int despawnTime) {
        return despawnTime != -1 && getExistTime() >= despawnTime;
    }
}