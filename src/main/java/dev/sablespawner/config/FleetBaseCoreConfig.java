package dev.sablespawner.config;

import net.neoforged.neoforge.common.ModConfigSpec;


public class FleetBaseCoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue DEFAULT_RADIUS = BUILDER
            .comment("舰队基地核心的默认半径（格）")
            .defineInRange("default_radius",128,1,2147483647);

    public static final ModConfigSpec.IntValue MIN_RADIUS = BUILDER
            .comment("半径最小值（格）")
            .defineInRange("min_radius",16,1,2147483647);

    public static final ModConfigSpec.IntValue MAX_RADIUS = BUILDER
            .comment("半径最大值（格）")
            .defineInRange("max_radius",1024,1,2147483647);

    public static final ModConfigSpec.IntValue RADIUS_STEP_FINE = BUILDER
            .comment("按下 Ctrl 时的半径步进值（格）")
            .defineInRange("radius_step_fine",8,1,2147483647);

    public static final ModConfigSpec.IntValue RADIUS_STEP = BUILDER
            .comment("默认的半径步进值（格）")
            .defineInRange("radius_step",16,1,2147483647);

    public static final ModConfigSpec.IntValue RADIUS_STEP_COARSE = BUILDER
            .comment("按下 Shift 时的半径步进值（格）")
            .defineInRange("radius_step_coarse",64,1,2147483647);


    public static final ModConfigSpec SPEC = BUILDER.build();
}
