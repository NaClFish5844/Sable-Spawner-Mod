package dev.sable.sablespawner.manager.blueprint;


import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.slf4j.Logger;

@Getter
public class BlueprintManager {
    public static final BlueprintManager INSTANCE = new BlueprintManager();

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BLUEPRINT_REGISTRY = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> PROPERTY_BLUEPRINT_MAP = new Object2ObjectOpenHashMap<>();
    // 小的直接存对象 大的存引用
    // 需要在使用文档里提示“加载大东西可能相对较慢”
    // 以后是否能重写 使得巨型东西加载变快？？？


    public void reloadBlueprint() {
        BLUEPRINT_REGISTRY.clear();
        PROPERTY_BLUEPRINT_MAP.clear();

        BLUEPRINT_REGISTRY.putAll(BlueprintLoader.loadBlueprintRef());
        if ( BLUEPRINT_REGISTRY.isEmpty() ) {
            getLogger().info("未扫描到任何蓝图");
            return;
        } else {
            getLogger().info("扫描到 {} 个蓝图", BLUEPRINT_REGISTRY.size());
        }

        PROPERTY_BLUEPRINT_MAP.putAll(BlueprintLoader.loadPropertyBlueprintMap());
        if ( PROPERTY_BLUEPRINT_MAP.isEmpty() ) {
            getLogger().info("未发现任何蓝图属性与蓝图的映射");
            return;
        } else {
            getLogger().info("发现 {} 个蓝图映射", PROPERTY_BLUEPRINT_MAP.size());
        }

        Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> bufferSlice = BlueprintLoader.loadBuffer();
        if ( bufferSlice.isEmpty() ) {
            getLogger().info("未向缓存载入蓝图");
        } else {
            getLogger().info("向内存中缓存了 {} 个蓝图", bufferSlice.size());

            BLUEPRINT_REGISTRY.putAll(bufferSlice);
        }

    }


    public BlueprintQuery query() {
        return new BlueprintQuery( BLUEPRINT_REGISTRY, PROPERTY_BLUEPRINT_MAP );
    }


    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
}
