package dev.sablespawner.registry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.DataManager;
import dev.sablespawner.manager.blueprint.BlueprintEntry;
import dev.sablespawner.manager.blueprint.BlueprintKey;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.DatapackSource;
import dev.sablespawner.manager.datapack.property.config.DefaultConfig;
import dev.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sablespawner.player.PlayerManager;
import dev.sablespawner.player.PlayerStatus;
import dev.sablespawner.spawn.EnemyControl;
import dev.sablespawner.spawn.GlobalControl;
import dev.sablespawner.spawn.session.spawnqueue.SpawnQueue;
import dev.sablespawner.spawn.session.tracker.entry.EnemySubLevelEntry;
import dev.sablespawner.spawn.session.spawnqueue.SpawnTicket;
import dev.sablespawner.spawn.session.spawnqueue.SpawnTicketBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static dev.sablespawner.SableSpawnerConfig.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = SableSpawner.MODID)
public final class SableSpawnerCommands {
    private static final TargetResolver SELF = ctx -> ctx.getSource().getPlayerOrException();
    private static final TargetResolver ARG = ctx -> EntityArgument.getPlayer(ctx, "player");

    private SableSpawnerCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return literal("sablespawner")
                .requires(source -> source.hasPermission(2))
                .then(reload())
                .then(worldConfig())
                .then(defaultConfig())
                .then(player())
                .then(debug());
    }
        private static LiteralArgumentBuilder<CommandSourceStack> reload() {
            return literal("reload")
                    .then(literal("all").executes(ctx -> reload(ctx, "all")))
                    .then(literal("datapack").executes(ctx -> reload(ctx, "datapack")))
                    .then(literal("blueprint").executes(ctx -> reload(ctx, "blueprint")));
        }
        private static LiteralArgumentBuilder<CommandSourceStack> worldConfig() {
            return literal("worldconfig")
                    .then(literal("getLevels").executes(ctx -> worldConfig(ctx, "levels")))
                    .then(literal("getPrefix").executes(ctx -> worldConfig(ctx, "prefix")));
        }
        private static LiteralArgumentBuilder<CommandSourceStack> defaultConfig() {
            return literal("defaultconfig")
                    .then(literal("getLevels").executes(ctx -> defaultConfig(ctx, "levels")))
                    .then(literal("getPrefix").executes(ctx -> defaultConfig(ctx, "prefix")));
        }
        private static LiteralArgumentBuilder<CommandSourceStack> player() {
            return literal("player")
                    .then(protection(SELF))
                    .then(score(SELF))
                    .then(argument("player", EntityArgument.player())
                            .then(protection(ARG))
                            .then(score(ARG)));
        }
            private static LiteralArgumentBuilder<CommandSourceStack> protection(TargetResolver target) {
                return literal("protection")
                        .then(literal("getExpireTime")
                                .executes(ctx -> player(ctx, target.resolve(ctx), "protection", "getExpireTime", null)))
                        .then(literal("set")
                                .executes(ctx -> player(ctx, target.resolve(ctx), "protection", "set", null)))
                        .then(literal("setTime")
                                .then(argument("tick", LongArgumentType.longArg(0))
                                        .executes(ctx -> player(ctx, target.resolve(ctx), "protection", "setTime",
                                                LongArgumentType.getLong(ctx, "tick")))))
                        .then(literal("remove")
                                .executes(ctx -> player(ctx, target.resolve(ctx), "protection", "remove", null)));
            }
            private static LiteralArgumentBuilder<CommandSourceStack> score(TargetResolver target) {
                return literal("score")
                        .then(literal("get")
                                .executes(ctx -> player(ctx, target.resolve(ctx), "score", "get", null)))
                        .then(literal("add")
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> player(ctx, target.resolve(ctx), "score", "add",
                                                (long) IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(literal("set")
                                .then(argument("amount", IntegerArgumentType.integer(0))
                                        .executes(ctx -> player(ctx, target.resolve(ctx), "score", "set",
                                                (long) IntegerArgumentType.getInteger(ctx, "amount")))))
                        .then(literal("sub")
                                .then(argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> player(ctx, target.resolve(ctx), "score", "sub",
                                                (long) IntegerArgumentType.getInteger(ctx, "amount")))));
            }
        private static LiteralArgumentBuilder<CommandSourceStack> debug() {
            return literal("debug")
                    .then(getRegistry())
                    .then(getRunTime())
                    .then(runtime());
        }
            private static LiteralArgumentBuilder<CommandSourceStack> runtime() {
                return literal("runtime")
                        .then(spawn())
                        .then(forceFlushSpawnQueue());
            }
                private static LiteralArgumentBuilder<CommandSourceStack> spawn() {
                    return literal("spawn")
                            .then(argument("packname", StringArgumentType.string())
                                    .suggests(SableSpawnerCommands::suggestPackNames)
                                    .then(argument("name", StringArgumentType.string())
                                            .suggests(SableSpawnerCommands::suggestPropertyNames)
                                            .executes(ctx -> debugSpawn(ctx,
                                                    ctx.getArgument("packname", String.class),
                                                    ctx.getArgument("name", String.class)))));
                }
                private static LiteralArgumentBuilder<CommandSourceStack> forceFlushSpawnQueue() {
                    return literal("forceFlushSpawnQueue")
                            .executes(ctx -> debugForceFlushSpawnQueue(ctx, currentDimension(ctx)))
                            .then(argument("dimension", DimensionArgument.dimension())
                                    .executes(ctx -> debugForceFlushSpawnQueue(ctx, dimensionArg(ctx, "dimension"))));
                }
            private static LiteralArgumentBuilder<CommandSourceStack> getRegistry() {
                return literal("getRegistry")
                        .then(literal("blueprintRegistry").executes(ctx -> debugRegistry(ctx, "blueprint_registry")))
                        .then(literal("propertyBlueprintMap").executes(ctx -> debugRegistry(ctx, "property_blueprint_map")))
                        .then(literal("datapackRegistry").executes(ctx -> debugRegistry(ctx, "datapack_registry")))
                        .then(literal("propertyManager").executes(ctx -> debugRegistry(ctx, "property_manager")))
                        .then(literal("worldconfigManager").executes(ctx -> debugRegistry(ctx, "worldconfig_manager")))
                        .then(literal("all").executes(ctx -> debugRegistry(ctx, "all")));
            }
            private static LiteralArgumentBuilder<CommandSourceStack> getRunTime() {
                return literal("getRunTime")
                        .then(literal("controllers").executes(SableSpawnerCommands::debugControllers))
                        .then(literal("playerTracker").executes(SableSpawnerCommands::debugPlayerTracker))
                        .then(literal("spawnQueue")
                                .executes(ctx -> debugSpawnQueue(ctx, currentDimension(ctx)))
                                .then(argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> debugSpawnQueue(ctx, dimensionArg(ctx, "dimension")))))
                        .then(literal("enemyTracker")
                                .executes(ctx -> debugEnemyTracker(ctx, currentDimension(ctx)))
                                .then(argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> debugEnemyTracker(ctx, dimensionArg(ctx, "dimension")))))
                        .then(literal("rawEnemyTracker")
                                .executes(ctx -> debugRawEnemyTracker(ctx, currentDimension(ctx)))
                                .then(argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> debugRawEnemyTracker(ctx, dimensionArg(ctx, "dimension")))))
                        .then(literal("debrisTracker")
                                .executes(ctx -> debugDebrisTracker(ctx, currentDimension(ctx)))
                                .then(argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> debugDebrisTracker(ctx, dimensionArg(ctx, "dimension")))));
            }

    private static int reload(CommandContext<CommandSourceStack> ctx, String field) {
        long start = System.nanoTime();
        return switch (field) {
            case "all" -> {
                DataManager.reloadAll();
                yield success(ctx,
                        "sablespawner.command.reload.all",
                        DataManager.getDatapackAmount(),
                        DataManager.getBlueprintAmount(),
                        elapsedMs(start)
                );
            }
            case "datapack" -> {
                getDatapackManager().reloadDatapack();
                yield success(ctx,
                        "sablespawner.command.reload.datapack",
                        DataManager.getDatapackAmount(),
                        elapsedMs(start)
                );
            }
            case "blueprint" -> {
                getBlueprintManager().reloadBlueprint();
                yield success(ctx,
                        "sablespawner.command.reload.blueprint",
                        DataManager.getBlueprintAmount(),
                        elapsedMs(start)
                );
            }
            default -> 0;
        };
    }
    private static int worldConfig(CommandContext<CommandSourceStack> ctx, String field) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldConfig config = getDatapackManager().worldConfigQuery()
                .ofDimension(level)
                .collect();

        if (config == null) {
            return fail(ctx,
                    "sablespawner.command.worldconfig.not_found",
                    String.valueOf(level.dimension().location())
            );
        }

        return switch (field) {
            case "levels" -> success(ctx,
                    "sablespawner.command.worldconfig.levels",
                    String.valueOf(config.getWorldLevel())
            );
            case "prefix" -> success(ctx, "sablespawner.command.worldconfig.prefix",
                    config.getEnemyPrefix(),
                    config.getAllyPrefix(),
                    config.getNeutralPrefix()
            );
            default -> 0;
        };
    }
    private static int defaultConfig(CommandContext<CommandSourceStack> ctx, String field) {
        DefaultConfig config = getDatapackManager().getDEFAULT_CONFIG();

        if (config == null) {
            return fail(ctx,
                    "sablespawner.command.defaultconfig.not_found"
            );
        }

        return switch (field) {
            case "levels" -> success(ctx,
                    "sablespawner.command.defaultconfig.levels",
                    String.valueOf(config.getWorldLevel())
            );
            case "prefix" -> success(ctx, "sablespawner.command.defaultconfig.prefix",
                    config.getEnemyPrefix(),
                    config.getAllyPrefix(),
                    config.getNeutralPrefix()
            );
            default -> 0;
        };
    }
    private static int player(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String field, String operation, @Nullable Long amount) {
        PlayerStatus status = getPlayerManager().getStatus(player);
        if (status == null) {
            return fail(ctx,
                    "sablespawner.command.player.not_found",
                    player.getDisplayName()
            );
        }

        return switch (field) {
            case "protection" -> switch (operation) {
                case "getExpireTime" -> success(ctx,
                        "sablespawner.command.protection.get_expire_time",
                        player.getDisplayName(),
                        status.getProtectionExpireTime()
                );
                case "set" -> {
                    status.protect(PLAYER_PROTECTION_TIME.getAsInt());
                    yield success(ctx,
                            "sablespawner.command.protection.set",
                            player.getDisplayName()
                    );
                }
                case "setTime" -> {
                    if (amount == null) { yield fail(ctx, "sablespawner.command.invalid_amount"); }
                    status.protect(amount);
                    yield success(ctx,
                            "sablespawner.command.protection.set_time",
                            player.getDisplayName(),
                            amount
                    );
                }
                case "remove" -> {
                    status.removeProtect();
                    yield success(ctx,
                            "sablespawner.command.protection.remove",
                            player.getDisplayName()
                    );
                }
                default -> 0;
            };
            case "score" -> switch (operation) {
                case "get" -> {
                    WorldConfig config = getDatapackManager().worldConfigQuery()
                            .ofDimension(player.serverLevel())
                            .collect();
                    
                    if ( config != null ) {
                        int level = config.getScoreLevel(status.getScore());
                        yield success(ctx,
                                "sablespawner.command.score.get",
                                player.getDisplayName(),
                                status.getScore(),
                                level
                        );
                    } else {
                        yield success(ctx,
                                "sablespawner.command.score.get_no_level",
                                player.getDisplayName(),
                                status.getScore()
                        );
                    }
                }
                case "add", "set", "sub" -> {
                    if (amount == null) {
                        yield fail(ctx,
                                "sablespawner.command.score.invalid"
                        );
                    }
                    if (amount > Integer.MAX_VALUE || amount < 0) {
                        yield fail(ctx,
                                "sablespawner.command.score.out_of_range",
                                Integer.MAX_VALUE
                        );
                    }

                    int value = amount.intValue();
                    switch (operation) {
                        case "add" -> {
                            status.addScore(value);
                            yield success(ctx,
                                    "sablespawner.command.score.add",
                                    player.getDisplayName(),
                                    value,
                                    status.getScore()
                            );
                        }
                        case "sub" -> {
                            status.subScore(value);
                            yield success(ctx,
                                    "sablespawner.command.score.sub",
                                    player.getDisplayName(),
                                    value,
                                    status.getScore()
                            );
                        }
                        case "set" -> {
                            status.setScore(value);
                            yield success(ctx,
                                    "sablespawner.command.score.set",
                                    player.getDisplayName(),
                                    status.getScore()
                            );
                        }
                        default -> {
                            yield 0;
                        }
                    }
                }
                default -> 0;
            };
            default -> 0;
        };
    }

    private static int debugRegistry(CommandContext<CommandSourceStack> ctx, String register) {
        switch (register) {
            case "blueprint_registry" -> logBlueprintRegistry();
            case "property_blueprint_map" -> logPropertyBlueprintMap();
            case "datapack_registry" -> logDatapackRegistry();
            case "property_manager" -> logPropertyManager();
            case "worldconfig_manager" -> logWorldconfigManager();
            case "all" -> {
                logBlueprintRegistry();
                logPropertyBlueprintMap();
                logDatapackRegistry();
                logPropertyManager();
                logWorldconfigManager();
            }
            default -> {
                return 0;
            }
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
        private static void logBlueprintRegistry() {
            var registry = getBlueprintManager().getBLUEPRINT_REGISTRY();
            getLogger().info("蓝图注册表：{} 项 | Blueprint registry: {} entries", registry.size(), registry.size());
            for (Map.Entry<BlueprintKey, BlueprintEntry> e : registry.entrySet()) {
                getLogger().info("  {} [{}] -> {}", e.getKey().name(), shortHash(e.getKey().fileHash()), e.getValue().getEntryType());
            }
        }
        private static void logPropertyBlueprintMap() {
            var map = getBlueprintManager().getPROPERTY_BLUEPRINT_MAP();
            getLogger().info("属性-蓝图映射：{} 项 | Property-blueprint map: {} entries", map.size(), map.size());
            for (Map.Entry<PropertyKey, BlueprintKey> e : map.entrySet()) {
                getLogger().info("  {} -> {}", e.getKey(), e.getValue());
            }
        }
        private static void logDatapackRegistry() {
            var registry = getDatapackManager().getDATAPACK_REGISTRY();
            getLogger().info("数据包注册表：{} 项 | Datapack registry: {} entries", registry.size(), registry.size());
            for (DatapackSource source : registry) {
                getLogger().info("  pack={} root={} validRoot={}",
                        source.getPackMeta().packName(), source.getRoot(), source.getValidRootEntry());
            }
        }
        private static void logPropertyManager() {
            var manager = getDatapackManager().getPROPERTY_MANAGER();
            getLogger().info("属性注册表：{} 项 | Property registry: {} entries", manager.size(), manager.size());
            for (Map.Entry<PropertyKey, AbstractSchematicProperty> e : manager.entrySet()) {
                getLogger().info("  {} -> {} schematic={}",
                        e.getKey(), e.getValue().getSublevelType(), e.getValue().getSchematicName());
            }
        }
        private static void logWorldconfigManager() {
            var manager = getDatapackManager().getWORLDCONFIG_MANAGER();
            getLogger().info("维度配置注册表：{} 项 | Worldconfig registry: {} entries", manager.size(), manager.size());
            for (Map.Entry<String, DefaultConfig> e : manager.entrySet()) {
                DefaultConfig config = e.getValue();
                getLogger().info("  {} levels={} enemy={} ally={} neutral={}",
                        e.getKey(), config.getWorldLevel(), config.getEnemyPrefix(), config.getAllyPrefix(), config.getNeutralPrefix());
            }
        }
    private static int debugControllers(CommandContext<CommandSourceStack> ctx) {
        var controllers = getGlobalControl().CONTROLLERS;
        long nextScan = SCAN_INTERVAL.getAsInt() - (getGameTime() % SCAN_INTERVAL.getAsInt());

        getLogger().info("全局控制器：{} 个控制器，下次冷扫描：{} tick后 | Global controllers: {} controllers, next scan in {} ticks",
                controllers.size(), nextScan, controllers.size(), nextScan);
        for (Map.Entry<String, EnemyControl> e : controllers.entrySet()) {
            getLogger().info("\t{} : 刷怪器活跃={}", e.getKey(), e.getValue().isSpawnerActive());
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugPlayerTracker(CommandContext<CommandSourceStack> ctx) {
        var tracker = getPlayerManager().getPLAYER_TRACKER();

        getLogger().info("玩家追踪器：{} 个玩家 | Player tracker: {} players", tracker.size(), tracker.size());
        for (PlayerStatus status : tracker.values()) {
            ServerPlayer player = status.getPlayer();
            String dim = player.level().dimension().location().toString();
            EnemyControl controller = getGlobalControl().CONTROLLERS.get(dim);
            boolean fighting = controller != null && controller.isEnemyNearby(player);

            getLogger().info("\t[{}] {}：保护时间={}，正在交战={}",
                    dim, player.getScoreboardName(), status.getProtectionExpireTime(), fighting);
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugSpawnQueue(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = findController(ctx, dim);
        if (controller == null) { return 0; }

        var queue = controller.getSPAWN_QUEUE().getQueue();
        getLogger().info("刷怪队列：{} 个玩家 | Spawn queue: {} players", queue.size(), queue.size());
        for (Map.Entry<UUID, SpawnTicket> e : queue.entrySet()) {
            SpawnTicket ticket = e.getValue();
            ServerPlayer target = SableSpawner.SERVER.getPlayerList().getPlayer(e.getKey());
            PlayerStatus status = target == null ? null : getPlayerManager().getStatus(target);
            long remaining = status == null ? -1 : ticket.getScheduledSpawnTime(status) - getGameTime();

            getLogger().info("\t[{}] {}：{}x {}，延迟{}ticks，预计在{}tick后刷出",
                    dim,
                    playerName(e.getKey()),
                    ticket.amount(),
                    ticket.property().getSchematicName(),
                    ticket.spawnDelay(),
                    remaining
            );
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugEnemyTracker(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = findController(ctx, dim);
        if (controller == null) { return 0; }

        var entries = controller.getENEMY_TRACKER().getEntries();
        ObjectList<EnemySubLevelEntry> enemies = new ObjectArrayList<>();
        for (EnemySubLevelEntry entry : entries.values()) {
            if ( !entry.isDebris() ) { enemies.add(entry); }
        }

        getLogger().info("敌方追踪器：{} 中有 {} 个敌人 | Enemy tracker: {} enemies in {}",
                dim, enemies.size(), dim, enemies.size());
        long gameTime = getGameTime();
        for (EnemySubLevelEntry entry : enemies) {
            EnemyProperty property = Objects.requireNonNull(entry.getProperty());
            long ftlRemain = entry.getFTLChargeStartTime() == -1
                    ? -1
                    : entry.getFTLChargeStartTime() + property.getFTLChargeDuration() - gameTime;

            getLogger().info("\t{}-{}：生成时间={}，总质量={}，剩余质量={}（{}%），剩余撤离时间={}，超光速充能剩余时间={}",
                    shortUuid(entry.getUuid()), shipName(entry),
                    entry.getSpawnedGameTick(),
                    formatDouble(entry.getTotalMass()), formatDouble(entry.getRemainingMass()), formatDouble(entry.getMassPercentage()),
                    property.getLifeTime() - (gameTime - entry.getSpawnedGameTick()),
                    ftlRemain);
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugDebrisTracker(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = findController(ctx, dim);
        if (controller == null) { return 0; }

        var entries = controller.getENEMY_TRACKER().getEntries();
        ObjectList<EnemySubLevelEntry> debris = new ObjectArrayList<>();
        for (EnemySubLevelEntry entry : entries.values()) {
            if ( entry.isDebris() ) { debris.add(entry); }
        }

        getLogger().info("碎片追踪器：{} 中有 {} 个碎片 | Debris tracker: {} debris in {}",
                dim, debris.size(), dim, debris.size());
        for (EnemySubLevelEntry entry : debris) {
            UUID parentUuid = entry.getSublevel().getSplitFromSubLevel();
            EnemySubLevelEntry parent = parentUuid == null ? null : entries.get(parentUuid);
            String source = parentUuid == null
                    ? "未知"
                    : shortUuid(parentUuid) + "-" + (parent == null ? "未知" : shipName(parent));

            getLogger().info("\t{}：碎片源={}，生成时间={}，质量={}",
                    shortUuid(entry.getUuid()), source,
                    entry.getSpawnedGameTick(), formatDouble(entry.getRemainingMass()));
        }
        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugRawEnemyTracker(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = findController(ctx, dim);
        if (controller == null) { return 0; }

        Object2ObjectOpenHashMap<UUID, EnemySubLevelEntry> entries = controller.getENEMY_TRACKER().getEntries();
        getLogger().info("原始追踪器：{} 中有 {} 个条目 | Raw tracker: {} entries in {}",
                dim, entries.size(), dim, entries.size());

        long gameTime = getGameTime();
        for (EnemySubLevelEntry entry : entries.values()) {
            var subLevel = entry.getSublevel();
            UUID splitFrom = subLevel.getSplitFromSubLevel();
            EnemyProperty property = entry.getProperty();

            getLogger().info("\t{}-{}：isDebris={}，property={}，longLived={}，生成时间={}，存在时间={}，质量={}（{}%），splitFrom={}，剩余={}",
                    shortUuid(entry.getUuid()),
                    shipName(entry),
                    entry.isDebris(),
                    property == null ? "null" : property.getSchematicName(),
                    entry.isLongLivedDebris(),
                    entry.getSpawnedGameTick(),
                    gameTime - entry.getSpawnedGameTick(),
                    formatDouble(entry.getRemainingMass()),
                    formatDouble(entry.getMassPercentage()),
                    splitFrom == null ? "null" : shortUuid(splitFrom),
                    remainText(entry, property, gameTime));
        }

        if (controller.getCONTAINER() != null) {
            getLogger().info("容器子空间视图： | Container sublevel view:");
            for (var subLevel : controller.getCONTAINER().getAllSubLevels()) {
                UUID splitFrom = subLevel.getSplitFromSubLevel();
                getLogger().info("\t[容器] {}：名称={}，splitFrom={}，已追踪={}",
                        shortUuid(subLevel.getUniqueId()),
                        subLevel.getName(),
                        splitFrom == null ? "null" : shortUuid(splitFrom),
                        entries.containsKey(subLevel.getUniqueId()));
            }
        }

        return success(ctx, "sablespawner.command.debug.done");
    }
    private static int debugSpawn(CommandContext<CommandSourceStack> ctx, String packName, String propertyName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        EnemyControl controller = findController(ctx, currentDimension(ctx));
        if ( controller == null ) { return 0; }

        PropertyKey key = PropertyKey.of(propertyName, packName, AbstractSchematicProperty.SublevelType.enemy);
        SpawnTicket ticket = SpawnTicketBuilder.of(key, player.getUUID());

        String display = packName + ":" + propertyName;
        if ( ticket == null ) {
            return fail(ctx, "sablespawner.command.debug.property_not_found", display);
        }
        if ( controller.spawnEnemy(ticket) ) {
            return success(ctx, "sablespawner.command.debug.spawned", display);
        }
        return fail(ctx, "sablespawner.command.debug.spawn_failed", display);
    }
    private static int debugForceFlushSpawnQueue(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = findController(ctx, dim);
        if ( controller == null ) { return 0; }

        SpawnQueue queue = controller.getSPAWN_QUEUE();
        queue.getQueue().clear();
        queue.updateQueue();
        return success(ctx, "sablespawner.command.debug.force_flushed");
    }

    private static int success(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendSuccess(() -> Component.translatable(key, args), false);
        return SINGLE_SUCCESS;
    }
    private static int fail(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        ctx.getSource().sendFailure(Component.translatable(key, args));
        return 0;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
    private static String shortHash(String hash) {
        if (hash == null) { return "null"; }
        return hash.length() > 8 ? hash.substring(0, 8) : hash;
    }
    private static String shortUuid(UUID uuid) {
        return uuid.toString().substring(0, 8);
    }
    private static String shipName(EnemySubLevelEntry entry) {
        String name = entry.getSublevel().getName();
        return name == null ? "未命名" : name;
    }
    private static String formatDouble(double value) {
        return String.format("%.2f", value);
    }
    private static String playerName(UUID uuid) {
        ServerPlayer player = SableSpawner.SERVER.getPlayerList().getPlayer(uuid);
        return player != null ? player.getScoreboardName() : shortUuid(uuid);
    }
    @Nullable private static EnemyControl findController(CommandContext<CommandSourceStack> ctx, String dim) {
        EnemyControl controller = getGlobalControl().CONTROLLERS.get(dim);
        if (controller == null) {
            getLogger().warn("未找到维度 {} 的控制器 | No controller found for dimension {}", dim, dim);
            fail(ctx, "sablespawner.command.debug.no_controller", dim);
        }
        return controller;
    }
    private static long getGameTime() {
        return SableSpawner.SERVER.overworld().getGameTime();
    }
    private static String currentDimension(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getLevel().dimension().location().toString();
    }
    private static String dimensionArg(CommandContext<CommandSourceStack> ctx, String name) {
        return ctx.getArgument(name, ResourceLocation.class).toString();
    }
    private static String remainText(EnemySubLevelEntry entry, @Nullable EnemyProperty property, long gameTime) {
        long exist = gameTime - entry.getSpawnedGameTick();
        if (property == null) {
            int despawn = entry.isLongLivedDebris() ? LONG_DEBRIS_DESPAWN_TIME.getAsInt() : DEBRIS_DESPAWN_TIME.getAsInt();
            return (despawn - exist) + "（碎片）";
        }
        return (property.getLifeTime() - exist) + "（存在）";
    }
    private static CompletableFuture<Suggestions> suggestPackNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Set<String> packs = new HashSet<>();
        for ( PropertyKey key : getDatapackManager().propertyQuery().isEnemy().collectKeys() ) {
            packs.add( key.packName() );
        }
        return SharedSuggestionProvider.suggest(packs, builder);
    }
    private static CompletableFuture<Suggestions> suggestPropertyNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String packName = ctx.getArgument("packname", String.class);

        List<String> names = new ArrayList<>();
        for ( PropertyKey key : getDatapackManager().propertyQuery().isEnemy().ofPackName(packName).collectKeys() ) {
            names.add( key.propertyName() );
        }
        return SharedSuggestionProvider.suggest(names, builder);
    }

    private static DatapackManager getDatapackManager() {
        return SableSpawner.DATAPACK_MANAGER;
    }
    private static BlueprintManager getBlueprintManager() {
        return SableSpawner.BLUEPRINT_MANAGER;
    }
    private static PlayerManager getPlayerManager() {
        return SableSpawner.PLAYER_MANAGER;
    }
    private static GlobalControl getGlobalControl() {
        return SableSpawner.GLOBAL_CONTROLLER;
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }

    @FunctionalInterface
    private interface TargetResolver {
        ServerPlayer resolve(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException;
    }
}
