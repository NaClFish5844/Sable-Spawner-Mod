package dev.sable.sablespawner.manager.datapack.query;

import dev.sable.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class PropertyQuery {
    private final Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> source;
    private Predicate<PropertyKey> keyPredicate = k -> true;
    private Predicate<AbstractSchematicProperty> propertyPredicate = p -> true;

    private final Random RANDOM = new Random();

    public PropertyQuery(Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> source) { this.source = source; }

    public PropertyQuery ofPath(String path) {
        keyPredicate = keyPredicate.and(k -> Objects.equals(k.path(), path) );
        return this;
    }
    public PropertyQuery nameContains(String part) {
        keyPredicate = keyPredicate.and(k -> k.getName().contains(part));
        return this;
    }
    public PropertyQuery nameContainsIgnoreCase(String part) {
        keyPredicate = keyPredicate.and(k -> k.getName().toLowerCase().contains(part.toLowerCase()));
        return this;
    }

    public PropertyQuery isNaturalSpawn() {
        propertyPredicate = propertyPredicate.and(p ->{
                if ( !(p instanceof EnemyProperty enemy) ) { return false; }
                return enemy.isNaturalSpawn();
                });
        return this;
    }

    public PropertyQuery isEnemy() {
        propertyPredicate = propertyPredicate.and(p -> p.getSublevelType() == AbstractSchematicProperty.SublevelType.enemy);
        return this;
    }
    public PropertyQuery isAlly() {
        propertyPredicate = propertyPredicate.and(p -> p.getSublevelType() == AbstractSchematicProperty.SublevelType.ally);
        return this;
    }
    public PropertyQuery isPrefab() {
        propertyPredicate = propertyPredicate.and(p -> p.getSublevelType() == AbstractSchematicProperty.SublevelType.prefab);
        return this;
    }

    public PropertyQuery isWarship() {
        propertyPredicate = propertyPredicate.and(p -> p.getSublevelFunction() == AbstractSchematicProperty.SublevelFunction.warship);
        return this;
    }
    public PropertyQuery isCargo() {
        propertyPredicate = propertyPredicate.and(p -> p.getSublevelFunction() == AbstractSchematicProperty.SublevelFunction.cargo);
        return this;
    }

    public PropertyQuery ofWorldLevel(int playerScoreLevel) {
        propertyPredicate = propertyPredicate.and(p -> {
            if (!(p instanceof EnemyProperty enemy)) { return false; }
            ArrayList<Integer> levels = enemy.getAvailableWorldLevel();
            return levels == null || levels.isEmpty() || levels.contains(playerScoreLevel);
        });
        return this;
    }
    public PropertyQuery ofDimension(ServerLevel level) {
        String dimensionName = level.dimension().location().toString();
        propertyPredicate = propertyPredicate.and(p -> {
            if (!(p instanceof EnemyProperty enemy)) { return false; }
            ArrayList<String> dimensions = enemy.getAvailableDimension();
            return dimensions == null || dimensions.isEmpty() || dimensions.contains(dimensionName);
        });
        return this;
    }

    @Nullable public AbstractSchematicProperty pickRandomly() {
        List<AbstractSchematicProperty> candidates = collect().values().stream().toList();
        if (candidates.isEmpty()) { return null; }

        return candidates.get( RANDOM.nextInt(candidates.size()) );
    }
    @Nullable public AbstractSchematicProperty pickEnemy() {
        List<AbstractSchematicProperty> candidates = collect().values().stream().toList();
        if (candidates.isEmpty()) { return null; }

        long totalWeight = 0;
        for (AbstractSchematicProperty s : candidates) { totalWeight += weightOf(s); }
        if (totalWeight <= 0) { return candidates.get(RANDOM.nextInt(candidates.size())); }

        long r = RANDOM.nextLong(totalWeight);

        for (AbstractSchematicProperty s : candidates) {
            if ( (r -= weightOf(s)) < 0) { return s; }
        }

        return candidates.getLast();
    }

    public ObjectList<PropertyKey> collectKeys() {
        ObjectList<PropertyKey> result = new ObjectArrayList<>();

        for (var entry : source.entrySet()) {
            if (keyPredicate.test(entry.getKey()) && propertyPredicate.test(entry.getValue())) {
                result.add(entry.getKey());
            }
        }

        return result;
    }
    public Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> collect() {
        Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> result = new Object2ObjectOpenHashMap<>();

        for (Map.Entry<PropertyKey, AbstractSchematicProperty> entry : source.entrySet() ) {
            if (keyPredicate.test(entry.getKey()) && propertyPredicate.test(entry.getValue())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
    @Nullable public AbstractSchematicProperty get(PropertyKey key) { return source.get(key); }

    private static long weightOf(AbstractSchematicProperty prop) {
        if (prop instanceof EnemyProperty enemy) { return enemy.getWeight(); }
        return 0;
    }

}
