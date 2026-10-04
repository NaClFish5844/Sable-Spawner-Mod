package dev.sablespawner.blockentity.tracker.query;

import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class FleetBaseCoreBlockEntityQuery extends BlockEntityQuery<FleetBaseCoreBlockEntity> {
    public FleetBaseCoreBlockEntityQuery(Object2ObjectOpenHashMap<UUID, FleetBaseCoreBlockEntity> source) {
        super(source);
    }

    public FleetBaseCoreBlockEntityQuery ofLevel(Level level) {
        predicate = predicate.and(core -> core.getLevel() == level);
        return this;
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
        predicate = predicate.and(FleetBaseCoreBlockEntity::isBoundToSubLevel);
        return this;
    }


    public ObjectList<FleetBaseCoreBlockEntity> covering(ServerLevel level, Vec3 pos) {
        ObjectList<FleetBaseCoreBlockEntity> result = new ObjectArrayList<>();

        for ( FleetBaseCoreBlockEntity core : source.values() ) {
            if ( !predicate.test(core) || core.getLevel() != level ) { continue; }
            if ( core.covers(pos) ) { result.add(core); }
        }

        return result;
    }
    public boolean isInProtectedArea(ServerLevel level, Vec3 pos) {
        for ( FleetBaseCoreBlockEntity core : source.values() ) {
            if ( !predicate.test(core) || core.getLevel() != level ) { continue; }
            if ( core.covers(pos) ) { return true; }
        }

        return false;
    }
    @Nullable public FleetBaseCoreBlockEntity nearestCore(ServerLevel level, Vec3 pos) {
        FleetBaseCoreBlockEntity nearest = null;
        double nearestSqr = Double.MAX_VALUE;

        for ( FleetBaseCoreBlockEntity core : source.values() ) {
            if ( !predicate.test(core) || core.getLevel() != level ) { continue; }

            double distanceSqr = core.getWorldPos().distanceToSqr(pos);
            if ( distanceSqr < nearestSqr ) {
                nearestSqr = distanceSqr;
                nearest = core;
            }
        }

        return nearest;
    }
    public ObjectList<FleetBaseCoreBlockEntity> nearCore(ServerLevel level, Vec3 pos, double distance) {
        double distanceSqr = distance * distance;
        ObjectList<FleetBaseCoreBlockEntity> result = new ObjectArrayList<>();

        for ( FleetBaseCoreBlockEntity core : source.values() ) {
            if ( !predicate.test(core) || core.getLevel() != level ) { continue; }
            if ( core.getWorldPos().distanceToSqr(pos) <= distanceSqr ) { result.add(core); }
        }

        return result;
    }

}
