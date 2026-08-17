package dev.sable.sablespawner.datapack;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.AllyProperty;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.datapack.property.PrefabProperty;
import dev.sable.sablespawner.datapack.schematics.SchematicProvider;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class LoadDatapack {
    private LoadDatapack() {}

    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    static Logger LOGGER = SableSpawner.LOGGER;

    private static final Set<String> SCHEMATIC_SOURCES = enumNames(AbstractSchematicProperty.SchematicSource.class);
    private static final Set<String> SOURCE_MOD_IDS = enumNames(AbstractSchematicProperty.SourceModId.class);
    private static final Set<String> SUBLEVEL_TYPES = enumNames(AbstractSchematicProperty.SublevelType.class);
    private static final Set<String> SUBLEVEL_FUNCTIONS = enumNames(AbstractSchematicProperty.SublevelFunction.class);

    private static final Set<String> TOP_LEVEL_KEYS = Set.of(
            "schematic_source", "source_mod_id", "schematic_name",
            "sublevel_types", "sublevel_function",
            "enemy_property", "ally_property", "prefab_property"
    );
    private static final Set<String> ENEMY_KEYS = propertyKeys(EnemyProperty.class);
    private static final Set<String> ALLY_KEYS = propertyKeys(AllyProperty.class);
    private static final Set<String> PREFAB_KEYS = propertyKeys(PrefabProperty.class);

    private static Set<String> enumNames(Class<? extends Enum<?>> enumClass) {
        Set<String> names = new HashSet<>();
        for (Enum<?> constant : enumClass.getEnumConstants()) { names.add(constant.name()); }
        return names;
    }
    private static Set<String> propertyKeys(Class<? extends AbstractSchematicProperty> propertyClass) {
        Set<String> keys = new HashSet<>();
        for (Field field : propertyClass.getDeclaredFields()) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            keys.add(serializedName != null ? serializedName.value() : GSON.fieldNamingStrategy().translateName(field));
        }
        return keys;
    }

    public static boolean checkFormat(Map.Entry<ResourceLocation, JsonElement> entry) {
        ResourceLocation key = entry.getKey();
        JsonObject object = entry.getValue().getAsJsonObject();

        JsonElement sourceElement = object.get("schematic_source");
        if (sourceElement == null || !sourceElement.isJsonPrimitive() || !sourceElement.getAsJsonPrimitive().isString()) {
            LOGGER.warn("[{}] schematic_source 缺失或不是字符串，文件已跳过", key);
            return false;
        }
        if (!SCHEMATIC_SOURCES.contains(sourceElement.getAsString())) {
            LOGGER.warn("[{}] schematic_source 非法: {}（应为 datapack/folder），文件已跳过", key, sourceElement.getAsString());
            return false;
        }

        JsonElement modElement = object.get("source_mod_id");
        if (modElement == null || !modElement.isJsonPrimitive() || !modElement.getAsJsonPrimitive().isString()
                || modElement.getAsString().isEmpty()) {
            LOGGER.warn("[{}] source_mod_id 缺失或为空，文件已跳过", key);
            return false;
        }
        if (!SOURCE_MOD_IDS.contains(modElement.getAsString())) {
            LOGGER.warn("[{}] source_mod_id 未获支持: {}（当前支持 {}），文件已跳过", key, modElement.getAsString(), SOURCE_MOD_IDS);
            return false;
        }

        JsonElement nameElement = object.get("schematic_name");
        if (nameElement == null || !nameElement.isJsonPrimitive() || !nameElement.getAsJsonPrimitive().isString()
                || nameElement.getAsString().isEmpty()) {
            LOGGER.warn("[{}] schematic_name 缺失或为空，文件已跳过", key);
            return false;
        }

        JsonElement typesElement = object.get("sublevel_types");
        if (typesElement == null || !typesElement.isJsonArray()) {
            LOGGER.warn("[{}] sublevel_types 缺失或不是数组，文件已跳过", key);
            return false;
        }

        for (String field : object.keySet()) {
            if (!TOP_LEVEL_KEYS.contains(field)) {
                LOGGER.warn("[{}] 未知顶层字段: {}（将被忽略）", key, field);
            }
        }

        JsonArray types = typesElement.getAsJsonArray();
        for (int i = 0; i < types.size(); i++) {
            JsonElement element = types.get(i);
            String value = element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : null;
            if (value == null || !SUBLEVEL_TYPES.contains(value)) {
                LOGGER.warn("[{}] sublevel_types[{}] 非法: {}（已替换为 neutral）", key, i, value == null ? element : value);
                types.set(i, new JsonPrimitive("neutral"));
            }
        }

        JsonElement functionElement = object.get("sublevel_function");
        if (functionElement != null && (!functionElement.isJsonPrimitive() || !functionElement.getAsJsonPrimitive().isString()
                || !SUBLEVEL_FUNCTIONS.contains(functionElement.getAsString()))) {
            LOGGER.warn("[{}] sublevel_function 非法: {}（已移除，默认 placeholder）", key, functionElement);
            object.remove("sublevel_function");
        }

        checkBlock(key, object, "enemy_property", ENEMY_KEYS);
        checkBlock(key, object, "ally_property", ALLY_KEYS);
        checkPrefabBlock(key, object);

        return true;
    }

    private static void checkBlock(ResourceLocation key, JsonObject object, String blockName, Set<String> allowedKeys) {
        JsonElement blockElement = object.get(blockName);
        if (blockElement == null) { return; }

        if (!blockElement.isJsonObject()) {
            LOGGER.warn("[{}] {} 不是对象（已移除，该类型不生成）", key, blockName);
            object.remove(blockName);
            return;
        }
        for (String field : blockElement.getAsJsonObject().keySet()) {
            if (!allowedKeys.contains(field)) {
                LOGGER.warn("[{}] {}.{} 未知字段（将被忽略）", key, blockName, field);
            }
        }
    }

    private static void checkPrefabBlock(ResourceLocation key, JsonObject object) {
        JsonElement blockElement = object.get("prefab_property");
        if (blockElement == null) { return; }

        if (!blockElement.isJsonObject()) {
            LOGGER.warn("[{}] prefab_property 不是对象（已移除）", key);
            object.remove("prefab_property");
            return;
        }
        JsonObject block = blockElement.getAsJsonObject();

        for (String field : block.keySet()) {
            if (!PREFAB_KEYS.contains(field)) {
                LOGGER.warn("[{}] prefab_property.{} 未知字段（将被忽略）", key, field);
            }
        }

        JsonElement price = block.get("price");
        if (price != null && !isInteger(price)) {
            LOGGER.warn("[{}] prefab_property.price 非法: {}（已移除，默认 0）", key, price);
            block.remove("price");
        }

        JsonElement reusable = block.get("reusable");
        if (reusable != null && (!reusable.isJsonPrimitive() || !reusable.getAsJsonPrimitive().isBoolean())) {
            LOGGER.warn("[{}] prefab_property.reusable 非法: {}（已移除，默认 false）", key, reusable);
            block.remove("reusable");
        }
    }

    private static boolean isInteger(JsonElement element) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) { return false; }
        return element.getAsJsonPrimitive().getAsBigDecimal().stripTrailingZeros().scale() <= 0;
    }

    public static ArrayList<AbstractSchematicProperty> loadProperty(JsonObject root) {
        ArrayList<AbstractSchematicProperty> properties = new ArrayList<>();

        JsonArray types = root.getAsJsonArray("sublevel_types");
        for (JsonElement element : types) {
            String type = element.getAsString();
            switch (type) {
                case "enemy" -> {
                    EnemyProperty ep = loadEnemy(root);
                    if (ep != null) properties.add(ep);
                }
                case "ally" -> {
                    AllyProperty ap = loadAlly(root);
                    if (ap != null) properties.add(ap);
                }
                case "prefab" -> {
                    PrefabProperty pp = loadPrefab(root);
                    if (pp != null) properties.add(pp);
                }
            }
        }

        return properties;
    }

    public static WorldConfig loadWorldLevel(JsonObject root) {
        WorldConfig config = new WorldConfig();
        JsonArray levelsArray = root.getAsJsonArray("levels");

        for (JsonElement e : levelsArray) {
            config.getWorldLevel().add(e.getAsInt());
        }

        if (root.has("enemy_prefix")) {
            config.setEnemyPrefix("[" + root.get("enemy_prefix").getAsString() + "] ");
        }
        if (root.has("ally_prefix")) {
            config.setAllyPrefix("[" + root.get("ally_prefix").getAsString() + "] ");
        }

        return config;
    }

    private static EnemyProperty loadEnemy(JsonObject root) {
        JsonObject block = root.getAsJsonObject("enemy_property");
        EnemyProperty prop = GSON.fromJson(block, EnemyProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.enemy);
        return prop;
    }
    private static AllyProperty loadAlly(JsonObject root) {
        JsonObject block = root.getAsJsonObject("ally_property");
        AllyProperty prop = GSON.fromJson(block, AllyProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.ally);
        return prop;
    }
    private static PrefabProperty loadPrefab(JsonObject root) {
        JsonObject block = root.getAsJsonObject("prefab_property");
        PrefabProperty prop = GSON.fromJson(block, PrefabProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.prefab);
        return prop;
    }

    private static void fillCommon(JsonObject root, AbstractSchematicProperty prop, AbstractSchematicProperty.SublevelType type) {
        prop.setSublevelType(type);

        if (root.has("schematic_source")) {
            prop.setSchematicSource(AbstractSchematicProperty.SchematicSource.valueOf(root.get("schematic_source").getAsString()));
        }

        if (root.has("source_mod_id")) {
            String source = root.get("source_mod_id").getAsString();
            switch ( source ) {
                case "sable_schematic_api" -> prop.setSourceModId( AbstractSchematicProperty.SourceModId.sable_schematic_api );
                // 以后可能会加自动文件格式推断
            }
        }

        if (root.has("schematic_name")) {
            String name = root.get("schematic_name").getAsString();

            if (prop.getSchematicSource() == AbstractSchematicProperty.SchematicSource.folder) {
                if (prop.getSourceModId() == AbstractSchematicProperty.SourceModId.sable_schematic_api) {
                    prop.setSchematicPath(SchematicProvider.getSableSchematicApiFullPath(name));
                } else {
                    LOGGER.warn("folder 蓝图来源未获支持: {}", prop.getSourceModId());
                }
            } else {
                prop.setSchematicPath(name);
                prop.setSchematicResourceLocation(
                        ResourceLocation.fromNamespaceAndPath("sablespawner", "sablespawner/schematics/" + name)
                );
            }
        }

        if (root.has("sublevel_function")) {
            prop.setSublevelFunction(AbstractSchematicProperty.SublevelFunction.valueOf(root.get("sublevel_function").getAsString()));
        }
    }
}
