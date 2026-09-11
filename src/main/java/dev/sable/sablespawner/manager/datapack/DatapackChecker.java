package dev.sable.sablespawner.manager.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.AllyProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PrefabProperty;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

public final class DatapackChecker {
    private static final Gson GSON = DatapackLoader.getGSON();

    private static final Set<String> PROPERTY_TOP_LEVEL_KEYS = Set.of(
            "schematic_source", "source_mod_id", "schematic_name",
            "sublevel_types", "sublevel_function",
            "enemy_property", "ally_property", "prefab_property"
    );
    private static final Set<String> WORLDCONFIG_TOP_LEVEL_KEYS = Set.of(
            "dimension", "world_level",
            "enemy_prefix", "ally_prefix", "neutral_prefix",
            "spawn_pattern"
    );

    public static boolean checkMetaFormat(JsonObject object) {
        getLogger().info("正在数据包元信息文件");

        return DatapackChecker.isString("packname", object);
    }
    public static FileType interpretFileType(JsonObject object) {
        getLogger().info("正在尝试推断文件类型");
        int isProperty = 0;
        int isWorldConfig = 0;
        for ( String str : PROPERTY_TOP_LEVEL_KEYS ) { if ( object.has(str) ) isProperty++; }
        for ( String str : WORLDCONFIG_TOP_LEVEL_KEYS ) { if ( object.has(str) ) isWorldConfig++; }

        if ( isProperty == 0 && isWorldConfig == 0 ) {
            getLogger().info("推断失败");
            return FileType.invalid;
        }
        if ( isProperty >= isWorldConfig ) {
            getLogger().info("推断为蓝图属性文件");
            return FileType.property;
        }
        else {
            getLogger().info("推断为维度配置文件");
            return FileType.worldconfig;
        }
    }
    public static boolean checkDefaultWorldConfigFormat(JsonObject object) {
        getLogger().info("正在检查默认值配置文件");

        return
                DatapackChecker.checkNumberArrListSorted("levels", object ) &
                        DatapackChecker.isString("enemy_prefix", object) &
                        DatapackChecker.isString("ally_prefix", object) &
                        DatapackChecker.isString("neutral_prefix", object);

    }
    public static WorldConfigCheckResult checkWorldConfigFormat(JsonObject object) {
        getLogger().info("正在检查维度配置文件格式");

        WorldConfigCheckResult nullableFormat = checkWorldConfigNullableFormat(object);

        boolean isDimensionValid =
                checkDimensionKey("dimension", object);
        boolean isSpawnPatternValid =
                checkStringInEnum("spawn_pattern", object, WorldConfig.Pattern.class);

        return WorldConfigCheckResult.concat(
                nullableFormat,
                isDimensionValid,
                isSpawnPatternValid
        );
    }
    public static WorldConfigCheckResult checkWorldConfigNullableFormat(JsonObject object) {

        boolean isWorldLevelValid = true;
        boolean isEnemyPrefixValid = true;
        boolean isAllyPrefixValid = true;
        boolean isNeutralPrefixValid = true;

        if ( object.has("world_level") ) {
            isWorldLevelValid = checkNumberArrListSorted("world_level", object);
        }
        if ( object.has("enemy_prefix") ) {
            isEnemyPrefixValid = isString("enemy_prefix",object);
        }
        if ( object.has("ally_prefix") ) {
            isAllyPrefixValid = isString("ally_prefix",object);
        }
        if ( object.has("neutral_prefix") ) {
            isNeutralPrefixValid = isString("neutral_prefix",object);
        }

        return WorldConfigCheckResult.ofNullable(
                isWorldLevelValid,
                isEnemyPrefixValid,
                isAllyPrefixValid,
                isNeutralPrefixValid
        );

    }
    public static PropertyCheckResult checkPropertyFormat(JsonObject object) {
        getLogger().info("正在检查蓝图属性文件格式");

        boolean isTopLevelKeysValid =
                checkStringInEnum( "schematic_source", object, DatapackManager.BlueprintSourceFileLocation.class ) &
                        checkStringInEnum( "source_mod_id", object, DatapackManager.BlueprintSourceModId.class ) &
                        isString("schematic_name",object);

        boolean isTypeKeysValid =
                checkStringsInEnum("sublevel_types", object, AbstractSchematicProperty.SublevelType.class) &
                        checkStringInEnum("sublevel_function", object, AbstractSchematicProperty.SublevelFunction.class);

        JsonElement sublevelTypesElement = object.get("sublevel_types");
        if ( !isTypeKeysValid ) {
            getLogger().warn("[sublevel_types] 无效，将不生成具体属性");
            return new PropertyCheckResult(
                    isTopLevelKeysValid,
                    isTypeKeysValid,
                    false,
                    false,
                    false
            );
        }

        boolean isAllyPropertyValid = false;
        boolean isEnemyPropertyValid = false;
        boolean isPrefabPropertyValid = false;

        if ( extractStringArrList(sublevelTypesElement).contains("ally") ) {
            isAllyPropertyValid = checkPropertyBlock("ally_property", object.get("ally_property"), AllyProperty.class );
        }
        if ( extractStringArrList(sublevelTypesElement).contains("enemy") ) {
            isEnemyPropertyValid = checkPropertyBlock("enemy_property", object.get("enemy_property"), EnemyProperty.class );
        }
        if ( extractStringArrList(sublevelTypesElement).contains("prefab") ) {
            isPrefabPropertyValid = checkPropertyBlock("prefab_property", object.get("prefab_property"), PrefabProperty.class );
        }

        return new PropertyCheckResult(
                isTopLevelKeysValid,
                isTypeKeysValid,
                isAllyPropertyValid,
                isEnemyPropertyValid,
                isPrefabPropertyValid
        );
    }
    public static boolean checkPropertyBlock( String key, JsonElement element, Class<? extends AbstractSchematicProperty> propertyClass) {
        Set<String> propertyKeys = propertyKeyNames(propertyClass);

        if ( element == null ) { return false; }

        if (!element.isJsonObject()) {
            getLogger().warn("[{}] 不是对象", key );
            return false;
        }
        for (String field : element.getAsJsonObject().keySet()) {
            if (!propertyKeys.contains(field)) {
                getLogger().warn("[{}] {} 为未知字段，将被忽略", key, field);
            }
        }

        return true;
    }

