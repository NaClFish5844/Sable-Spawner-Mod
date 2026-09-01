package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.DatapackManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BlueprintProvider {
    private BlueprintProvider(){}

    public static Set<ResourceLocation> readDatapackBlueprints(DatapackManager.BlueprintSourceModId modId, ResourceManager resourceManager ) {
        Set<ResourceLocation> resourceLocations = new HashSet<>();
        switch ( modId ) {
            case sable_schematic_api -> {
                if ( !getModList().isLoaded("sable_schematic_api") ) { return resourceLocations; }
                resourceLocations = resourceManager.listResources(
                        "sablespawner/schematics", p -> p.toString().endsWith(".nbt")
                ).keySet();
                return resourceLocations;
            }
            default -> {
                return resourceLocations;
            }
        }
    }
    public static Set<Path> readFolderBlueprints(DatapackManager.BlueprintSourceModId modId) {
        Set<Path> files = new HashSet<>();
        switch ( modId ) {
            case sable_schematic_api -> {
                if ( !getModList().isLoaded("sable_schematic_api") ) { return files; }

                Path folder = getGameDir().resolve("Sable-Schematics");
                return scanBlueprintFolder( folder, ".nbt" );
            }
            default -> { return files; }
        }
    }
    private static Set<Path> scanBlueprintFolder(Path path, String suffix ) {
        Set<Path> files = new HashSet<>();

        if ( !Files.isDirectory(path) ) { return files; }

        try ( Stream<Path> stream = Files.list(path) ) {
            stream
                    .filter(Files::isRegularFile)
                    .filter( p -> p.getFileName().toString().endsWith(suffix) )
                    .forEach( files::add );

        } catch ( IOException e ) {
            getLogger().error("此目录下的蓝图加载失败：{}", path, e);
        }

        return files;
    }

    private DatapackManager.BlueprintSourceModId parseAutoSource() {
        // 这是未填写源mod的蓝图的自动解析 预计非常复杂 以后再说
        return DatapackManager.BlueprintSourceModId.invalid;
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
}
