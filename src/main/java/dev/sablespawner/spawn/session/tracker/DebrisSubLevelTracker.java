package dev.sablespawner.spawn.session.tracker;

import dev.sablespawner.spawn.session.tracker.entry.DebrisSubLevelEntry;
import dev.sablespawner.spawn.session.tracker.query.DebrisTrackerQuery;

public class DebrisSubLevelTracker extends SubLevelTracker<DebrisSubLevelEntry> {
    private final boolean placeholder = true;

    public DebrisTrackerQuery query()  { return new DebrisTrackerQuery(this.Tracker); }
}
