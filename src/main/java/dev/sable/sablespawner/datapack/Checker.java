package dev.sable.sablespawner.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.sublevel.AllyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.PrefabProperty;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Checker {
    protected record CheckResult(
            boolean isTopLevelKeysValid,
            boolean isTypeKeysValid,
            boolean isAllyPropertyValid,
            boolean isEnemyPropertyValid,
            boolean isPrefabPropertyValid
    ) {}

    private static final Gson GSON = LoadDatapack.GSON;

    protected static CheckResult checkPropertyFormat(JsonObject object) {
        getLogger().info("正在检查文件格式");

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
            return new CheckResult(
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

        return new CheckResult(
                isTopLevelKeysValid,
                isTypeKeysValid,
                isAllyPropertyValid,
                isEnemyPropertyValid,
                isPrefabPropertyValid
        );
    }
    protected static CheckResult checkWorldConfigFormat(JsonObject object) {
        getLogger().info("正在检查文件格式");
        return new CheckResult(false,false,false,false,false);
    }
    protected static boolean checkPropertyBlock( String key, JsonElement element, Class<? extends AbstractSchematicProperty> propertyClass) {
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

    protected static boolean checkNumberInRange( String key, JsonElement element, Number min, Number max ) {
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
    protected static boolean checkStringInEnum( String key, JsonElement element, Class< ? extends Enum<?> > enumClass ) {
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
    protected static boolean checkStringsInEnum( String key, JsonElement element, Class< ? extends Enum<?> > enumClass ) {
        // 输入jsonArrList
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
    protected static boolean checkNumberArrListSorted( String key, JsonElement element ) {
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

    protected static boolean checkNumberInRange( String key, JsonObject root, Number min, Number max ) {
        JsonElement element = root.get(key);
        return checkNumberInRange(key,element,min,max);
    }
    protected static boolean checkStringInEnum( String key, JsonObject root, Class< ? extends Enum<?> > enumClass ) {
        JsonElement element = root.get(key);
        return checkStringInEnum(key,element,enumClass);
    }
    protected static boolean checkNumberArrListSorted( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return checkNumberArrListSorted(key,element);
    }

    protected static ArrayList<Integer> getNoDuplicatedSortedArrList(JsonElement element ) {
        if ( element == null || !element.isJsonArray() ) { return new ArrayList<>(); }

        TreeSet<Integer> set = new TreeSet<>();
        for (JsonElement e : element.getAsJsonArray()) {
            if ( e.isJsonPrimitive() && e.getAsJsonPrimitive().isNumber() ) { set.add(e.getAsInt()); }
        }
        return new ArrayList<>(set);
    }
    protected static Set<String> extractStringArrList(JsonElement element ) {
        if ( element == null || !element.isJsonArray() ) { return new HashSet<>(); }

        Set<String> set = new HashSet<>();
        for (JsonElement e : element.getAsJsonArray()) {
            if ( e.isJsonPrimitive() && e.getAsJsonPrimitive().isString() ) { set.add(e.getAsString()); }
        }
        return set;
    }

    protected static boolean isNumber(String key, JsonElement element ) {
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
    protected static boolean isInteger(String key, JsonElement element ) {
        if ( !isNumber( key, element ) ) { return false; }
        if ( element.getAsJsonPrimitive().getAsBigDecimal().stripTrailingZeros().scale() > 0 ) {
            getLogger().warn("[{}] 不是整数", key);
        }
        return true;
    }
    protected static boolean isString(String key, JsonElement element ) {
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
    protected static boolean isArrList( String key, JsonElement element ) {
        if (
                element == null ||
                        ! element.isJsonArray()
        ) {
            getLogger().warn("[{}] 缺失或不是列表", key);
            return false;
        }
        return true;
    }
    protected static boolean isBoolean( String key, JsonElement element ) {
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

    protected static boolean isNumber(String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isNumber(key,element);
    }
    protected static boolean isInteger(String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isInteger(key,element);
    }
    protected static boolean isString(String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isString(key,element);
    }
    protected static boolean isArrList( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isArrList(key,element);
    }
    protected static boolean isBoolean( String key, JsonObject root ) {
        JsonElement element = root.get(key);
        return isBoolean(key,element);
    }

    protected static Set<String> enumNames(Class<? extends Enum<?>> enumClass) {
        Set<String> names = new HashSet<>();
        for (Enum<?> constant : enumClass.getEnumConstants()) { names.add(constant.name()); }
        return names;
    }
    protected static Set<String> enumNamesIgnoreFlag(Class<? extends Enum<?>> enumClass) {
        Set<String> names = enumNames(enumClass);
        names.remove("invalid");
        names.remove("auto");

        return names;
    }
    protected static Set<String> propertyKeyNames( Class<? extends AbstractSchematicProperty> propertyClass ) {
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
}
