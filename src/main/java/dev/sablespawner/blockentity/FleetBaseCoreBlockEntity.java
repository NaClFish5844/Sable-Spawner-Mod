package dev.sablespawner.blockentity;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.config.FleetBaseCoreConfig;
import dev.sablespawner.player.PlayerStatus;
import dev.sablespawner.registry.SableSpawnerBlockEntities;
import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

import static dev.sablespawner.util.AccessUtil.*;

public class FleetBaseCoreBlockEntity extends BlockEntity implements ISyncPersistRPCBlockEntity {

    @Getter private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Getter @Persisted @DescSynced private UUID coreId = UUID.randomUUID();
    @Getter @Persisted @DescSynced private UUID owner = Util.NIL_UUID;
    @Getter @Persisted @DescSynced private String ownerName = "";
    @Getter @Persisted @DescSynced private int radius = FleetBaseCoreConfig.DEFAULT_RADIUS.getDefault();
    @Getter @Persisted @DescSynced private boolean enabled = true;
    @Getter @Persisted @DescSynced private boolean forceLoaded = false;
    @Getter @DescSynced private boolean boundToSubLevel = false;

    @Nullable private String forceLoadSubLevelId;

    public FleetBaseCoreBlockEntity(BlockPos pos, BlockState state) {
        super(SableSpawnerBlockEntities.FLEET_BASE_CORE.get(), pos, state);
    }

    public static void initTicketTypes() {}

    public void tick() {
        if ( this.level == null || this.level.isClientSide ) { return; }
        if ( this.level.getGameTime() % 20 != 0 ) { return; }

        maintainForceLoad();
        if ( this.enabled ) { applyProtect(); }

        this.boundToSubLevel = ( getBoundSubLevel( (ServerLevel) this.level ) != null );

        getFleetBaseCoreTracker().executeTrackerUpdate();
    }
    @Override public void onLoad() {
        super.onLoad();
        if ( this.level instanceof ServerLevel ) {
            this.radius = clampRadius(this.radius);
            getFleetBaseCoreTracker().deferredAppenderAdd(this);
            if ( this.forceLoaded ) { applyForceLoad(); }

            this.boundToSubLevel = ( getBoundSubLevel( (ServerLevel) this.level ) != null );
        }
    }
    @Override public void setRemoved() {
        if ( this.level instanceof ServerLevel ) {
            getFleetBaseCoreTracker().deferredRemoverAdd(this);
        }
        super.setRemoved();
    }

    private void releaseOldSubLevelTicket(ServerLevel serverLevel) {
        if ( this.forceLoadSubLevelId == null ) { return; }

        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if ( container == null ) { return; }

        SubLevel old = container.getSubLevel(UUID.fromString(this.forceLoadSubLevelId));
        if ( old instanceof ServerSubLevel serverSubLevel ) {
            container.removeForceLoadTicket(serverSubLevel, FORCE_LOAD_TICKET, this.coreId);
        }
    }

    private void applyProtect() {
        if ( !(this.level instanceof ServerLevel serverLevel) ) { return; }

        Vec3 center = getWorldPos();
        double radiusSqr = (double) this.radius * this.radius;

        for ( ServerPlayer player : serverLevel.players() ) {
            if ( player.distanceToSqr(center) > radiusSqr ) { continue; }

            PlayerStatus status = getPlayerManager().getStatus(player);
            if ( status == null ) { continue; }

            status.protect();
        }
    }

