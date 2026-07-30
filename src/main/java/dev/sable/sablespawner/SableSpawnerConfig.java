package dev.sable.sablespawner;

import net.neoforged.neoforge.common.ModConfigSpec;


public class SableSpawnerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SPAWN_INTERVAL = BUILDER
            .comment("每波敌人刷新的时间间隔（秒）\nThe spawn interval between enemy engaging (second)")
            .defineInRange("spawn_interval", 60, 10, 3600);

    public static final ModConfigSpec.IntValue CLEANUP_INTERVAL_LIGHT = BUILDER
            .comment("轻型敌人的清理间隔（秒），敌人将无条件在刷新之后的此时间后被清除\nThe cleanup interval of light enemies (second), enemy will be cleaned up after this time since spawned")
            .defineInRange("cleanup_interval_light", 600,10,86400);
    public static final ModConfigSpec.IntValue CLEANUP_INTERVAL_MIDDLE = BUILDER
            .comment("中型敌人的清理间隔（秒），敌人将无条件在刷新之后的此时间后被清除\nThe cleanup interval of middle enemies (second), enemy will be cleaned up after this time since spawned")
            .defineInRange("cleanup_interval_middle", 1200,10,86400);
    public static final ModConfigSpec.IntValue CLEANUP_INTERVAL_HEAVY = BUILDER
            .comment("重型敌人的清理间隔（秒），敌人将无条件在刷新之后的此时间后被清除\nThe cleanup interval of heavy enemies (second), enemy will be cleaned up after this time since spawned")
            .defineInRange("cleanup_interval_heavy", 1800,10,86400);

    public static final ModConfigSpec.DoubleValue DESTROYED_THRESHOLD = BUILDER
            .comment(("敌人的击沉阈值（百分比），敌人的总质量百分比低于此值时判定为击沉\nThe destroyed threshold (percentage), the enemy is destroyed when total mass percentage is below this"))
            .defineInRange("destroyed_threshold",50d,0d,100d);

    public static final ModConfigSpec.IntValue MIN_SPAWN_DISTANCE = BUILDER
            .comment("敌人的最小刷新距离（格）\nThe minimum spawn distance of enemy")
            .defineInRange("min_spawn_distance",64,8,1024);
    public static final ModConfigSpec.IntValue MAX_SPAWN_DISTANCE = BUILDER
            .comment("敌人的最大刷新距离（格）\nThe maximum spawn distance of enemy")
            .defineInRange("max_spawn_distance",96,8,1024);

    public static final ModConfigSpec.IntValue ENEMY_DETECTION_DISTANCE = BUILDER
            .comment("敌人的探测距离（格），距离超过此数值后将判定为脱离敌人\nThe detection distance of enemy, disengages when further than this distance")
            .defineInRange("enemy_detection_distance",256,8,1024);

    public static final ModConfigSpec.IntValue DEBRIS_DESPAWN_TIME = BUILDER
            .comment("敌人碎片的消失时间（秒）\nThe despawn time of enemy's debris")
            .defineInRange("debris_despawn_time",300,10,86400);

    static final ModConfigSpec SPEC = BUILDER.build();
}
