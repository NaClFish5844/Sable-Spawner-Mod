package dev.sable.sablespawner.datapack;

import com.google.gson.*;
import dev.sable.sablespawner.datapack.blueprint.BlueprintEntry;
import dev.sable.sablespawner.datapack.blueprint.PropertyKey;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;

import java.nio.file.Path;


public class LoadDatapack {
    private LoadDatapack() {}

    private enum Type {

    }

    protected static Object2ObjectMap.Entry<PropertyKey, BlueprintEntry> loadBlueprints(ResourceManager resourceManager) {

        return null;
    }
    protected static Object2ObjectMap.Entry<PropertyKey, AbstractSchematicProperty> parseProperty(JsonElement json) {
        checkPropertyFormat( json );
        return null;
    }
    protected static Object2ObjectMap.Entry<String, DefaultConfig> parseWorldConfig(JsonElement json) {
        checkWorldConfigFormat( json );
        return null;
    }

    protected static boolean checkPropertyFormat(JsonElement json) {
        return false;
    }
    protected static boolean checkWorldConfigFormat(JsonElement json) {
        return false;
    }

    // BlueprintProvider
    protected static ObjectList<Path> readFolderBlueprints(DatapackManager.BlueprintSourceModId modId) {
        ObjectList<Path> paths = new ObjectArrayList<>();
        switch ( modId ) {
            case sable_schematic_api -> {
                if ( !getModList().isLoaded("sable_schematic_api") ) { return paths; }
                paths = scanFolder(""); // 开扫
            }
        }

        return paths;
    }

    private static ObjectList<Path> scanFolder(Path path) {
        ObjectList<Path> paths = new ObjectArrayList<>();
        // 扫啊
        return paths;
    }


    // 一个防呆机制 防止有人把config放进property之类的操作
    // “开启强力检测”
    // 直接从Path侧重读压缩包
    // 以后再说
    private static Type interpretType(JsonObject object) {
        return null;
    }
    private static JsonObject loadFile(Path path) {
        // json -> JsonObject
        return null;
    }
    // 读列表
    private static ObjectList<Path> readProperties() {
        return null;
    }
    private static ObjectList<Path> readWorldConfigs() {
        return null;
    }
    private static ObjectList<ResourceLocation> readDatapackBlueprints() {
        return null;
    }

    private static ModList getModList() {
        return ModList.get();
    }
}
