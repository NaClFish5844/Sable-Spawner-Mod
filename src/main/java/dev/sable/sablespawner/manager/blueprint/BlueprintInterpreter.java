package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import dev.sable.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sable.sablespawner.util.FileIOUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;

public final class BlueprintInterpreter {

    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(BlueprintEntry entry, @Nullable DatapackManager.BlueprintSourceModId ignore) {

        if ( entry.hasPathReference() ) {
            return interpretBlueprintSource(entry.path(), ignore);
        }
        if ( entry.hasDatapackPathReference() ) {
            return interpretBlueprintSource(entry.datapackSource(), entry.path(), ignore);
        }

        return DatapackManager.BlueprintSourceModId.invalid;
    }

    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(String filePath, @Nullable DatapackManager.BlueprintSourceModId ignore) {
        DatapackManager.BlueprintSourceModId result = byDirName(filePath);
        if ( isUsable(result, ignore) ) { return result; }

        result = byNBTFormat(filePath);
        if ( isUsable(result, ignore) ) { return result; }

        return DatapackManager.BlueprintSourceModId.invalid;
    }
    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(DatapackSource datapack, String path, @Nullable DatapackManager.BlueprintSourceModId ignore) {
        DatapackManager.BlueprintSourceModId result = byDatapackProperty(datapack, path);
        if ( isUsable(result, ignore) ) { return result; }

        result = byNBTFormat(datapack, path);
        if ( isUsable(result, ignore) ) { return result; }

        return DatapackManager.BlueprintSourceModId.invalid;
    }

    private static boolean isUsable(DatapackManager.BlueprintSourceModId result, @Nullable DatapackManager.BlueprintSourceModId ignore) {
        if ( result == null ) { return false; }
        if ( result == ignore ) { return false; }
        return result != DatapackManager.BlueprintSourceModId.invalid;
    }

    private static DatapackManager.BlueprintSourceModId byDirName(String filePath) {
        for ( DatapackManager.BlueprintSourceModId modId : DatapackManager.BlueprintSourceModId.values() ) {
            boolean matched = switch ( modId ) {
                case sable_schematic_api -> filePath.startsWith( FileIOUtil.pathToString( getSableSchematicApiFolder() ) + "/" );
                default -> false;
            };

            if ( matched ) { return modId; }
        }
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    private static DatapackManager.BlueprintSourceModId byDatapackProperty(DatapackSource datapack, String path) {
        String packName = datapack.getPackMeta().packName();
        String fileName = getFileName(path);

        for ( AbstractSchematicProperty property : getPropertyManager().values() ) {
            if ( property.getSchematicSource() != DatapackManager.BlueprintSourceFileLocation.datapack ) { continue; }
            if ( !Objects.equals( property.getPackName(), packName ) ) { continue; }
            if ( !Objects.equals( property.getSchematicName(), fileName ) ) { continue; }

            return property.getSourceModId();
        }
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    // 就这么放着吧 估计一段时间内不会动它
    // 但在组装时要把它当作能工作的方法写进去
    private static DatapackManager.BlueprintSourceModId byNBTFormat(String filePath) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    private static DatapackManager.BlueprintSourceModId byNBTFormat(DatapackSource datapack, String Path) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }

    private static String getFileName(String path) {
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }
    private static ModList getModList() {
        return ModList.get();
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static Path getSableSchematicApiFolder() {
        return getGameDir().resolve("Sable-Schematics");
    }
    private static ObjectSet<DatapackSource> getDatapackRegistry() {
        return SableSpawner.DATAPACK_MANAGER.getDATAPACK_REGISTRY();
    }
    private static Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> getPropertyManager() {
        return SableSpawner.DATAPACK_MANAGER.getPROPERTY_MANAGER();
    }

}
