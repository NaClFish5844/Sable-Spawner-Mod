package dev.sable.sablespawner;

import dev.sable.sablespawner.datapack.property.EnemyProperty;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class EnemyControl {
    public static final int SAMPLE_INTERVAL = 5 * 20 ;

    public boolean isEnemyNearby(ServerPlayer Player){
        EntityDimensions playerDimension = Player.getDimensions(Pose.STANDING);
        Vec3 playerWorldPos = Player.position();
        int enemy_detection_distance = SableSpawnerConfig.ENEMY_DETECTION_DISTANCE.getAsInt();
        AABB cubicBoundary = AABB.ofSize(playerWorldPos,enemy_detection_distance,enemy_detection_distance,enemy_detection_distance);

        // 首先 人要在太空
        // 其次 获取人的位置
        // 然后 获取敌人的位置 测距
        // 为了防止性能开销过大 可以先用enemy_detection_distance为半径画个方形 框定再判断
        // 当然如果敌人太多就没办法了 显然那会导致卡顿
        return true;
    }
    public boolean spawnEnemy(ServerPlayer Player, EnemyProperty Enemy){
        return true;
    }
}
