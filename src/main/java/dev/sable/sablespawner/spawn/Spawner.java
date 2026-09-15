package dev.sable.sablespawner.spawn;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintPlacer;
import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.blueprint.BlueprintManager;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.manager.datapack.property.sublevel.*;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class Spawner {

    private final ServerSubLevelContainer Container;
    private final ServerLevel level;
    private final Random RANDOM = new Random();

    public Spawner(ServerLevel level){
        this.Container = SubLevelContainer.getContainer(level);
        this.level = level;
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

    public @Nullable ServerSubLevel spawnSublevelAs(PropertyKey propertyKey, ServerLevel level, BlueprintPlacementPlan plan) {
        AbstractSchematicProperty property = getDatapackManager().propertyQuery().get(propertyKey);
        if ( property == null ) { return null; }

        return switch ( propertyKey.type() ) {
            case enemy, ally, prefab -> spawnAndName(propertyKey, property, level, plan);
            default -> null;
        };
    }

    private @Nullable ServerSubLevel spawnAndName(PropertyKey propertyKey, AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        if ( Container == null ) { return null; }
        return applyName( spawnSublevel(propertyKey, level, plan), property );
    }
    private @Nullable ServerSubLevel applyName(@Nullable Map<UUID, UUID> result, AbstractSchematicProperty property) {
        if (result == null || result.isEmpty()) { return null; }

        UUID spawnedUUID = result.values().iterator().next();
        ServerSubLevel spawnedSublevel = (ServerSubLevel) Container.getSubLevel(spawnedUUID);

        if ( spawnedSublevel == null ) { return null; }

        spawnedSublevel.setName(nameBuilder(property));

        return spawnedSublevel;
    }

    public @Nullable Map<UUID, UUID> spawnSublevel(PropertyKey propertyKey, ServerLevel level, BlueprintPlacementPlan plan) {
        Pair<Class<?>, Object> blueprintObject = getBlueprintManager().query().getAsObject(propertyKey);

        if ( blueprintObject == null || !(blueprintObject.right() instanceof SableBlueprint blueprint) ) {
            SableSpawner.LOGGER.error("Failed to resolve blueprint: {}", propertyKey);
            return null;
        }

        if ( !BoundBoxVacantDetection(level, plan) ) { return null; }

        return SableBlueprintPlacer.place(level, blueprint, plan).subLevelUuidMap();
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
        if ( getWorldConfig(this.level) == null || prop.getSublevelType() == null ) { return randomName(); }

        String prefix = switch (prop.getSublevelType()) {
            case enemy -> getWorldConfig(this.level).getEnemyPrefix();
            case ally -> getWorldConfig(this.level).getAllyPrefix();
            default -> getWorldConfig(this.level).getNeutralPrefix();
        };
        return prefix + randomName();
    }

    private static DatapackManager getDatapackManager() {
        return SableSpawner.DATAPACK_MANAGER;
    }
    private static BlueprintManager getBlueprintManager() {
        return SableSpawner.BLUEPRINT_MANAGER;
    }
    private static WorldConfig getWorldConfig(ServerLevel level) {
        return getDatapackManager().worldConfigQuery().ofDimension(level).collect();
    }
}
