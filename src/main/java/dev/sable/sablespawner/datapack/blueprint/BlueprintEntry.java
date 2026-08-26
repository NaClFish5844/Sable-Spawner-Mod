package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.datapack.DatapackManager;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.nio.file.Path;

public record BlueprintEntry(
        @Nullable Object blueprintObject,
        @Nullable ResourceLocation datapackResourceLocation,
        @Nullable Path filePath
) {
    public DatapackManager.BlueprintSourceFileLocation getSourceFileLocationType() {
        if ( datapackResourceLocation == null && filePath == null ) { return DatapackManager.BlueprintSourceFileLocation.invalid; }
        if ( datapackResourceLocation != null ) { return DatapackManager.BlueprintSourceFileLocation.datapack; }
        return DatapackManager.BlueprintSourceFileLocation.folder;
    }

    public boolean fromDatapack() {
        return getSourceFileLocationType() == DatapackManager.BlueprintSourceFileLocation.datapack;
    }
    public boolean fromFolder() {
        return getSourceFileLocationType() == DatapackManager.BlueprintSourceFileLocation.folder;
    }
    public boolean isBuffered() {
        return blueprintObject != null;
    }

}
