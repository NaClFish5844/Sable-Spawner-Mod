package dev.sable.sablespawner.manager.blueprint;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import dev.sable.sablespawner.util.FileIOUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public final class BlueprintScanner {

    public static Set<BlueprintEntry> scanBlueprintsOfPack(DatapackSource datapack) {
        getLogger().info("正在扫描包内蓝图文件");

        Set<BlueprintEntry> entries = new HashSet<>();
        Set<String> files = FileIOUtil.treeDatapackFileDir(datapack, "data/blueprints");

        if ( files.isEmpty() ) { return entries; }

        for ( String path : files ) {
            BlueprintEntry e = BlueprintEntry.ofUnknownSource(datapack, path);
        }

    }
    public static Set<BlueprintEntry> scanBlueprintsOfFolder() {
        getLogger().info("正在扫描文件夹内蓝图文件");

        Set<BlueprintEntry> entries = new HashSet<>();
        Set<String> files = new HashSet<>();

        for ( DatapackManager.BlueprintSourceModId modId : DatapackManager.BlueprintSourceModId.values() ) {
            String mod = modId.toString();
            if ( !getModList().isLoaded(mod) ) { continue; }

            getLogger().info("发现 mod id：{} 正在从文件夹加载蓝图", mod);

            files.addAll( scanModFolder(modId) );
        }

        return entries;
    }
    public static Set<String> scanModFolder(DatapackManager.BlueprintSourceModId modId) {
        return switch ( modId ) {
            case sable_schematic_api -> FileIOUtil.treeDir(getSableSchematicApiFolder());
            default ->  new HashSet<>();
        };
    }

    public static Pair<String, Object> getBlueprintObjectOfRef(Path blueprintPath, DatapackManager.BlueprintSourceModId modId) {
        DatapackManager.BlueprintSourceModId mid;
        mid = modId;

        if ( mid == DatapackManager.BlueprintSourceModId.auto ) { mid = BlueprintInterpreter.interpretBlueprintSource(blueprintPath); }
        switch (mid) {
            case sable_schematic_api -> {
                try {
                    String type = "SableBlueprint";
                    CompoundTag tag = NbtIo.readCompressed(blueprintPath, NbtAccounter.unlimitedHeap());
                    Object blueprintObject = SableBlueprint.load(tag);

                    return Pair.of(type, blueprintObject);

                } catch (IOException e) {
                    getLogger().error("蓝图加载失败");
                    return Pair.of("path", null);
                }
            }
            default -> {
                return Pair.of("path", null);
            }
        }
    }
    public static Pair<String, Object> getBlueprintObjectOfRef(InputStream blueprintStream, DatapackManager.BlueprintSourceModId modId) {
        DatapackManager.BlueprintSourceModId mid;
        mid = modId;

        if ( mid == DatapackManager.BlueprintSourceModId.auto ) { mid = BlueprintInterpreter.interpretBlueprintSource(blueprintStream); }
        switch (mid) {
            case sable_schematic_api -> {
                try {
                    String type = "SableBlueprint";
                    CompoundTag tag = NbtIo.readCompressed(blueprintStream, NbtAccounter.unlimitedHeap());
                    Object blueprintObject = SableBlueprint.load(tag);

                    return Pair.of(type, blueprintObject);

                } catch (IOException e) {
                    getLogger().error("蓝图加载失败");
                    return Pair.of("path", null);
                }
            }
            default -> {
                return Pair.of("path", null);
            }
        }
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
    private static ObjectSet<DatapackSource> getDatapackRegistry() {
        return SableSpawner.DATAPACK_MANAGER.getDATAPACK_REGISTRY();
    }



}
