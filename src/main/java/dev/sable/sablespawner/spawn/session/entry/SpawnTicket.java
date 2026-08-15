package dev.sable.sablespawner.spawn.session.entry;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.spawn.Spawner;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class SpawnTicket {
    @Getter private final EnemyProperty property;
    @Getter private final int amount;

    @Getter private final UUID target;

    @Getter private final double distanceFromTarget;
    @Getter private final Quaterniond orientationFromTarget;

    @Getter private final ArrayList<Vec3> spawnOrigins = new ArrayList<>();
    @Getter private final long scheduledSpawnTime;

    private final Random RANDOM = new Random();


    public SpawnTicket(EnemyProperty property, UUID target, long pushTime) {
        this.property = property;
        this.amount = RANDOM.nextInt( this.property.getMaxSpawnAmount() ) + 1;

        this.target = target;

        this.distanceFromTarget = getDistance();
        this.orientationFromTarget = getOrientation();

        this.scheduledSpawnTime = getScheduledSpawnTime(pushTime);

        generateSpawnOrigins();
    }

    public ArrayList<BlueprintPlacementPlan> getBlueprintPlacementPlans(Vec3 playerPos) {
        SableBlueprint bp = Spawner.getSableBlueprint(this.property);
        if (bp == null) { return new ArrayList<>(); }

        ArrayList<BlueprintPlacementPlan> plans = new ArrayList<>(this.spawnOrigins.size());

        for (Vec3 origin : this.spawnOrigins) {
            Vec3 abs = playerPos.add(origin);
            Pose3d pose = new Pose3d(
                    new Vector3d(abs.x, abs.y, abs.z),
                    getOrientationToTarget(),
                    new Vector3d(),
                    new Vector3d(1, 1, 1)
            );
            plans.add(BlueprintPlacementPlan.forPose(bp, pose));
        }
        return plans;
    }


    private void generateSpawnOrigins() {
        SableBlueprint bp = Spawner.getSableBlueprint(this.property);
        if ( bp == null ) { return; }

        double spacing = getMaxDim( bp.canonicalBounds() ) * 1.1 ;

        Vector3d normal = orientationFromTarget.transform(new Vector3d(0, 0, 1)).normalize();
        Vector3d ref = Math.abs(normal.y) < 0.99 ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
        Vector3d u = normal.cross(ref, new Vector3d()).normalize();
        Vector3d v = normal.cross(u, new Vector3d()).normalize();

        double radius = this.amount > 1 ? spacing / ( 2 * Math.sin( Math.PI / this.amount ) ) : 0;

        Vector3d center = new Vector3d(normal).mul(this.distanceFromTarget);

        for (int i = 0; i < this.amount ; i++ ) {
            double angle = 2 * Math.PI * i / this.amount;
            double cos = Math.cos(angle) * radius;
            double sin = Math.sin(angle) * radius;
            this.spawnOrigins.add(new Vec3(
                    center.x + u.x * cos + v.x * sin,
                    center.y + u.y * cos + v.y * sin,
                    center.z + u.z * cos + v.z * sin
            ));
        }
    }

    private double getMaxDim( BoundingBox3d box ) {
        return Math.max( box.size().x, Math.max( box.size().y, box.size().z ) );
    }
    private long getScheduledSpawnTime( long pushTime ) {
        return  pushTime +
                property.getMinSpawnInterval() +
                RANDOM.nextLong( range( property.getMinSpawnInterval(), property.getMaxSpawnInterval() ));
    }
    private double getDistance() {
        double base = property.getMinSpawnDistance();
        double max = property.getMaxSpawnDistance();
        double deviation = RANDOM.nextGaussian(0.5,0.15) * (max - base);
        return Math.min(max, Math.max(base, base + deviation));
    }
    private Quaterniond getOrientation() {
        return new Quaterniond().rotationXYZ(
                RANDOM.nextDouble(2) * Math.PI,
                RANDOM.nextDouble(2) * Math.PI,
                RANDOM.nextDouble(2) * Math.PI
        );
    }
    private Quaterniond getOrientationToTarget() {
        return this.orientationFromTarget.rotateY(Math.PI);
    }

    private long range(long min, long max ) {
        return max - min <= 0 ? min : max - min;
    }

}
