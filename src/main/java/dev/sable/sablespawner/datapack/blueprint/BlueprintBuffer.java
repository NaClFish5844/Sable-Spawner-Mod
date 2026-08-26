package dev.sable.sablespawner.datapack.blueprint;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BlueprintBuffer {
    public static final BlueprintBuffer INSTANCE = new BlueprintBuffer();

    private static final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> CONTAINER = new Object2ObjectOpenHashMap<>();
    // 小的直接存对象 大的存路径或ResLoc
    // 需要在使用文档里提示“加载大东西可能相对较慢”
    // 以后是否能重写 使得巨型东西加载变快？？？

    public static void append(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        CONTAINER.putAll(entry);
    }
    public static void append(BlueprintKey key, BlueprintEntry entry) {
        CONTAINER.put(key, entry);
    }

    public static void remove(BlueprintKey key) {
        CONTAINER.remove(key);
    }
    public static void remove(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        if ( entry.isEmpty() ) { return; }
        for ( BlueprintKey e : entry.keySet() ) {
            CONTAINER.remove(e);
        }
    }


    public static void clearBuffer() {
        CONTAINER.clear();
    }

    public static Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getContainer() { return CONTAINER; }


}
