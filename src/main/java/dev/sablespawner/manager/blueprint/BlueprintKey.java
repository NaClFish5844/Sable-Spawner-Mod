package dev.sablespawner.manager.blueprint;

import dev.sablespawner.manager.datapack.DatapackSource;
import dev.sablespawner.util.FileHashUtil;

import java.nio.file.Path;

public record BlueprintKey(
        String name,
        String fileHash,
        String source
) {
    public static BlueprintKey of(String name, String fileHash, String source) {
        return new BlueprintKey(name, fileHash, source);
    }
    public static BlueprintKey ofEntry(BlueprintEntry entry, String source) {
        String path = entry.path();
        if ( path == null ) { return null; }

        DatapackSource datapack = entry.datapackSource();
        String fileHash = null;
        if (entry.hasPathReference()) { fileHash = FileHashUtil.getFileMD5(Path.of(path)); }
        if (entry.hasDatapackPathReference()) { fileHash = FileHashUtil.getDatapackFileMD5(datapack, path); }

        if ( fileHash == null ) { return null; }

        return new BlueprintKey( getFileName(path), fileHash, source );
    }

    static String getFileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
}
