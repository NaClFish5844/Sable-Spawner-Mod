package dev.sable.sablespawner.util;

import dev.ryanhcode.sable.companion.math.Pose3d;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.Random;

public final class SpawnPatternUtil {
    private SpawnPatternUtil() {}
    private static final Random RANDOM = new Random();

    public enum SpacePattern{
        line,
        ring,
        triangle,
        cube,
        sphere,
        hemisphere
    }
    @Nullable public static ObjectList<Pose3d> generateSpacePattern(
            SpacePattern pattern,
            double spacing,
            int amount,
            Vector3d playerPos,
            double distance,
            Quaterniond orientation
    ) {
        return switch (pattern) {
            case line -> forLine(spacing, amount, playerPos, distance, orientation);
            case ring -> forRing(spacing, amount, playerPos, distance, orientation);
            case triangle -> forTriangle(spacing, amount, playerPos, distance, orientation);
            case cube -> forCube(spacing, amount, playerPos, distance, orientation);
            case sphere -> forSphere(spacing, amount, playerPos, distance, orientation);
            case hemisphere -> forHemisphere(spacing, amount, playerPos, distance, orientation);
        };
    }


    @Nullable public static ObjectList<Pose3d> generateSpacePattern(
            SpacePattern pattern,
            double spacing,
            int amount,
            Vector3d playerPos,
            Vector3d offset
    ) {
        double distance = offset.length();
        Quaterniond orientation = distance == 0
                ? new Quaterniond()
                : new Quaterniond().rotateTo(new Vector3d(0, 0, 1), new Vector3d(offset).normalize());

        return switch (pattern) {
            case line -> forLine(spacing, amount, playerPos, distance, orientation);
            case ring -> forRing(spacing, amount, playerPos, distance, orientation);
            case triangle -> forTriangle(spacing, amount, playerPos, distance, orientation);
            case cube -> forCube(spacing, amount, playerPos, distance, orientation);
            case sphere -> forSphere(spacing, amount, playerPos, distance, orientation);
            case hemisphere -> forHemisphere(spacing, amount, playerPos, distance, orientation);
        };
    }
    @Nullable public static ObjectList<Pose3d> generateRandomSpacePattern(
            SpacePattern pattern,
            double spacing,
            int amount,
            Vector3d playerPos,
            double distance
    ) {
        Quaterniond orientation = randomOrientation();

        return generateSpacePattern(pattern, spacing, amount, playerPos, distance, orientation);
    }
    @Nullable public static ObjectList<Pose3d> generateRandomSpacePattern(
            double spacing,
            int amount,
            Vector3d playerPos,
            double distance
    ) {
        SpacePattern pattern = SpacePattern.values()[RANDOM.nextInt(SpacePattern.values().length)];
        Quaterniond orientation = randomOrientation();

        return generateSpacePattern(pattern, spacing, amount, playerPos, distance, orientation);
    }

    private static Vector3d parseOrigin(Vector3d playerPos, double distanceFromTarget, Quaterniond orientationFromTarget) {
        Vector3d normal = orientationFromTarget.transform(new Vector3d(0, 0, 1)).normalize();
        Vector3d offset = new Vector3d(normal).mul(distanceFromTarget);

        return playerPos.add(offset);
    }
    private static Vector3d parseOrigin(Vector3d playerPos, Vector3d offset) {
        return playerPos.add(offset);
    }
    private static Quaterniond randomOrientation() {
        return new Quaterniond().rotationXYZ(
                RANDOM.nextDouble(2) * Math.PI,
                RANDOM.nextDouble(2) * Math.PI,
                RANDOM.nextDouble(2) * Math.PI
        );
    }

    private static Pose3d forPoint(Vector3d playerPos, Vector3d offset, Quaterniond orientation) {
        return new Pose3d(
                parseOrigin(playerPos, offset),
                orientation,
                new Vector3d(),
                new Vector3d(1, 1, 1)
        );
    }
    private static Pose3d forPoint(Vector3d playerPos, double distance, Quaterniond orientation) {
        return new Pose3d(
                parseOrigin(playerPos, distance, orientation),
                orientation,
                new Vector3d(),
                new Vector3d(1, 1, 1)
        );
    }
    private static ObjectList<Pose3d> forLine(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        // WIP
        return forRing(spacing,amount,playerPos,distance,orientationFromTarget);
    }
    private static ObjectList<Pose3d> forRing(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        ObjectList<Pose3d> poses = new ObjectArrayList<>();

        if ( amount < 1 ) { return null; }
        if ( amount == 1 ) { poses.add(forPoint(playerPos, distance, orientationFromTarget)); }
        if ( amount > 1 ) {
            Vector3d normal = orientationFromTarget.transform(new Vector3d(0, 0, 1)).normalize();
            Vector3d ref = Math.abs(normal.y) < 0.99 ? new Vector3d(0, 1, 0) : new Vector3d(1, 0, 0);
            Vector3d u = normal.cross(ref, new Vector3d()).normalize();
            Vector3d v = normal.cross(u, new Vector3d()).normalize();

            double radius = spacing / ( 2 * Math.sin( Math.PI / amount ) );

            Vector3d center = new Vector3d(normal).mul(distance);

            for (int i = 0; i < amount ; i++ ) {
                double angle = 2 * Math.PI * i / amount;
                double cos = Math.cos(angle) * radius;
                double sin = Math.sin(angle) * radius;

                Vector3d origin = new Vector3d(
                        center.x + u.x * cos + v.x * sin,
                        center.y + u.y * cos + v.y * sin,
                        center.z + u.z * cos + v.z * sin
                ).add(playerPos);

                Quaterniond orientationTowardsTarget = new Quaterniond(orientationFromTarget).rotateY(Math.PI);

                Pose3d pose = new Pose3d(
                        origin,
                        orientationTowardsTarget,
                        new Vector3d(),
                        new Vector3d(1, 1, 1)
                );

                poses.add( pose );
            }
        }

        return poses;
    }
    private static ObjectList<Pose3d> forTriangle(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        // WIP
        return forRing(spacing,amount,playerPos,distance,orientationFromTarget);
    }
    private static ObjectList<Pose3d> forCube(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        // WIP
        return forRing(spacing,amount,playerPos,distance,orientationFromTarget);
    }
    private static ObjectList<Pose3d> forSphere(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        // WIP
        return forRing(spacing,amount,playerPos,distance,orientationFromTarget);
    }
    private static ObjectList<Pose3d> forHemisphere(double spacing, int amount, Vector3d playerPos, double distance, Quaterniond orientationFromTarget) {
        // WIP
        return forRing(spacing,amount,playerPos,distance,orientationFromTarget);
    }

    // Pose3d:
    // Vector3d position;
    // Quaterniond orientation;
    // Vector3d rotationPoint;
    // Vector3d scale;
}
