package dev.sable.sablespawner.datapack.query;

import dev.sable.sablespawner.datapack.blueprint.PropertyKey;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.sublevel.EnemyProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

public class PropertyQuery {
    private final Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> source;
    private Predicate<AbstractSchematicProperty> keyPredicate = k -> true;
    private Predicate<AbstractSchematicProperty> propertyPredicate = p -> true;

    private final Random RANDOM = new Random();

    public PropertyQuery(Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> source) { this.source = source; }

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

    public PropertyQuery ofName(String name) {
        propertyPredicate = propertyPredicate.and(p -> Objects.equals(p.getSchematicName(), name) );
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
        ArrayList<AbstractSchematicProperty> candidates = collect();
        if (candidates.isEmpty()) { return null; }

        return candidates.get( RANDOM.nextInt(candidates.size()) );
    }

    @Nullable public AbstractSchematicProperty pickEnemy() {
        ArrayList<AbstractSchematicProperty> candidates = collect();
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

    private static long weightOf(AbstractSchematicProperty prop) {
        if (prop instanceof EnemyProperty enemy) { return enemy.getWeight(); }
        return 0;
    }


    // 要改
    public ArrayList<AbstractSchematicProperty> collect() {
        ArrayList<AbstractSchematicProperty> result = new ArrayList<>();
        for (AbstractSchematicProperty s : source) {
            if (propertyPredicate.test(s)) { result.add(s); }
        }
        return result;
    }
}
