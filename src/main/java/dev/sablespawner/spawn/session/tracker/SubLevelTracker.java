package dev.sablespawner.spawn.session.tracker;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.sablespawner.spawn.session.tracker.entry.SubLevelEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class SubLevelTracker<T extends SubLevelEntry> {
    protected Object2ObjectOpenHashMap<UUID, T> Tracker = new Object2ObjectOpenHashMap<>();
    private final ObjectList<T> deferredAppender = new ObjectArrayList<>();
    private final ObjectList<T> deferredRemover = new ObjectArrayList<>();

    public T get(UUID uuid) {
        return this.Tracker.get(uuid);
    }
    public int size() {
        return this.Tracker.size();
    }
    public boolean contains(UUID uuid) {
        return this.Tracker.containsKey(uuid);
    }
    public boolean contains(T entry) {
        return this.Tracker.containsValue(entry);
    }

    public void deferredAppenderAdd(T entry) {
        this.deferredAppender.add(entry);
    }
    public void deferredAppenderAddAll(ObjectList<T> list) {
        this.deferredAppender.addAll(list);
    }
    public void deferredAppenderCancel(T entry) {
        this.deferredAppender.remove(entry);
    }
    public void deferredAppenderClear() {
        this.deferredAppender.clear();
    }
    public void executeAppend() {
        for (T entry : deferredAppender) {
            if ( entry.getSublevel().isRemoved() ) { continue; }
            push(entry);
        }
        this.deferredAppender.clear();
    }

    public void deferredRemoverAdd(T entry) {
        this.deferredRemover.add(entry);
    }
    public void deferredRemoverAddAll(ObjectList<T> list) {
        this.deferredRemover.addAll(list);
    }
    public void deferredRemoverCancel(T entry) {
        this.deferredRemover.remove(entry);
    }
    public void deferredRemoverClear() {
        this.deferredRemover.clear();
    }
    public void executeRemove() {
        for (T entry : deferredRemover) {
            if ( !Tracker.containsKey(entry.getUuid()) ) { continue; }
            popWithDelete(entry);
        }
        this.deferredRemover.clear();
    }

    public void executeTrackerUpdate() {
        executeAppend();
        executeRemove();
    }

    public void rebindAll(@Nullable ServerSubLevelContainer container) {
        this.Tracker.values().removeIf(entry -> !entry.rebind(container) );
    }

    public void push(T entry) {
        this.Tracker.put( entry.getUuid(), entry );
    }
    public T popWithDelete(T entry) {
        entry.removeSubLevel();
        return pop(entry.getUuid());
    }
    public T pop(T entry) {
        return pop(entry.getUuid());
    }
    public T pop(UUID uuid) {
        return this.Tracker.remove(uuid);
    }

}
