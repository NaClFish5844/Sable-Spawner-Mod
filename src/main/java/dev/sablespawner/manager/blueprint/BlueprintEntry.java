package dev.sablespawner.manager.blueprint;

import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.DatapackSource;
import dev.sablespawner.util.FileIOUtil;

import javax.annotation.Nullable;
import java.nio.file.Path;

public record BlueprintEntry(
        DatapackManager.BlueprintSourceModId sourceMod,
        @Nullable DatapackSource datapackSource,
        @Nullable String path,
        @Nullable Object object
) {
    public static BlueprintEntry ofUnknownSource(String path) {
        DatapackManager.BlueprintSourceModId source =
                BlueprintInterpreter.interpretBlueprintSource(path, null);

        return new BlueprintEntry(
                source,
                null,
                path,
                null
        );
    }
    public static BlueprintEntry ofUnknownSource(DatapackSource datapack, String path) {
        DatapackManager.BlueprintSourceModId source =
                BlueprintInterpreter.interpretBlueprintSource(datapack, path, null);

        return new BlueprintEntry(
                source,
                datapack,
                path,
                null
        );
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
    public static BlueprintEntry autoParseSource(BlueprintEntry oldEntry) {
        DatapackManager.BlueprintSourceModId newSource =
                BlueprintInterpreter.interpretBlueprintSource(oldEntry, oldEntry.sourceMod);

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
        if ( this.isBuffered() ) { return object.getClass().getSimpleName(); }
        if ( this.hasPathReference() || this.hasDatapackPathReference() ) { return "Reference"; }
        return "null";
    }

}
