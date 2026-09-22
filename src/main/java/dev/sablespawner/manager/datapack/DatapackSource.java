package dev.sablespawner.manager.datapack;

import lombok.Getter;

import javax.annotation.Nullable;
import java.nio.file.Path;

@Getter
public class DatapackSource {
    Path root;
    @Nullable String validRootEntry;
    PackMeta packMeta;

    private DatapackSource(Path root, @Nullable String validRootEntry, PackMeta packMeta) {
        this.root = root;
        this.validRootEntry = validRootEntry;
        this.packMeta = packMeta;
    }

    @Deprecated
    public static DatapackSource of(Path root, String relativeValidRoot, PackMeta packMeta) {
        return new DatapackSource(root, relativeValidRoot, packMeta);
    }
    public static DatapackSource ofDir(Path root, PackMeta packMeta) {
        return new DatapackSource(root, null, packMeta);
    }
    public static DatapackSource ofZip(Path root, String relativeValidRoot, PackMeta packMeta) {
        return new DatapackSource(root, relativeValidRoot, packMeta);
    }


    public boolean isZipFile() {
        return this.root.toString().endsWith(".zip");
    }

}