package dev.sable.sablespawner.manager.blueprint;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class BlueprintInterpreter {
    // 这是未填写源mod的蓝图的自动解析 预计非常复杂 以后再说
    // 不对 我可以把文件路径塞进解析器
    // 还可以把prop的查询也塞进去
    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(BlueprintEntry entry) {
        DatapackManager.BlueprintSourceModId ignore = entry.sourceMod();

        if ( entry.hasPathReference() ) {
            return interpretBlueprintSource(entry.path(), ignore);
        }
        if ( entry.hasDatapackPathReference() ) {
            return interpretBlueprintSource(entry.datapackSource(), entry.path(), ignore);
        }


        return DatapackManager.BlueprintSourceModId.invalid;
    }


    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(String filePath, DatapackManager.BlueprintSourceModId ignore) {
        // 实际上会自动忽略auto
        // 从这里出去的entry 源类型必然不是auto
        // ignore是用来排除错误类型的
        // 又：最多调用两层方法 即给出两种解释
        // 因此只需要一个变量记录ignore 而不需要List
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    public static DatapackManager.BlueprintSourceModId interpretBlueprintSource(DatapackSource datapack, String path, DatapackManager.BlueprintSourceModId ignore) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }


    private static DatapackManager.BlueprintSourceModId byDirName(String filePath) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    private static DatapackManager.BlueprintSourceModId byDatapackProperty(DatapackSource datapack, String path) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    private static DatapackManager.BlueprintSourceModId byNBTFormat(String filePath) {
        return DatapackManager.BlueprintSourceModId.invalid;
    }
    private static DatapackManager.BlueprintSourceModId byNBTFormat(DatapackSource datapack, String Path) {
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
    private static Path getSableSchematicApiFolder() {
        return getGameDir().resolve("Sable-Schematics");
    }
    private static ObjectSet<DatapackSource> getDatapackRegistry() {
        return SableSpawner.DATAPACK_MANAGER.getDATAPACK_REGISTRY();
    }

}
