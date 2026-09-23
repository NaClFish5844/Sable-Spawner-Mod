package dev.sablespawner.manager.blueprint;

import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class BlueprintQuery {

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> source;
    private final Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> map;
    private Predicate<BlueprintKey> keyPredicate = k -> true;
    private Predicate<BlueprintEntry> entryPredicate = e -> true;

    public BlueprintQuery(
            Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> source,
            Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> map
    ) {
        this.source = source;
        this.map = map;
    }

    public BlueprintQuery ofName(String name) {
        keyPredicate = keyPredicate.and(k -> Objects.equals(k.name(), name) );
        return this;
    }
    public BlueprintQuery ofLocationType(String location) {
        keyPredicate = keyPredicate.and(k -> Objects.equals(k.source(), location) );
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
    public BlueprintQuery ofFileHash(String hash) {
        keyPredicate = keyPredicate.and(k -> k.fileHash().equals(hash) );
        return this;
    }


    public BlueprintQuery ofSourceModId(DatapackManager.BlueprintSourceModId modId) {
        entryPredicate = entryPredicate.and(e -> e.sourceMod() == modId );
        return this;
    }
    public BlueprintQuery ofSourceModId(String modId) {
        entryPredicate = entryPredicate.and(e -> Objects.equals(e.sourceMod().toString(), modId) );
        return this;
    }
    public BlueprintQuery isBuffered() {
        entryPredicate = entryPredicate.and( BlueprintEntry::isBuffered);
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
    @Nullable public BlueprintEntry get(BlueprintKey key) {
        return source.get(key);
    }
    @Nullable public BlueprintEntry get(PropertyKey propertyKey) {
        return source.get( map.get(propertyKey) );
    }
    @Nullable public Pair<Class<?>, Object> getAsObject(BlueprintKey key) {
        BlueprintEntry entry = source.get(key);
        if ( entry == null ) { return null; }
        if ( entry.isBuffered() ) { return Pair.of( entry.object().getClass(), entry.object() ); }

        return BlueprintLoader.loadBlueprintEntryAsObject(entry);
    }
    @Nullable public Pair<Class<?>, Object> getAsObject(PropertyKey propertyKey) {
        BlueprintKey key = map.get(propertyKey);
        if ( key == null ) { return null; }
        return getAsObject(key);
    }

}

