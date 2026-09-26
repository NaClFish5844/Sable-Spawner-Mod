package dev.sablespawner.spawn.session.tracker.query;

import dev.sablespawner.spawn.session.tracker.entry.AllySubLevelEntry;
import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.UUID;
import java.util.function.Predicate;

public class AllyTrackerQuery {
    private final Object2ObjectOpenHashMap<UUID, AllySubLevelEntry> tracker;
    private Predicate<AllySubLevelEntry> entryPredicate = entry -> true;

    public AllyTrackerQuery(Object2ObjectOpenHashMap<UUID, AllySubLevelEntry> tracker) { this.tracker = tracker; }

    // 预留：友军过滤条件待玩法接入

    public ObjectList<AllySubLevelEntry> collect() {
        ObjectList<AllySubLevelEntry> result = new ObjectArrayList<>();
        for ( AllySubLevelEntry entry : tracker.values() ) {
            if ( entryPredicate.test(entry) ) { result.add(entry); }
        }
        return result;
    }
}
