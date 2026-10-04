package dev.sablespawner.registry;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.blockentity.FleetBaseCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SableSpawnerBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SableSpawner.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FleetBaseCoreBlockEntity>> FLEET_BASE_CORE =
            BLOCK_ENTITIES.register("fleet_base_core",
                    () -> BlockEntityType.Builder
                            .of(FleetBaseCoreBlockEntity::new, SableSpawnerBlocks.FLEET_BASE_CORE.get())
                            .build(null));
}
