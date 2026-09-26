package dev.sablespawner.spawn.session.tracker;

import dev.sablespawner.spawn.session.tracker.entry.AllySubLevelEntry;
import dev.sablespawner.spawn.session.tracker.query.AllyTrackerQuery;

public class AllySubLevelTracker extends SubLevelTracker<AllySubLevelEntry> {
    public void updateMass() {
        for ( AllySubLevelEntry entry : this.Tracker.values() ) {
            entry.updateMass();
        }
    }

    public AllyTrackerQuery query()  { return new AllyTrackerQuery(this.Tracker); }
}
