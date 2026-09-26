package dev.sablespawner.spawn.session.tracker.query;

import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.UUID;
import java.util.function.Predicate;

public class EnemyTrackerQuery {
    private final Object2ObjectOpenHashMap<UUID, EnemySubLevelEntry> tracker;
    private Predicate<EnemySubLevelEntry> entryPredicate = entry -> true;

    public EnemyTrackerQuery(Object2ObjectOpenHashMap<UUID, EnemySubLevelEntry> tracker) { this.tracker = tracker; }

    public EnemyTrackerQuery isExpired() {
        entryPredicate = entryPredicate.and(EnemySubLevelEntry::isExpired);
        return this;
    }
    public EnemyTrackerQuery isDestroyed() {
        entryPredicate = entryPredicate.and(EnemySubLevelEntry::isDestroyed);
        return this;
    }
    public EnemyTrackerQuery isFTLCharging() {
        entryPredicate = entryPredicate.and(EnemySubLevelEntry::isFTLCharging);
        return this;
    }

    public ObjectList<EnemySubLevelEntry> collect() {
        ObjectList<EnemySubLevelEntry> result = new ObjectArrayList<>();
        for ( EnemySubLevelEntry entry : tracker.values() ) {
            if ( entryPredicate.test(entry) ) { result.add(entry); }
        }
        return result;
    }
}
