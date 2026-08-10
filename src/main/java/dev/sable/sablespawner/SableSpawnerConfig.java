package dev.sable.sablespawner;

import net.neoforged.neoforge.common.ModConfigSpec;


public class SableSpawnerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ENEMY_DETECTION_DISTANCE = BUILDER
            .comment("敌人的探测距离（格），距离超过此数值后将判定为脱离敌人\nThe detection distance of enemy, disengages when further than this distance")
            .defineInRange("enemy_detection_distance",256,8,1024);

    public static final ModConfigSpec.IntValue DEBRIS_DESPAWN_TIME = BUILDER
            .comment("敌人碎片的消失时间（秒）\nThe despawn time of enemy's debris")
            .defineInRange("debris_despawn_time",120,10,86400);

    public static final ModConfigSpec.IntValue SCAN_INTERVAL = BUILDER
            .comment("扫描间隔（秒），每次敌人状态扫描（包括敌人的碎片）之间的间隔，请勿设置过低！")
            .defineInRange("scan_interval",5,1,60);

    static final ModConfigSpec SPEC = BUILDER.build();
}
