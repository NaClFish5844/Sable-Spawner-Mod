package dev.sablespawner.blockentity.tracker;

import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import dev.sablespawner.blockentity.tracker.query.FleetBaseCoreBlockEntityQuery;

public class FleetBaseCoreBlockEntityTracker extends BlockEntityTracker<FleetBaseCoreBlockEntity> {
    public static final FleetBaseCoreBlockEntityTracker INSTANCE = new FleetBaseCoreBlockEntityTracker();

    public FleetBaseCoreBlockEntityQuery query() {
        return new FleetBaseCoreBlockEntityQuery(TRACKER);
    }

    @Override public void executeAppend() {
        for (FleetBaseCoreBlockEntity entry : deferredAppender) {
            if ( entry.isRemoved() ) { continue; }
            push(entry.getCoreId(), entry);
        }
        this.deferredAppender.clear();
    }
    @Override public void executeRemove() {
        for (FleetBaseCoreBlockEntity entry : deferredRemover) {
            if ( !TRACKER.containsKey(entry.getCoreId()) ) { continue; }
            pop( entry.getCoreId() );
        }
        this.deferredRemover.clear();
    }
}
