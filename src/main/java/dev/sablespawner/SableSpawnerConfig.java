package dev.sablespawner;

import net.neoforged.neoforge.common.ModConfigSpec;


public class SableSpawnerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue ENEMY_DETECTION_DISTANCE = BUILDER
            .comment("敌人的探测距离（格），距离超过此数值后将判定为脱离敌人")
            .defineInRange("enemy_detection_distance",256,8,1048576);

    public static final ModConfigSpec.IntValue DEBRIS_DESPAWN_TIME = BUILDER
            .comment("短时碎片的消失时间（tick），此功能用于清理*断裂碎片*，设置为-1以禁用")
            .defineInRange("debris_despawn_time",2400,-1,72000);

    public static final ModConfigSpec.IntValue LONG_DEBRIS_DESPAWN_TIME = BUILDER
            .comment("长时碎片的消失时间（tick），此功能用于清理*击沉残骸*，设置为-1以禁用")
            .defineInRange("long_debris_despawn_time",24000,-1,1728000);

    public static final ModConfigSpec.IntValue SCAN_INTERVAL = BUILDER
            .comment("扫描间隔（tick），每次敌人状态扫描之间的间隔，请勿设置过低！")
            .defineInRange("scan_interval",100,10,172800);

    public static final ModConfigSpec.IntValue PLAYER_PROTECTION_TIME = BUILDER
            .comment("玩家保护时间（tick），击败敌人后玩家的保护时间")
            .defineInRange("player_protection_time",1200,10,172800);

    public static final ModConfigSpec.IntValue PLAYER_SPAWN_PROTECTION_TIME = BUILDER
            .comment("玩家的出生保护时间（tick），登入服务器或切换维度后的保护时间")
            .defineInRange("player_spawn_protection_time",2400,10,172800);

    public static final ModConfigSpec.IntValue BLUEPRINT_CACHE_MAX_BLOCKS = BUILDER
            .comment("蓝图缓存的最大方块数，方块数量超出此值的大型蓝图将不会进入缓存")
            .defineInRange("blueprint_cache_max_blocks",100000,10,2147483647);



    static final ModConfigSpec SPEC = BUILDER.build();
}