    public static boolean checkNumberInRange( String key, JsonObject root, Number min, Number max ) {
        JsonElement element = root.get(key);

        if ( !isNumber( key, element ) ) { return false; }

        double value = element.getAsJsonPrimitive().getAsNumber().doubleValue();
        double upperBound = max.doubleValue();
        double lowerBound = min.doubleValue();

        if ( Double.isNaN(value) ) {
            getLogger().warn("[{}] 值为NaN", key);
            return false;
        }
        if ( value < lowerBound || value > upperBound ) {
            getLogger().warn("[{}] 超出范围", key);
            return false;
        }
        return true;
    }
    public static boolean checkStringInEnum( String key, JsonObject root, Class< ? extends Enum<?> > enumClass ) {
        JsonElement element = root.get(key);

        Set<String> valid = enumNamesIgnoreFlag(enumClass);
        if ( !isString( key, element ) ) { return false; }

        String value = element.getAsString();
        String validValues = String.join(", ", valid);

        if ( !valid.contains(value) ) {
            getLogger().warn("[{}] 名称非法，为{}，（合法值为{}）", key, value, validValues);
            return false;
        }

        return true;
    }
    public static boolean checkStringsInEnum( String key, JsonObject root, Class< ? extends Enum<?> > enumClass ) {
        JsonElement element = root.get(key);

        Set<String> valid = enumNamesIgnoreFlag(enumClass);
        if ( !isArrList( key, element ) ) { return false; }

        Set<String> values = extractStringArrList(element);
        String validValues = String.join(", ", valid);

        for ( String v : values ) {
            if ( !valid.contains(v) ) {
                getLogger().warn("[{}] 中发现非法值，为{}，（合法值为{}）", key, v, validValues);
                return false;
            }
        }

        return true;
    }
    public static boolean checkNumberArrListSorted( String key, JsonObject root ) {
        JsonElement element = root.get(key);

        if ( !isArrList( key, element ) ) { return false; }

        JsonArray array = element.getAsJsonArray();

        for (int i = 1; i < array.size(); i++) {
            JsonElement prev = array.get(i - 1);
            JsonElement cur = array.get(i);
            if ( !prev.isJsonPrimitive() || !prev.getAsJsonPrimitive().isNumber()
                    || !cur.isJsonPrimitive() || !cur.getAsJsonPrimitive().isNumber() ) { continue; }

            double p = prev.getAsDouble();
            double c = cur.getAsDouble();
            if ( c < p ) {
                getLogger().warn( "[{}] 发现列表未按升序排列", key );
                return false; }
            if ( c == p ) {
                getLogger().warn( "[{}] 发现列表存在重复值", key );
                return false;
            }
        }
        return true;
    }
    public static boolean checkDimensionKey( String key, JsonElement element ) {
        if ( !isString(key, element) ) { return false; }
        if ( ResourceLocation.tryParse( element.getAsString() ) == null ) {
            getLogger().warn("[{}] 不是合法的维度标识（应为 namespace:dim，如 deepspace:space）", key);
            return false;
        }
        return true;
    }

    public static ArrayList<Integer> getNoDuplicatedSortedArrList(JsonElement element ) {
        if ( element == null || !element.isJsonArray() ) { return new ArrayList<>(); }

        TreeSet<Integer> set = new TreeSet<>();
        for (JsonElement e : element.getAsJsonArray()) {
            if ( e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ) { set.add(e.getAsInt()); }
        }
        return new ArrayList<>(set);
    }
    public static Set<String> extractStringArrList(JsonElement element ) {
        if ( element == null || !element.isJsonArray() ) { return new HashSet<>(); }

        Set<String> set = new HashSet<>();
        for (JsonElement e : element.getAsJsonArray()) {
            if ( e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() ) { set.add(e.getAsString()); }
        }
        return set;
    }

