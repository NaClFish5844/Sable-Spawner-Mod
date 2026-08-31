package dev.sable.sablespawner.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import dev.sable.sablespawner.SableSpawner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DatapackReloadListener extends SimpleJsonResourceReloadListener {

    public DatapackReloadListener() {
        super(new Gson(), "sablespawner");
    }

    @Override
    protected void apply(
        @NotNull Map<ResourceLocation, JsonElement> files,
        @NotNull ResourceManager resourceManager,
        @NotNull ProfilerFiller profiler)
    {
        SableSpawner.RESOURCE_MANAGER = resourceManager;
        getDatapackManager().loadDatapack( files );
    }

    private static DatapackManager getDatapackManager() {
        return SableSpawner.DATAPACK_MANAGER;
    }

}
