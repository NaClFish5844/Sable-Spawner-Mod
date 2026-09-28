package dev.sablespawner.util;

import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Collection;

public final class BoxUtil {

    // 子空间方块的"存储空间"包围盒：合并其 plot 内已加载区块的实时块包围盒（方块搬运会实时更新）
    @Nullable public static BoundingBox3i subLevelStorageBoundBox(ServerSubLevel subLevel) {
        BoundingBox3i result = null;
        for ( PlotChunkHolder holder : subLevel.getPlot().getLoadedChunks() ) {
            BoundingBox3ic local = holder.getBoundingBox();
            if ( local == null ) { continue; }

            ChunkPos pos = holder.getPos();
            BoundingBox3i moved = local.move(pos.getMinBlockX(), 0, pos.getMinBlockZ(), new BoundingBox3i(0, 0, 0, 0, 0, 0));
            result = result == null ? moved : result.expandTo(moved, result);
        }
        return result;
    }
    public static boolean restoreInside(BoundingBox3i storageBBox, BlockPos storageAnchor, BlockPos targetAnchor, BoundingBox3i targetBBox) {
        return inside(restoreBoundBox(storageBBox, storageAnchor, targetAnchor), targetBBox);
    }

    public static BoundingBox3i boundingBoxOf(Collection<BlockPos> blocks) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for ( BlockPos pos : blocks ) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new BoundingBox3i(minX, minY, minZ, maxX, maxY, maxZ);
    }
    public static boolean inside(BoundingBox3i inner, BoundingBox3i outer) {
        return inner.minX() >= outer.minX() && inner.maxX() <= outer.maxX()
                && inner.minY() >= outer.minY() && inner.maxY() <= outer.maxY()
                && inner.minZ() >= outer.minZ() && inner.maxZ() <= outer.maxZ();
    }
    public static BoundingBox3i restoreBoundBox(BoundingBox3i storageBBox, BlockPos storageAnchor, BlockPos targetAnchor) {
        return storageBBox.move(
                targetAnchor.getX() - storageAnchor.getX(),
                targetAnchor.getY() - storageAnchor.getY(),
                targetAnchor.getZ() - storageAnchor.getZ(),
                new BoundingBox3i(0, 0, 0, 0, 0, 0));
    }

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
