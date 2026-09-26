package dev.sablespawner.spawn.session.tracker.query;

import dev.sablespawner.spawn.session.tracker.entry.DebrisSubLevelEntry;
import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.UUID;
import java.util.function.Predicate;

public class DebrisTrackerQuery {
    private final Object2ObjectOpenHashMap<UUID, DebrisSubLevelEntry> tracker;
    private Predicate<DebrisSubLevelEntry> entryPredicate = entry -> true;

    public DebrisTrackerQuery(Object2ObjectOpenHashMap<UUID, DebrisSubLevelEntry> tracker) { this.tracker = tracker; }

    public DebrisTrackerQuery isExpired() {
        entryPredicate = entryPredicate.and(DebrisSubLevelEntry::isExpired);
        return this;
    }
    public DebrisTrackerQuery ofCause(DebrisSubLevelEntry.Cause cause) {
        entryPredicate = entryPredicate.and(entry -> entry.getCause() == cause);
        return this;
    }
    public DebrisTrackerQuery ofType(DebrisSubLevelEntry.SourceSubLevelType type) {
        entryPredicate = entryPredicate.and(entry -> entry.getType() == type);
        return this;
    }

    public ObjectList<DebrisSubLevelEntry> collect() {
        ObjectList<DebrisSubLevelEntry> result = new ObjectArrayList<>();
        for ( DebrisSubLevelEntry entry : tracker.values() ) {
            if ( entryPredicate.test(entry) ) { result.add(entry); }
        }
        return result;
    }
}
