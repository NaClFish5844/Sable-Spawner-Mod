package dev.sable.sablespawner;

import net.neoforged.neoforge.common.ModConfigSpec;


public class SableSpawnerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ENEMY_DETECTION_DISTANCE = BUILDER
            .comment("敌人的探测距离（格），距离超过此数值后将判定为脱离敌人")
            .defineInRange("enemy_detection_distance",256,8,1024);

    public static final ModConfigSpec.IntValue DEBRIS_DESPAWN_TIME = BUILDER
            .comment("敌人碎片的消失时间（tick）")
            .defineInRange("debris_despawn_time",2400,20,72000);

    public static final ModConfigSpec.IntValue SCAN_INTERVAL = BUILDER
            .comment("扫描间隔（tick），每次敌人状态扫描（包括敌人的碎片）之间的间隔，请勿设置过低！")
            .defineInRange("scan_interval",100,10,172800);

    public static final ModConfigSpec.IntValue PLAYER_PROTECTION_TIME = BUILDER
            .comment("玩家保护时间（tick），击败敌人后玩家的保护时间")
            .defineInRange("player_protection_time",1200,10,172800);

    public static final ModConfigSpec.IntValue PLAYER_SPAWN_PROTECTION_TIME = BUILDER
            .comment("玩家的出生保护时间（tick），登入服务器或切换维度后的保护时间")
            .defineInRange("player_spawn_protection_time",2400,10,172800);

    static final ModConfigSpec SPEC = BUILDER.build();
}
