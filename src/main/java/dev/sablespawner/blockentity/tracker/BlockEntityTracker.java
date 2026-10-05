package dev.sablespawner.blockentity.tracker;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public abstract class BlockEntityTracker<T extends BlockEntity> {
    protected Object2ObjectOpenHashMap<UUID, T> TRACKER = new Object2ObjectOpenHashMap<>();
    protected final ObjectList<T> deferredAppender = new ObjectArrayList<>();
    protected final ObjectList<T> deferredRemover = new ObjectArrayList<>();

    public T get(UUID uuid) {
        return this.TRACKER.get(uuid);
    }
    public int size() {
        return this.TRACKER.size();
    }
    public boolean contains(UUID uuid) {
        return this.TRACKER.containsKey(uuid);
    }
    public boolean contains(T entry) {
        return this.TRACKER.containsValue(entry);
    }
    public void clear() {
        this.deferredAppender.clear();
        this.deferredRemover.clear();
        this.TRACKER.clear();
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
    public abstract void executeAppend();

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
    public abstract void executeRemove();

    public void executeTrackerUpdate() {
        executeAppend();
        executeRemove();
    }

    public void push(UUID uuid, T entry) {
        this.TRACKER.put( uuid, entry );
    }
    public T pop(UUID uuid) {
        return this.TRACKER.remove(uuid);
    }

}
