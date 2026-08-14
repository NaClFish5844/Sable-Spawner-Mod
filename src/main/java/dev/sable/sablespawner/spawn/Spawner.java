package dev.sable.sablespawner.spawn;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.rew1nd.sableschematicapi.blueprint.SableBlueprintPlacer;
import dev.rew1nd.sableschematicapi.survival.BlueprintPlacementPlan;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.datapack.property.AllyProperty;
import dev.sable.sablespawner.datapack.property.PrefabProperty;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class Spawner {
    // 以后想办法重构成链式的？
    DatapackManager DATAPACK_MANAGER = SableSpawner.DATAPACK_MANAGER;
    private final ServerSubLevelContainer CONTAINER;
    private final Random RANDOM = new Random();

    public Spawner(ServerLevel level){
        this.CONTAINER = SubLevelContainer.getContainer(level);
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

    public ServerSubLevel spawnSublevelAsEnemy(AbstractSchematicProperty property, ServerLevel level, BlockPos origin) {
        if ( property instanceof EnemyProperty ) {
            return spawnAndName(property, level, origin);
        }
        return null;
    }
    public ServerSubLevel spawnSublevelAsEnemy(AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        if ( property instanceof EnemyProperty ) {
            return spawnAndName(property, level, plan);
        }
        return null;
    }

    public ServerSubLevel spawnSublevelAsAlly(AbstractSchematicProperty property, ServerLevel level, BlockPos origin) {
        if ( property instanceof AllyProperty ) {
            return spawnAndName(property, level, origin);
        }
        return null;
    }
    public ServerSubLevel spawnSublevelAsAlly(AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        if ( property instanceof AllyProperty ) {
            return spawnAndName(property, level, plan);
        }
        return null;
    }

    public ServerSubLevel spawnSublevelAsPrefab(AbstractSchematicProperty property, ServerLevel level, BlockPos origin) {
        if ( property instanceof PrefabProperty ) {
            return spawnAndName(property, level, origin);
        }
        return null;
    }
    public ServerSubLevel spawnSublevelAsPrefab(AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        if ( property instanceof PrefabProperty ) {
            return spawnAndName(property, level, plan);
        }
        return null;
    }

    private @Nullable ServerSubLevel spawnAndName(AbstractSchematicProperty property, ServerLevel level, BlockPos origin) {
        if (DATAPACK_MANAGER == null) { return null; }
        if (CONTAINER == null ) { return null; }
        return applyName(spawnSublevel(property, level, origin), property);
    }
    private @Nullable ServerSubLevel spawnAndName(AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        if (DATAPACK_MANAGER == null) { return null; }
        if (CONTAINER == null ) { return null; }
        return applyName(spawnSublevel(property, level, plan), property);
    }
    private @Nullable ServerSubLevel applyName(@Nullable Map<UUID, UUID> result, AbstractSchematicProperty property) {
        if (result == null || result.isEmpty()) { return null; }

        UUID spawnedUUID = result.values().iterator().next();
        ServerSubLevel spawnedSublevel = (ServerSubLevel) CONTAINER.getSubLevel(spawnedUUID);

        if ( spawnedSublevel == null ) { return null; }

        spawnedSublevel.setName(nameBuilder(property));

        return spawnedSublevel;
    }

    public @Nullable Map<UUID, UUID> spawnSublevel(AbstractSchematicProperty property, ServerLevel level, BlockPos origin) {
        Vec3 og = Vec3.atLowerCornerOf(origin);
        SableBlueprint blueprint = getSableBlueprint(property);
        if (blueprint != null) {
            return spawnSublevel(property, level, BlueprintPlacementPlan.legacy(blueprint, og));
        }
        return null;
    }
    public @Nullable Map<UUID, UUID> spawnSublevel(AbstractSchematicProperty property, ServerLevel level, BlueprintPlacementPlan plan) {
        SableBlueprint blueprint = getSableBlueprint(property);
        if ( BoundBoxVacantDetection(level, plan) && blueprint != null ) {
            return SableBlueprintPlacer.place(level, blueprint, plan).subLevelUuidMap();
        } else {
            SableSpawner.LOGGER.error("Failed to spawn sublevel");
        }
        return null;
    }

    public static @Nullable SableBlueprint getSableBlueprint(AbstractSchematicProperty property){
        if (property == null) { return null; }

        try {
            switch (property.getSchematicSource() == null
                    ? AbstractSchematicProperty.SchematicSource.datapack
                    : property.getSchematicSource())
            {
                case folder -> {
                    String path = property.getSchematicPath();
                    if (path == null) { return null; }
                    CompoundTag tag = NbtIo.readCompressed(Path.of(path), NbtAccounter.unlimitedHeap());
                    return SableBlueprint.load(tag);
                }
                case datapack -> {
                    ResourceLocation loc = property.getSchematicResourceLocation();
                    if (loc == null) { return null; }
                    Optional<Resource> resource = getResourceManager().getResource(loc);
                    if (resource.isEmpty()) {
                        SableSpawner.LOGGER.error("Failed to find blueprint in datapack: {}", loc);
                        return null;
                    }
                    try (InputStream is = resource.get().open()) {
                        CompoundTag tag = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
                        return SableBlueprint.load(tag);
                    }
                }
            }
        } catch (IOException e) {
            SableSpawner.LOGGER.error("Failed to load blueprint: {}", property.getSchematicPath(), e);
        }
        return null;
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
        String prefix = null;
        if ( prop.getSublevelType() != null ) {
            prefix = switch (prop.getSublevelType()) {
                case enemy -> DATAPACK_MANAGER.getWorldConfig().getEnemyPrefix();
                case ally -> DATAPACK_MANAGER.getWorldConfig().getAllyPrefix();
                case prefab -> DATAPACK_MANAGER.getWorldConfig().getNeutralPrefix();
                default -> DATAPACK_MANAGER.getWorldConfig().getNeutralPrefix();
            };
        }
        return prefix + randomName();
    }
    private static ResourceManager getResourceManager() { return SableSpawner.RESOURCE_MANAGER; }

}
