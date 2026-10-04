package dev.sablespawner.blockentity.tracker.query;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class BlockEntityQuery<T> {
    private final Object2ObjectOpenHashMap<UUID, T> source;
    protected Predicate<T> predicate = entry -> true;

    public BlockEntityQuery(Object2ObjectOpenHashMap<UUID, T> source) { this.source = source; }

    public Object2ObjectOpenHashMap<UUID, T> collect() {
        Object2ObjectOpenHashMap<UUID, T> result = new Object2ObjectOpenHashMap<>();

        for ( Map.Entry<UUID, T> entry : source.entrySet() ) {
            if ( predicate.test(entry.getValue()) ) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
    @Nullable public T first() {
        for ( T entry : source.values() ) {
            if ( predicate.test(entry) ) { return entry; }
        }
        return null;
    }
    public boolean anyMatch() {
        return first() != null;
    }

}
