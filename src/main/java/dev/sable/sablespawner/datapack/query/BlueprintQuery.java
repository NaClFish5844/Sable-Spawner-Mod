package dev.sable.sablespawner.datapack.query;

import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.blueprint.BlueprintEntry;
import dev.sable.sablespawner.datapack.blueprint.BlueprintKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class BlueprintQuery {
    // template
    // will be rewritten

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> source;
    private Predicate<BlueprintKey> keyPredicate = k -> true;
    private Predicate<BlueprintEntry> entryPredicate = e -> true;

    public BlueprintQuery(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> source) { this.source = source; }

    public BlueprintQuery ofName(String name) {
        keyPredicate = keyPredicate.and(k -> Objects.equals(k.name(), name) );
        return this;
    }
    public BlueprintQuery nameContains(String part) {
        keyPredicate = keyPredicate.and(k -> k.name() != null && k.name().contains(part));
        return this;
    }
    public BlueprintQuery nameContainsIgnoreCase(String part) {
        keyPredicate = keyPredicate.and(k -> k.name() != null
                && k.name().toLowerCase().contains(part.toLowerCase()));
        return this;
    }
    public BlueprintQuery ofSourceModId(DatapackManager.BlueprintSourceModId modId) {
        keyPredicate = keyPredicate.and(k -> k.sourceMod() == modId );
        return this;
    }
    public BlueprintQuery ofSourceModId(String modId) {
        keyPredicate = keyPredicate.and(k -> Objects.equals(k.sourceMod().toString(), modId) );
        return this;
    }


    public BlueprintQuery fromFolder() {
        entryPredicate = entryPredicate.and( BlueprintEntry::isPathRef);
        return this;
    }
    public BlueprintQuery isBuffered() {
        entryPredicate = entryPredicate.and( BlueprintEntry::isBuffered );
        return this;
    }
    public BlueprintQuery notBuffered() {
        entryPredicate = entryPredicate.and( e -> !e.isBuffered() );
        return this;
    }


    public ObjectList<BlueprintKey> collectKeys() {
        ObjectList<BlueprintKey> result = new ObjectArrayList<>();

        for (var entry : source.entrySet()) {
            if (keyPredicate.test(entry.getKey()) && entryPredicate.test(entry.getValue())) {
                result.add(entry.getKey());
            }
        }

        return result;
    }
    public Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> collect() {
        Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> result = new Object2ObjectOpenHashMap<>();

        for (var entry : source.entrySet()) {
            if (keyPredicate.test(entry.getKey()) && entryPredicate.test(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
    @Nullable public BlueprintEntry get(BlueprintKey key) { return source.get(key); }

}
