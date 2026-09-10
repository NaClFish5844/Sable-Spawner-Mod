package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.util.FileHashUtil;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public record BlueprintKey(
        String name,
        DatapackManager.BlueprintSourceModId sourceMod,
        String fileHash
) {
    public static BlueprintKey of(
            String name,
            DatapackManager.BlueprintSourceModId modId,
            String fileHash
    ) {
        return new BlueprintKey(name, modId, fileHash);
    }
    @Deprecated
    public static BlueprintKey of(
            String filePath,
            DatapackManager.BlueprintSourceModId sourceMod
    ) {
        Path path = resolveAsPath(filePath);
        if ( path == null ) { return null; }
        return new BlueprintKey(
                path.getFileName().toString(),
                sourceMod,
                FileHashUtil.getFileMD5(path)
        );
    }
    @Deprecated
    public static BlueprintKey of(
            Path filePath,
            DatapackManager.BlueprintSourceModId sourceMod
    ) {
        return new BlueprintKey(
                filePath.getFileName().toString(),
                sourceMod,
                FileHashUtil.getFileMD5(filePath)
        );
    }

    private static Path resolveAsPath(Object object) { // String/Path ->resolveName
        if ( object instanceof Path path ) { return path; }
        if ( object instanceof String str ) {
            try {
                return Path.of(str);
            } catch ( InvalidPathException e ) {
                return null;
            }
        }
        return null;
    }

}
