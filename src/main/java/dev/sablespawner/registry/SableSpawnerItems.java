package dev.sablespawner.registry;

import dev.sablespawner.SableSpawner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SableSpawnerItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(SableSpawner.MODID);

    public static final DeferredItem<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item");

    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("example_block", SableSpawnerBlocks.EXAMPLE_BLOCK);

    public static final DeferredItem<BlockItem> FLEET_BASE_CORE_ITEM =
            ITEMS.registerSimpleBlockItem("fleet_base_core", SableSpawnerBlocks.FLEET_BASE_CORE);
}
