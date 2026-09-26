package dev.sablespawner.spawn.session.tracker;

import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import dev.sablespawner.spawn.session.tracker.query.EnemyTrackerQuery;

public class EnemySubLevelTracker extends SubLevelTracker<EnemySubLevelEntry> {
    public void updateMass() {
        for ( EnemySubLevelEntry entry : this.Tracker.values() ) {
            entry.updateMass();
        }
    }

    public EnemyTrackerQuery query()  { return new EnemyTrackerQuery(this.Tracker); }
}
