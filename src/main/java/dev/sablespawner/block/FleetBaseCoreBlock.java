package dev.sablespawner.block;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import dev.sablespawner.gui.FleetBaseCoreBlockUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FleetBaseCoreBlock extends Block implements EntityBlock, BlockUIMenuType.BlockUI {
    public FleetBaseCoreBlock(Properties properties) {
        super(properties);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if ( !state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel ) {
            UUID coreId = level.getBlockEntity(pos) instanceof FleetBaseCoreBlockEntity core ? core.getCoreId() : null;
            FleetBaseCoreBlockEntity.releaseForceLoad(serverLevel, pos, coreId);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if ( level.getBlockEntity(pos) instanceof FleetBaseCoreBlockEntity core ) {
            core.setOwner(placer);
        }
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if ( level.isClientSide ) { return InteractionResult.SUCCESS; }

        if ( level.getBlockEntity(pos) instanceof FleetBaseCoreBlockEntity core ) {
            if ( player.isShiftKeyDown() ) {
                core.switchEnabled();
                return InteractionResult.CONSUME;
            }
            if ( player instanceof ServerPlayer serverPlayer ) {
                BlockUIMenuType.openUI(serverPlayer, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FleetBaseCoreBlockEntity(pos, state);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if ( level.isClientSide ) { return null; }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if ( blockEntity instanceof FleetBaseCoreBlockEntity core ) { core.tick(); }
        };
    }

    @Override public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) { return FleetBaseCoreBlockUI.build(holder); }
}
