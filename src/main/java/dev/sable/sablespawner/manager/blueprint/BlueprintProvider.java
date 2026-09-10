package dev.sable.sablespawner.manager.blueprint;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BlueprintProvider {
    private BlueprintProvider(){}

    public static Set<Path> scanFolderBlueprints(DatapackManager.BlueprintSourceModId modId) {
        return switch ( modId ) {
            case sable_schematic_api -> scanBlueprintFolder( getSableSchematicApiFolder(), ".nbt" );
            default ->  new HashSet<>();
        };
    }
    private static Set<Path> scanBlueprintFolder(Path path, String suffix ) {
        getLogger().info("正在扫描文件夹中的蓝图文件");

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

    public static Pair<String, Object> getBlueprintObjectOfRef(Path blueprintPath, DatapackManager.BlueprintSourceModId modId) {
        DatapackManager.BlueprintSourceModId mid;
        mid = modId;

        if ( mid == DatapackManager.BlueprintSourceModId.auto ) { mid = interpretBlueprintSource(blueprintPath); }
        switch (mid) {
            case sable_schematic_api -> {
                try {
                    String type = "SableBlueprint";
                    CompoundTag tag = NbtIo.readCompressed(blueprintPath, NbtAccounter.unlimitedHeap());
                    Object blueprintObject = SableBlueprint.load(tag);

                    return Pair.of(type, blueprintObject);

                } catch (IOException e) {
                    getLogger().error("蓝图加载失败");
                    return Pair.of("Path", null);
                }
            }
            default -> {
                return Pair.of("Path", null);
            }
        }
    }
    public static Pair<String, Object> getBlueprintObjectOfRef(InputStream blueprintStream, DatapackManager.BlueprintSourceModId modId) {
        DatapackManager.BlueprintSourceModId mid;
        mid = modId;

        if ( mid == DatapackManager.BlueprintSourceModId.auto ) { mid = interpretBlueprintSource(blueprintStream); }
        switch (mid) {
            case sable_schematic_api -> {
                try {
                    String type = "SableBlueprint";
                    CompoundTag tag = NbtIo.readCompressed(blueprintStream, NbtAccounter.unlimitedHeap());
                    Object blueprintObject = SableBlueprint.load(tag);

                    return Pair.of(type, blueprintObject);

                } catch (IOException e) {
                    getLogger().error("蓝图加载失败");
                    return Pair.of("Path", null);
                }
            }
            default -> {
                return Pair.of("Path", null);
            }
        }
    }

    // 这是未填写源mod的蓝图的自动解析 预计非常复杂 以后再说
    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(Path blueprintPath) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(InputStream blueprintStream) {
        return DatapackManager.BlueprintSourceModId.invalid;
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
