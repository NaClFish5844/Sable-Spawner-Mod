package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.util.FileHashUtil;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;

public record BlueprintKey(
        String name,
        DatapackManager.BlueprintSourceModId sourceMod,
        DatapackManager.BlueprintSourceFileLocation sourceFileLocation,
        String fileHash
) {
    public static BlueprintKey of(
            String name,
            DatapackManager.BlueprintSourceModId modId,
            DatapackManager.BlueprintSourceFileLocation sourceLocation,
            String fileHash
    ) {
        return new BlueprintKey(name, modId, sourceLocation, fileHash);
    }
    public static BlueprintKey of(
            BlueprintEntry referenceBlueprintEntry,
            DatapackManager.BlueprintSourceModId sourceMod
    ) {
        Object object = referenceBlueprintEntry.object();
        if ( object instanceof Path path ) {
            return new BlueprintKey(
                    path.getFileName().toString(),
                    sourceMod,
                    DatapackManager.BlueprintSourceFileLocation.folder,
                    FileHashUtil.getFileMD5(path)
            );
        }
        if ( object instanceof ResourceLocation resourceLocation ) {
            String resourcePath = resourceLocation.getPath();
            return new BlueprintKey(
                    resourcePath.substring( resourcePath.lastIndexOf('/') + 1 ),
                    sourceMod,
                    DatapackManager.BlueprintSourceFileLocation.datapack,
                    FileHashUtil.getFileMD5(resourceLocation)
            );
        }
        return null;
    }

}
