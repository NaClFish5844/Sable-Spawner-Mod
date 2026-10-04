package dev.sablespawner.blockentity.tracker.query;

import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class FleetBaseCoreBlockEntityQuery extends BlockEntityQuery<FleetBaseCoreBlockEntity> {
    public FleetBaseCoreBlockEntityQuery(Object2ObjectOpenHashMap<UUID, FleetBaseCoreBlockEntity> source) {
        super(source);
    }

    public FleetBaseCoreBlockEntityQuery ofOwner(UUID owner) {
        predicate = predicate.and(core -> owner.equals(core.getOwner()));
        return this;
    }
    public FleetBaseCoreBlockEntityQuery ofEnabled() {
        predicate = predicate.and(FleetBaseCoreBlockEntity::isEnabled);
        return this;
    }
    public FleetBaseCoreBlockEntityQuery ofDisabled() {
        predicate = predicate.and(core -> !core.isEnabled());
        return this;
    }
    public FleetBaseCoreBlockEntityQuery ofForceLoaded() {
        predicate = predicate.and(FleetBaseCoreBlockEntity::isForceLoaded);
        return this;
    }
    public FleetBaseCoreBlockEntityQuery ofBoundToSubLevel() {
        predicate = predicate.and(core ->
                core.getLevel() instanceof ServerLevel serverLevel
                        && core.getBoundSubLevel(serverLevel) != null);
        return this;
    }

    public Object2ObjectOpenHashMap<UUID, FleetBaseCoreBlockEntity> coveredBy(Vec3 position) {
        Object2ObjectOpenHashMap<UUID, FleetBaseCoreBlockEntity> result = new Object2ObjectOpenHashMap<>();

        for ( Map.Entry<UUID, FleetBaseCoreBlockEntity> entry : source.entrySet() ) {
            if ( predicate.test(entry.getValue()) ) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

}
