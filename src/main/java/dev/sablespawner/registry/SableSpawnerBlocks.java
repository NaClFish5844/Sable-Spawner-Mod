package dev.sablespawner.registry;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.block.FleetBaseCoreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SableSpawnerBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SableSpawner.MODID);

    public static final DeferredBlock<Block> EXAMPLE_BLOCK =
            BLOCKS.register("example_block",
                    () -> new Block(BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.STONE)));

    public static final DeferredBlock<FleetBaseCoreBlock> FLEET_BASE_CORE =
            BLOCKS.register("fleet_base_core",
                    () -> new FleetBaseCoreBlock(BlockBehaviour.Properties.of()
                            .strength(1.5F, 1000.0F)
                            .sound(SoundType.STONE)));

}
