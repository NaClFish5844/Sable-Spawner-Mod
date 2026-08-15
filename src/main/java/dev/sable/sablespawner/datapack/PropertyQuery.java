package dev.sable.sablespawner.datapack;

import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;

public class PropertyQuery {
    private final Collection<AbstractSchematicProperty> source;
    private Predicate<AbstractSchematicProperty> predicate = s -> true;

    private final Random RANDOM = new Random();

    public PropertyQuery(Collection<AbstractSchematicProperty> source) { this.source = source; }

    public PropertyQuery isNaturalSpawn() {
        predicate = predicate.and(s ->{
                if ( !(s instanceof EnemyProperty enemy) ) { return false; }
                return enemy.isNaturalSpawn();
                });
        return this;
    }

    public PropertyQuery isEnemy() {
        predicate = predicate.and(s -> s.getSublevelType() == AbstractSchematicProperty.SublevelType.enemy);
        return this;
    }
    public PropertyQuery isAlly() {
        predicate = predicate.and(s -> s.getSublevelType() == AbstractSchematicProperty.SublevelType.ally);
        return this;
    }
    public PropertyQuery isPrefab() {
        predicate = predicate.and(s -> s.getSublevelType() == AbstractSchematicProperty.SublevelType.prefab);
        return this;
    }

    public PropertyQuery isWarship() {
        predicate = predicate.and(s -> s.getSublevelFunction() == AbstractSchematicProperty.SublevelFunction.warship);
        return this;
    }
    public PropertyQuery isCargo() {
        predicate = predicate.and(s -> s.getSublevelFunction() == AbstractSchematicProperty.SublevelFunction.cargo);
        return this;
    }

    public PropertyQuery ofWorldLevel(int playerScoreLevel) {
        predicate = predicate.and(s -> {
            if (!(s instanceof EnemyProperty enemy)) { return false; }
            ArrayList<Integer> levels = enemy.getAvailableWorldLevel();
            return levels == null || levels.isEmpty() || levels.contains(playerScoreLevel);
        });
        return this;
    }
    public PropertyQuery ofDimension(ServerLevel level) {
        String dimensionName = level.dimension().location().toString();
        predicate = predicate.and(s -> {
            if (!(s instanceof EnemyProperty enemy)) { return false; }
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

    private static long weightOf(AbstractSchematicProperty s) {
        if (s instanceof EnemyProperty enemy) { return enemy.getWeight(); }
        return 0;
    }


    public ArrayList<AbstractSchematicProperty> collect() {
        ArrayList<AbstractSchematicProperty> result = new ArrayList<>();
        for (AbstractSchematicProperty s : source) {
            if (predicate.test(s)) { result.add(s); }
        }
        return result;
    }
}