    public static final SubLevelLoadingTicketType<UUID> FORCE_LOAD_TICKET =
            SubLevelLoadingTicketType.create(
                    ResourceLocation.fromNamespaceAndPath(SableSpawner.MODID, "fleet_core"),
                    UUIDUtil.CODEC);
    public void applyForceLoad() {
        if ( !(this.level instanceof ServerLevel serverLevel) ) { return; }

        ServerSubLevel sub = getBoundSubLevel(serverLevel);
        this.forceLoadSubLevelId = sub == null ? null : sub.getUniqueId().toString();

        if ( sub != null ) {
            ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
            if ( container == null ) { return; }

            if ( this.forceLoaded ) { container.addForceLoadTicket(sub, FORCE_LOAD_TICKET, this.coreId); }
            else { container.removeForceLoadTicket(sub, FORCE_LOAD_TICKET, this.coreId); }
            return;
        }

        serverLevel.setChunkForced(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4, this.forceLoaded);
    }
    private void maintainForceLoad() {
        if ( !this.forceLoaded || !(this.level instanceof ServerLevel serverLevel) ) { return; }

        ServerSubLevel sub = getBoundSubLevel(serverLevel);
        String current = sub == null ? null : sub.getUniqueId().toString();
        if ( Objects.equals(current, this.forceLoadSubLevelId) ) { return; }

        releaseOldSubLevelTicket(serverLevel);
        applyForceLoad();
    }
    public static void releaseForceLoad(ServerLevel level, BlockPos pos, @Nullable UUID coreId) {
        ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if ( container != null && container.inBounds(pos) && coreId != null ) {
            LevelPlot plot = container.getPlot(new ChunkPos(pos));
            if ( plot != null && plot.getSubLevel() instanceof ServerSubLevel sub ) {
                container.removeForceLoadTicket(sub, FORCE_LOAD_TICKET, coreId);
                return;
            }
        }

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        for ( var entry : level.getChunk(chunkX, chunkZ).getBlockEntities().entrySet() ) {
            if ( entry.getKey().equals(pos) ) { continue; }
            if ( entry.getValue() instanceof FleetBaseCoreBlockEntity core && core.forceLoaded ) { return; }
        }
        level.setChunkForced(chunkX, chunkZ, false);
    }


    @Nullable public ServerSubLevel getBoundSubLevel(ServerLevel serverLevel) {
        ServerSubLevelContainer container = SubLevelContainer.getContainer(serverLevel);
        if ( container == null || !container.inBounds(this.worldPosition) ) { return null; }

        LevelPlot plot = container.getPlot(new ChunkPos(this.worldPosition));
        return plot != null && plot.getSubLevel() instanceof ServerSubLevel sub ? sub : null;
    }
    public Vec3 getWorldPos() {
        if ( !(this.level instanceof ServerLevel serverLevel) ) { return Vec3.atCenterOf(this.worldPosition); }
        if ( !this.boundToSubLevel ) { return Vec3.atCenterOf(this.worldPosition); }

        ServerSubLevel sub = getBoundSubLevel(serverLevel);
        if ( sub == null ) { return Vec3.atCenterOf(this.worldPosition); }

        return sub.logicalPose().transformPosition(Vec3.atCenterOf(this.worldPosition));
    }

    public boolean covers(Vec3 pos) {
        double radiusSqr = (double) this.radius * this.radius;
        return pos.distanceToSqr(getWorldPos()) <= radiusSqr;
    }

    public void setRadius(int radius) {
        this.radius = clampRadius(radius);
        this.setChanged();
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.setChanged();
    }
    public void switchEnabled() {
        this.enabled = !this.enabled;
        this.setChanged();
    }
    public void setForceLoaded(boolean forceLoaded) {
        if ( this.forceLoaded == forceLoaded ) { return; }
        this.forceLoaded = forceLoaded;
        applyForceLoad();
        this.setChanged();
    }
    public void setOwner(@Nullable LivingEntity placer) {
        if ( !(placer instanceof ServerPlayer player) ) { return; }
        if ( player instanceof FakePlayer) { return; }

        this.owner = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        this.setChanged();
    }

    private static int clampRadius(int value) {
        return Math.max(FleetBaseCoreConfig.MIN_RADIUS.getAsInt(),
                Math.min(FleetBaseCoreConfig.MAX_RADIUS.getAsInt(), value));
    }

}
