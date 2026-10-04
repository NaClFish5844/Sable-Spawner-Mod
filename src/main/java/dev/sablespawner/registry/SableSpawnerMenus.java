package dev.sablespawner.registry;

import dev.sablespawner.SableSpawner;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SableSpawnerMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SableSpawner.MODID);

}
