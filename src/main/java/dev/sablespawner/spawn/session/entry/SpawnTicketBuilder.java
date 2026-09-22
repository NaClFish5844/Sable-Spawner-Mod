package dev.sablespawner.spawn.session.entry;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public final class SpawnTicketBuilder {
    private static final Random RANDOM = new Random();

    @Nullable public static SpawnTicket of(PropertyKey propertyKey, UUID targetPlayer) {
        EnemyProperty property = (EnemyProperty) getDatapackManager().propertyQuery().get(propertyKey);
        if ( property == null ) { return null; }

        return new SpawnTicket(
                propertyKey,
                property,
                targetPlayer,
                RANDOM.nextInt( property.getMaxSpawnAmount() ) + 1,
                buildDistance(property),
                flushSpawnDelay(property)
        );
    }

    public static double buildDistance(EnemyProperty property) {
        double base = property.getMinSpawnDistance();
        double max = property.getMaxSpawnDistance();
        double deviation = RANDOM.nextGaussian(0.5,0.15) * (max - base);
        return Math.min(max, Math.max(base, base + deviation));
    }
    private static long flushSpawnDelay(EnemyProperty property) {
        long minInterval = property.getMinSpawnInterval();
        long maxInterval = property.getMaxSpawnInterval();

        return randomInRange( minInterval, maxInterval );
    }
    private static long randomInRange(long min, long max ) {
        long upperBound;
        long lowerBound;
        if ( min <= 0 || max <= 0 ) { return 2147483647; }
        upperBound = Math.max( min, max ) + 1 ;
        lowerBound = Math.min( min, max );

        return lowerBound + RANDOM.nextLong( upperBound - lowerBound );
    }

    private static DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }

}
