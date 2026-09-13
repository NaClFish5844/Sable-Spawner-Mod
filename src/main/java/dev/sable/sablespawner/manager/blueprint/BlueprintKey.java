package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.manager.datapack.DatapackSource;
import dev.sable.sablespawner.util.FileHashUtil;

import java.nio.file.Path;

public record BlueprintKey(
        String name,
        String fileHash
) {
    public static BlueprintKey of(String name, String fileHash) {
        return new BlueprintKey(name, fileHash);
    }
    public static BlueprintKey ofEntry(BlueprintEntry entry) {
        String path = entry.path();
        if ( path == null ) { return null; }

        DatapackSource datapack = entry.datapackSource();
        String fileHash = ( datapack == null )
                ? FileHashUtil.getFileMD5( Path.of(path) )
                : FileHashUtil.getDatapackFileMD5( datapack, path );

        if ( fileHash == null ) { return null; }

        return new BlueprintKey( getFileName(path), fileHash );
    }

    private static String getFileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
