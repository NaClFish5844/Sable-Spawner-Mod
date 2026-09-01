package dev.sable.sablespawner.datapack.blueprint;


import dev.sable.sablespawner.datapack.query.BlueprintQuery;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BlueprintManager {
    public static final BlueprintManager INSTANCE = new BlueprintManager();

    private static final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BUFFER = new Object2ObjectOpenHashMap<>();
    // 小的直接存对象 大的存路径或ResLoc
    // 需要在使用文档里提示“加载大东西可能相对较慢”
    // 以后是否能重写 使得巨型东西加载变快？？？

    public static void append(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        BUFFER.putAll(entry);
    }
    public static void append(BlueprintKey key, BlueprintEntry entry) {
        BUFFER.put(key, entry);
    }

    public static void remove(BlueprintKey key) {
        BUFFER.remove(key);
    }
    public static void remove(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        if ( entry.isEmpty() ) { return; }
        for ( BlueprintKey e : entry.keySet() ) {
            BUFFER.remove(e);
        }
    }


    public static void clearBuffer() {
        BUFFER.clear();
    }

    public static void loadBlueprints() {

    }

    public static BlueprintQuery query() {
        return new BlueprintQuery( getBuffer() );
    }

    public static Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getBuffer() { return BUFFER; }
}
