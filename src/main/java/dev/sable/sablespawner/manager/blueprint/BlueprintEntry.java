package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import dev.sable.sablespawner.util.FileIOUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;

public record BlueprintEntry(
        DatapackManager.BlueprintSourceModId sourceMod,
        @Nullable DatapackSource datapackSource,
        @Nullable String path,
        @Nullable Object object
) {
    public static BlueprintEntry ofUnknownSource(Path path) {
        BlueprintEntry e = new BlueprintEntry(
                DatapackManager.BlueprintSourceModId.auto,
                null,
                FileIOUtil.pathToString(path),
                null
        );

        return parseAutoSource(e);
    }
    public static BlueprintEntry ofUnknownSource(DatapackSource datapack, String path) {
        BlueprintEntry e = new BlueprintEntry(
                DatapackManager.BlueprintSourceModId.auto,
                datapack,
                path,
                null
        );

        return parseAutoSource(e);
    }

    public static BlueprintEntry ofPath(DatapackManager.BlueprintSourceModId sourceMod, Path path) {
        return new BlueprintEntry(sourceMod, null, FileIOUtil.pathToString(path),null);
    }
    public static BlueprintEntry ofDatapackPath(DatapackManager.BlueprintSourceModId sourceMod, DatapackSource datapack, String path) {
        return new BlueprintEntry(sourceMod, datapack, path, null);
    }

    public static BlueprintEntry updateObject(BlueprintEntry oldEntry, Object object) {
        return new BlueprintEntry(oldEntry.sourceMod, oldEntry.datapackSource, oldEntry.path, object);
    }
    public static BlueprintEntry updateSource(BlueprintEntry oldEntry, DatapackManager.BlueprintSourceModId newSource) {
        return new BlueprintEntry(newSource, oldEntry.datapackSource, oldEntry.path, oldEntry.object);
    }
    public static BlueprintEntry parseAutoSource(BlueprintEntry oldEntry) {
        DatapackManager.BlueprintSourceModId newSource =
                BlueprintInterpreter.interpretBlueprintSource( oldEntry );

        return new BlueprintEntry(newSource, oldEntry.datapackSource, oldEntry.path, oldEntry.object);
    }

    public boolean isValidEntry() {
        return this.sourceMod != DatapackManager.BlueprintSourceModId.invalid;
    }
    public boolean hasPathReference() {
        return ( this.datapackSource == null && this.path != null );
    }
    public boolean hasDatapackPathReference() {
        return ( this.datapackSource != null && this.path != null );
    }
    public boolean isBuffered() {
        return this.object != null;
    }

    public String getEntryType() {
        if ( this.hasPathReference() || this.hasDatapackPathReference() ) { return "Reference"; }
        if ( this.isBuffered() ) { return object.getClass().getSimpleName(); }
        return "null";
    }

}
