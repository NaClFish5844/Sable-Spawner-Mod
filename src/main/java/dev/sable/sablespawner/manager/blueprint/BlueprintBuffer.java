package dev.sable.sablespawner.manager.blueprint;


import dev.sable.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.manager.datapack.query.BlueprintQuery;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public class BlueprintBuffer {
    public static final BlueprintBuffer INSTANCE = new BlueprintBuffer();

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BUFFER = new Object2ObjectOpenHashMap<>();
    // 小的直接存对象 大的存路径或ResLoc
    // 需要在使用文档里提示“加载大东西可能相对较慢”
    // 以后是否能重写 使得巨型东西加载变快？？？

    public void append(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        BUFFER.putAll(entry);
    }
    public void append(BlueprintKey key, BlueprintEntry entry) {
        BUFFER.put(key, entry);
    }

    public void remove(BlueprintKey key) {
        BUFFER.remove(key);
    }
    public void remove(Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> entry) {
        if ( entry.isEmpty() ) { return; }
        for ( BlueprintKey e : entry.keySet() ) {
            BUFFER.remove(e);
        }
    }

    public void clearBuffer() {
        BUFFER.clear();
    }

    @Nullable public Pair<String, Object> getBlueprintObject(BlueprintEntry blueprintEntry) {
        // 根据entry直接确定蓝图
    }
    @Nullable public Pair<String, Object> getBlueprintObject(AbstractSchematicProperty property) {
        // 根据hash直接确定蓝图
    }



    public BlueprintQuery query() {
        return new BlueprintQuery( getBuffer() );
    }

    public Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getBuffer() { return BUFFER; }
}
