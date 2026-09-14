package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import dev.sable.sablespawner.util.FileIOUtil;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class BlueprintScanner {

    public static Set<BlueprintEntry> scanBlueprintsOfPack(DatapackSource datapack) {
        getLogger().info("正在扫描包内蓝图文件");

        Set<BlueprintEntry> entries = new HashSet<>();

        Set<String> files = FileIOUtil.treeDatapackFileDir(datapack, "data/blueprints");
        files.addAll( FileIOUtil.treeDatapackFileDir(datapack, "data/schematics") );

        for ( String path : files ) {
            entries.add( BlueprintEntry.ofUnknownSource(datapack, path) );
        }
        return entries;
    }
    public static Set<BlueprintEntry> scanBlueprintsOfFolder() {
        getLogger().info("正在扫描文件夹内蓝图文件");

        Set<BlueprintEntry> entries = new HashSet<>();

        for ( DatapackManager.BlueprintSourceModId modId : DatapackManager.BlueprintSourceModId.values() ) {
            if ( modId == DatapackManager.BlueprintSourceModId.invalid ) { continue; }
            if ( !getModList().isLoaded( modId.toString() ) ) { continue; }

            getLogger().info("发现 mod id：{} 正在从文件夹加载蓝图", modId);
            entries.addAll( scanModFolder(modId) );
        }
        return entries;
    }
    public static Set<BlueprintEntry> scanModFolder(DatapackManager.BlueprintSourceModId modId) {
        return switch ( modId ) {
            case sable_schematic_api -> {
                Path folder = getSableSchematicApiFolder();

                Set<BlueprintEntry> entries = new HashSet<>();
                for ( String relative : FileIOUtil.treeDir(folder) ) {
                    entries.add( BlueprintEntry.ofPath(modId, folder.resolve(relative)) );
                }
                yield entries;
            }
            default -> new HashSet<>();
        };
    }


    private static Path getSableSchematicApiFolder() {
        return getGameDir().resolve("Sable-Schematics");
    }

    private static ModList getModList() {
        return ModList.get();
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

}
