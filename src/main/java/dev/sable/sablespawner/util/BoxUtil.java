package dev.sable.sablespawner.util;

import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import net.minecraft.world.phys.AABB;

public final class BoxUtil {
    private BoxUtil() {}

    public static boolean intersects(AABB a, AABB b) {
        return intersects6(
                a.minX, a.minY, a.minZ, a.maxX, a.maxY, a.maxZ,
                b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
    }

    public static boolean intersects(AABB a, BoundingBox3dc b) {
        return intersects6(
                a.minX, a.minY, a.minZ, a.maxX, a.maxY, a.maxZ,
                b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ());
    }

    public static boolean intersects(BoundingBox3dc a, AABB b) {
        return intersects6(
                a.minX(), a.minY(), a.minZ(), a.maxX(), a.maxY(), a.maxZ(),
                b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
    }

    public static boolean intersects(BoundingBox3dc a, BoundingBox3dc b) {
        return intersects6(
                a.minX(), a.minY(), a.minZ(), a.maxX(), a.maxY(), a.maxZ(),
                b.minX(), b.minY(), b.minZ(), b.maxX(), b.maxY(), b.maxZ());
    }

    private static boolean intersects6(
            double minX1, double minY1, double minZ1, double maxX1, double maxY1, double maxZ1,
            double minX2, double minY2, double minZ2, double maxX2, double maxY2, double maxZ2) {
        return minX1 <= maxX2 && minX2 <= maxX1
                && minY1 <= maxY2 && minY2 <= maxY1
                && minZ1 <= maxZ2 && minZ2 <= maxZ1;
    }
}
