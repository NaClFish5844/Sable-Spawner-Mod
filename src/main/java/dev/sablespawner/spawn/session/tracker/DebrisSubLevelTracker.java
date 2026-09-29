package dev.sablespawner.spawn.session.tracker;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.spawn.session.tracker.entry.DebrisSubLevelEntry;
import dev.sablespawner.spawn.session.tracker.entry.SubLevelEntry;
import dev.sablespawner.spawn.session.tracker.query.DebrisTrackerQuery;
import dev.sablespawner.util.BoxUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.sablespawner.util.AccessUtil.*;

public class DebrisSubLevelTracker extends SubLevelTracker<DebrisSubLevelEntry> {
    ServerSubLevelContainer CONTAINER;
    private final Consumer<DebrisSubLevelEntry> onDebrisCreated;
    private final Predicate<UUID> isTracked;

    public DebrisSubLevelTracker(ServerSubLevelContainer container, Consumer<DebrisSubLevelEntry> onDebrisCreated, Predicate<UUID> isTracked) {
        this.CONTAINER = container;
        this.onDebrisCreated = onDebrisCreated;
        this.isTracked = isTracked;
    }

    @Override public void rebindAll(@Nullable ServerSubLevelContainer container) {
        this.TRACKER.values().removeIf(entry -> !entry.rebind(container) );
        this.CONTAINER = container;
        this.pendingSplits.clear();
    }

    public record PendingSplit(
            @Nullable SubLevelEntry parentEntry,
            SubLevel parentSubLevel,
            long appendTime,
            Collection<BlockPos> blocks,
            BoundingBox3dc parentBounds
    ) {}
    private final ObjectList<PendingSplit> pendingSplits = new ObjectArrayList<>();

    public void pushPendingSplit(PendingSplit pendingSplit) {
        this.pendingSplits.add(pendingSplit);
    }
    public void collectPendingDebris() {
        if ( CONTAINER == null ) { return; }

        ObjectOpenHashSet<UUID> matchedThisTick = new ObjectOpenHashSet<>();
        for ( int i = pendingSplits.size() - 1; i >= 0; i-- ) {
            PendingSplit pending = pendingSplits.get(i);
            if ( getGameTime() - pending.appendTime() > 2 ) { pendingSplits.remove(i); continue; }

            ServerSubLevel debris = findMatchedDebris(pending, matchedThisTick);
            if ( debris == null ) { continue; }

            pendingSplits.remove(i);
            matchedThisTick.add(debris.getUniqueId());
            onDebrisCreated.accept( DebrisSubLevelEntry.ofSplit(pending.parentEntry(), pending.parentSubLevel(), debris) );
        }
    }
    @Nullable public ServerSubLevel findMatchedDebris(PendingSplit pending, ObjectSet<UUID> excluded) {
        if ( CONTAINER == null ) { return null; }

        for ( ServerSubLevel subLevel : CONTAINER.getAllSubLevels() ) {
            if ( subLevel.isRemoved() ) { continue; }
            if ( isTracked.test(subLevel.getUniqueId()) ) { continue; }
            if ( excluded.contains(subLevel.getUniqueId()) ) { continue; }

            if ( !BoxUtil.intersects( subLevel.boundingBox(), pending.parentBounds() ) ) { continue; }

            BoundingBox3i storageBBox = BoxUtil.subLevelStorageBoundBox(subLevel);
            if ( storageBBox == null ) { continue; }

            BlockPos anchor = pending.blocks().iterator().next();
            BoundingBox3i blocksBBox = BoxUtil.boundingBoxOf(pending.blocks());

            if ( !BoxUtil.restoreInside(storageBBox, subLevel.getPlot().getCenterBlock(), anchor, blocksBBox) ) { continue; }

            return subLevel;
        }
        return null;
    }


    public DebrisTrackerQuery query()  { return new DebrisTrackerQuery(this.TRACKER); }

}
