package dev.sablespawner.spawn.session.spawnqueue;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import it.unimi.dsi.fastutil.Pair;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public final class SpawnTicketBuilder {
    private static final Random RANDOM = new Random();

    @Nullable public static SpawnTicket of(PropertyKey propertyKey, UUID targetPlayer) {
        EnemyProperty property = (EnemyProperty) getDatapackManager().propertyQuery().get(propertyKey);
        if ( property == null ) { return null; }

        double distance = buildSpawnDistance(property);
        if ( distance == -1 ) { return null; }

        long delay = buildSpawnDelay(property);
        if ( delay == -1 ) { return null; }

        int amount = buildSpawnAmount(property);
        if ( amount == -1 ) { return null; }

        return new SpawnTicket(
                propertyKey,
                property,
                targetPlayer,
                amount,
                distance,
                delay
        );
    }

    private static double buildSpawnDistance(EnemyProperty property) {
        Pair<Integer, Integer> range = property.getSpawnDistanceRange();
        if ( range == null ) { return -1; }

        double base = range.left();
        double max = range.right();
        double deviationFactor = RANDOM.nextGaussian(0.5,0.15);
        deviationFactor = Math.max(0, deviationFactor);
        deviationFactor = Math.min(1, deviationFactor);

        double deviation = (max - base) * deviationFactor;

        return base + deviation;
    }
    private static long buildSpawnDelay(EnemyProperty property) {
        Pair<Integer, Integer> range = property.getSpawnIntervalRange();
        if ( range == null ) { return -1; }

        long minInterval = range.left();
        long maxInterval = range.right();

        return minInterval + RANDOM.nextLong( maxInterval - minInterval );
    }
    private static int buildSpawnAmount(EnemyProperty property) {
        int amount = property.getMaxSpawnAmount();
        if ( amount == -1 ) { return -1; }

        return RANDOM.nextInt( property.getMaxSpawnAmount() ) + 1;
    }

    private static DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }

}
