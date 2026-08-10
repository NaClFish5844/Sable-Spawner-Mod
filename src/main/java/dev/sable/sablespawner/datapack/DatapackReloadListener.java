package dev.sable.sablespawner.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DatapackReloadListener extends SimpleJsonResourceReloadListener {
    private final DatapackManager manager;

    public DatapackReloadListener(DatapackManager manager) {
        super(new Gson(), "sablespawner/properties");
        this.manager = manager;
    }

    @Override
    protected void apply(
        @NotNull Map<ResourceLocation, JsonElement> files,
        @NotNull ResourceManager resourceManager,
        @NotNull ProfilerFiller profiler)
    {
        manager.loadDatapack(files, resourceManager);
    }
}
