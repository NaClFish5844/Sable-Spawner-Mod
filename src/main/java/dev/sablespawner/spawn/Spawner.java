package dev.sablespawner.spawn;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintPlacer;
import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sablespawner.manager.datapack.property.sublevel.*;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.sablespawner.spawn.session.spawnqueue.SpawnTicket;
import dev.sablespawner.util.SpawnPatternUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.slf4j.Logger;

import java.util.*;

import static dev.sablespawner.util.AccessUtil.*;

public class Spawner {

    private final ServerSubLevelContainer CONTAINER;
    private final ServerLevel LEVEL;
    private final Random RANDOM = new Random();

    public Spawner(ServerLevel level, ServerSubLevelContainer container){
        this.LEVEL = level;
        this.CONTAINER = container;
    }

    public ObjectList<ServerSubLevel> spawnEnemyOfTicket(SpawnTicket ticket) {
        ObjectList<ServerSubLevel> spawned = new ObjectArrayList<>();
        ServerPlayer target = (ServerPlayer) LEVEL.getPlayerByUUID(ticket.targetPlayer());

        if ( target == null ) { return spawned; }

        if ( !getModList().isLoaded("sable_schematic_api") ) { return spawned; }

        Vector3d targetPos = new Vector3d(
                target.position().x,
                target.position().y,
                target.position().z
        );

        ObjectList<BlueprintPlacementPlan> placementPlans = SpawnPatternUtil.newSableBlueprintPlacementPlan(ticket, targetPos);

        if ( placementPlans.isEmpty() ) { return spawned; }

        for ( BlueprintPlacementPlan plan : placementPlans ) {
            if ( !BoundBoxVacantDetection(LEVEL, plan) ) { return spawned; }
        }

        for ( BlueprintPlacementPlan plan : placementPlans ) {
            ServerSubLevel subLevel = spawnSublevelWithName( ticket.propertyKey(), LEVEL, plan );
            if ( subLevel == null ) {
                for ( ServerSubLevel s : spawned ) { s.markRemoved(); }
                spawned.clear();
                return spawned;
            }
            spawned.add(subLevel);
        }
        return spawned;
    }

    public @Nullable ServerSubLevel spawnSublevelWithName(PropertyKey propertyKey, ServerLevel level, BlueprintPlacementPlan plan) {
        AbstractSchematicProperty property = getDatapackManager().propertyQuery().get(propertyKey);
        if ( property == null || CONTAINER == null ) {return null; }
        Map<UUID, UUID> result = spawnSublevel(propertyKey, level, plan);

        return applyName(result, property);
    }
    public @Nullable Map<UUID, UUID> spawnSublevel(PropertyKey propertyKey, ServerLevel level, BlueprintPlacementPlan plan) {
        Pair<Class<?>, Object> blueprintObject = getBlueprintManager().query().getAsObject(propertyKey);

        if ( blueprintObject == null || !(blueprintObject.right() instanceof SableBlueprint blueprint) ) {
            getLogger().error("蓝图解析失败：{} | Failed to resolve blueprint: {}", propertyKey, propertyKey);
            return null;
        }

        if ( !BoundBoxVacantDetection(level, plan) ) { return null; }

        return SableBlueprintPlacer.place(level, blueprint, plan).subLevelUuidMap();
    }
    private @Nullable ServerSubLevel applyName(@Nullable Map<UUID, UUID> result, AbstractSchematicProperty property) {
        if (result == null || result.isEmpty()) { return null; }

        UUID spawnedUUID = result.values().iterator().next();
        ServerSubLevel spawnedSublevel = (ServerSubLevel) CONTAINER.getSubLevel(spawnedUUID);

        if ( spawnedSublevel == null ) { return null; }

        spawnedSublevel.setName(nameBuilder(property));

        return spawnedSublevel;
    }

    public boolean BoundBoxVacantDetection(ServerLevel level, BlueprintPlacementPlan plan) {
        BoundingBox3d targetBoundingBox = plan.bounds();
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        List<ServerSubLevel> allSubLevels = new ArrayList<>();

        if (container != null) { allSubLevels = container.getAllSubLevels(); }

        for (ServerSubLevel sublevel : allSubLevels){
            BoundingBox3d result = new BoundingBox3d();
            targetBoundingBox.intersect( sublevel.boundingBox(), result );
            boolean occupied
                    = result.minX() <= result.maxX()
                    && result.minY() <= result.maxY()
                    && result.minZ() <= result.maxZ();
            if (occupied) { return false; }
        }

        return true;
    }
    private String randomName() {
        StringBuilder builder = new StringBuilder(6);
        for (int i = 0; i < 3; i++) {
            builder.append((char) ('A' + RANDOM.nextInt(26)));
        }
        for (int i = 0; i < 3; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
    private String nameBuilder(AbstractSchematicProperty prop) {
        WorldConfig config = getWorldConfig(this.LEVEL);
        if ( prop.getSublevelType() == null ) { return randomName(); }

        String prefix = switch (prop.getSublevelType()) {
            case enemy -> config.getEnemyPrefix();
            case ally -> config.getAllyPrefix();
            default -> config.getNeutralPrefix();
        };
        return prefix + randomName();
    }


    private static WorldConfig getWorldConfig(ServerLevel level) {
        WorldConfig config = getDatapackManager()
                .worldConfigQuery()
                .ofDimension(level)
                .collect();

        if ( config == null ) {
            config = WorldConfig.of( getDatapackManager().getDEFAULT_CONFIG(), null, WorldConfig.Pattern.invalid );
        }

        return config;
    }
}
