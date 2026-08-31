package dev.sable.sablespawner.datapack;

import com.google.gson.*;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.blueprint.PropertyKey;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.slf4j.Logger;

import java.nio.file.Path;


@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class LoadDatapack {
    private LoadDatapack() {}

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();


    protected static Object2ObjectMap.Entry<PropertyKey, AbstractSchematicProperty> parseProperty(JsonElement json) {
        JsonObject object = json.getAsJsonObject();
        Checker.checkPropertyFormat(object);
        return null;
    }
    protected static Object2ObjectMap.Entry<String, DefaultConfig> parseWorldConfig(JsonElement json) {
        JsonObject object = json.getAsJsonObject();
        Checker.checkWorldConfigFormat(object);
        return null;
    }


    // 一个防呆机制 防止有人把config放进property之类的操作
    // “开启强力检测”
    // 直接从Path侧重读压缩包
    // 以后再说
    private enum Type {
        placeholder
    }

    private static Type interpretType(JsonObject object) {
        return null;
    }
    private static JsonObject loadFile(Path path) {
        // .json -> JsonObject
        return null;
    }
    // 读列表
    private static ObjectList<Path> readProperties() {
        return null;
    }
    private static ObjectList<Path> readWorldConfigs() {
        return null;
    }


    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }

}
