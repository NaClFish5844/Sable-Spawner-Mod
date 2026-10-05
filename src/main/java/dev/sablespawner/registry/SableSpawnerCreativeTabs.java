package dev.sablespawner.registry;

import dev.sablespawner.SableSpawner;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SableSpawnerCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SableSpawner.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sablespawner"))
                    .icon(() -> new ItemStack(SableSpawnerItems.EXAMPLE_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SableSpawnerItems.EXAMPLE_ITEM.get());
                        output.accept(SableSpawnerItems.FLEET_BASE_CORE_ITEM.get());
                    })
                    .build());
}
