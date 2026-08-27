package dev.sable.sablespawner.spawn.session.entry;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.sable.sablespawner.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.player.PlayerStatus;
import dev.sable.sablespawner.spawn.Spawner;
import dev.sable.sablespawner.util.SpawnPatternUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import org.joml.Vector3d;

import java.util.Random;
import java.util.UUID;

public class SpawnTicket {
    @Getter private final EnemyProperty property;
    @Getter private final int amount;

    @Getter private final UUID target;

    @Getter private final double distanceFromTarget;

    @Getter private final long spawnDelay;

    private final Random RANDOM = new Random();


    public SpawnTicket(EnemyProperty property, UUID target ) {
        this.property = property;
        this.amount = RANDOM.nextInt( this.property.getMaxSpawnAmount() ) + 1;
        this.target = target;
        this.distanceFromTarget = getDistance();
        this.spawnDelay = flushSpawnDelay();
    }

    public ObjectList<BlueprintPlacementPlan> getBlueprintPlacementPlans(Vector3d playerPos) {
        SableBlueprint bp = Spawner.getSableBlueprint(this.property);
        if (bp == null) { return new ObjectArrayList<>(); }

        ObjectList<Pose3d> poses = SpawnPatternUtil.generateRandomSpacePattern(
                getSpacing(bp),
                this.amount,
                playerPos,
                this.distanceFromTarget
                );
        if ( poses == null || poses.isEmpty() ) { return new ObjectArrayList<>(); }

        ObjectList<BlueprintPlacementPlan> plans = new ObjectArrayList<>( poses.size() );

        for (Pose3d pose : poses) {
            plans.add(BlueprintPlacementPlan.forPose(bp, pose));
        }
        return plans;
    }

    public long getScheduledSpawnTime( PlayerStatus playerStatus ) {
        return playerStatus.getOutProtectionTime() + this.spawnDelay;
    }
    private long flushSpawnDelay() {
        long minInterval = property.getMinSpawnInterval();
        long maxInterval = property.getMaxSpawnInterval();

        return randomInRange( minInterval, maxInterval );
    }

    private double getSpacing (SableBlueprint blueprint) {
        if ( blueprint == null ) { return -1; }
        double maxDimension = Math.max(
                blueprint.canonicalBounds().size().x,
                Math.max(
                        blueprint.canonicalBounds().size().y,
                        blueprint.canonicalBounds().size().z )
        );
        return maxDimension * 1.1 ;
    }

    private double getDistance() {
        double base = property.getMinSpawnDistance();
        double max = property.getMaxSpawnDistance();
        double deviation = RANDOM.nextGaussian(0.5,0.15) * (max - base);
        return Math.min(max, Math.max(base, base + deviation));
    }

    private long randomInRange(long min, long max ) {
        long upperBound;
        long lowerBound;
        if ( min <= 0 || max <= 0 ) { return 2147483647; }
        upperBound = Math.max( min, max ) + 1 ;
        lowerBound = Math.min( min, max );

        return lowerBound + RANDOM.nextLong( upperBound - lowerBound );
    }

}