    public static boolean isNumber( String key, JsonElement element ) {
        if (
                element == null ||
                        ! element.isJsonPrimitive() ||
                        ! element.getAsJsonPrimitive().isNumber()
        ) {
            getLogger().warn("[{}] 缺失或不是数字", key);
            return false;
        }
        return true;
    }
    public static boolean isInteger( String key, JsonElement element ) {
        if ( !isNumber( key, element ) ) { return false; }
        if ( element.getAsJsonPrimitive().getAsBigDecimal().stripTrailingZeros().scale() > 0 ) {
            getLogger().warn("[{}] 不是整数", key);
        }
        return true;
    }
    public static boolean isString( String key, JsonElement element ) {
        if (
                element == null ||
                        ! element.isJsonPrimitive() ||
                        ! element.getAsJsonPrimitive().isString()
        ) {
            getLogger().warn("[{}] 缺失或不是字符串", key);
            return false;
        }
        return true;
    }
    public static boolean isArrList( String key, JsonElement element ) {
        if (
                element == null ||
                        ! element.isJsonArray()
        ) {
            getLogger().warn("[{}] 缺失或不是列表", key);
            return false;
        }
        return true;
    }
    public static boolean isBoolean( String key, JsonElement element ) {
        if (
                element == null ||
                        ! element.isJsonPrimitive() ||
                        ! element.getAsJsonPrimitive().isBoolean()
        ) {
            getLogger().warn("[{}] 缺失或不是布尔值", key);
            return false;
        }
        return true;
    }

    public static boolean isNumber( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isNumber(key,element);
    }
    public static boolean isInteger( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isInteger(key,element);
    }
    public static boolean isString( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isString(key,element);
    }
    public static boolean isArrList( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isArrList(key,element);
    }
    public static boolean isBoolean( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isBoolean(key,element);
    }

    private static Set<String> enumNames(Class<? extends Enum<?>> enumClass) {
        Set<String> names = new HashSet<>();
        for (Enum<?> constant : enumClass.getEnumConstants()) { names.add(constant.name()); }
        return names;
    }
    private static Set<String> enumNamesIgnoreFlag(Class<? extends Enum<?>> enumClass) {
        Set<String> names = enumNames(enumClass);
        names.remove("invalid");
        names.remove("auto");

        return names;
    }
    private static Set<String> propertyKeyNames( Class<? extends AbstractSchematicProperty> propertyClass ) {
        Set<String> keys = new HashSet<>();

        for ( Field field : propertyClass.getDeclaredFields() ) {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            keys.add(
                    serializedName != null ?
                            serializedName.value() :
                            GSON.fieldNamingStrategy().translateName(field)
            );
        }

        return keys;
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }

    public enum FileType {
        property,
        worldconfig,
        invalid
    }

    public record PropertyCheckResult(
            boolean isTopLevelKeysValid,
            boolean isTypeKeysValid,
            boolean isAllyPropertyValid,
            boolean isEnemyPropertyValid,
            boolean isPrefabPropertyValid
    ) {
        public boolean isAllFalse() {
            return !( isTopLevelKeysValid || isTypeKeysValid || isAllyPropertyValid || isEnemyPropertyValid || isPrefabPropertyValid );
        }
    }
    public record WorldConfigCheckResult(
            boolean isWorldLevelValid,
            boolean isEnemyPrefixValid,
            boolean isAllyPrefixValid,
            boolean isNeutralPrefixValid,
            boolean isDimensionValid,
            boolean isSpawnPatternValid
    ) {
        private static WorldConfigCheckResult ofNullable(
                boolean isWorldLevelValid,
                boolean isEnemyPrefixValid,
                boolean isAllyPrefixValid,
                boolean isNeutralPrefixValid
        ) {
            return new WorldConfigCheckResult(
                    isWorldLevelValid,
                    isEnemyPrefixValid,
                    isAllyPrefixValid,
                    isNeutralPrefixValid,
                    false,
                    false
            );
        }
        private static WorldConfigCheckResult concat(
                WorldConfigCheckResult nullableResult,
                boolean isDimensionValid,
                boolean isSpawnPatternValid
        ) {
            return new WorldConfigCheckResult(
                    nullableResult.isWorldLevelValid,
                    nullableResult.isEnemyPrefixValid,
                    nullableResult.isAllyPrefixValid,
                    nullableResult.isNeutralPrefixValid,
                    isDimensionValid,
                    isSpawnPatternValid
            );
        }
        public boolean isAllFalse() {
            return !( isWorldLevelValid || isEnemyPrefixValid || isAllyPrefixValid || isNeutralPrefixValid || isDimensionValid || isSpawnPatternValid );
        }
    }
}
