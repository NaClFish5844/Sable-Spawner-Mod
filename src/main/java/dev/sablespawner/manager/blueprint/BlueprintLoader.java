package dev.sablespawner.manager.blueprint;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.DatapackManager;
import dev.sablespawner.manager.datapack.DatapackSource;
import dev.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sablespawner.util.FileIOUtil;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static dev.sablespawner.SableSpawnerConfig.BLUEPRINT_CACHE_MAX_BLOCKS;

public final class BlueprintLoader {

    public static Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> loadBlueprintRef() {
        Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> reg = new Object2ObjectOpenHashMap<>();

        if ( getDatapackRegistry().isEmpty() ) {
            getLogger().info("未发现数据包，跳过数据包蓝图加载 | No datapack found, skipping datapack blueprint loading");
        } else {
            for ( DatapackSource datapack : getDatapackRegistry() ) {
                Set<BlueprintEntry> entries = BlueprintScanner.scanBlueprintsOfPack(datapack);

                if ( entries.isEmpty() ) { continue; }
                for ( BlueprintEntry entry : entries ) {
                    BlueprintKey key = BlueprintKey.ofEntry(entry);

                    reg.put(key, entry);
                }
            }
        }

        Set<BlueprintEntry> files = BlueprintScanner.scanBlueprintsOfFolder();

        if ( files.isEmpty() ) {
            getLogger().info("文件夹中未发现蓝图 | No blueprint found in folder");
        } else {
            for ( BlueprintEntry entry : files ) {
                BlueprintKey key = BlueprintKey.ofEntry(entry);

                reg.put(key, entry);
            }
        }

        return reg;

    }
    public static Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> loadPropertyBlueprintMap() {
        Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> map = new Object2ObjectOpenHashMap<>();

        Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> properties = getPropertyManager();
        if ( properties.isEmpty() ) { return map; }

        for ( Map.Entry<PropertyKey, AbstractSchematicProperty> e : properties.entrySet() ) {
            BlueprintKey matched = findKeyOfProperty( e.getValue() );

            if ( matched == null ) {
                getLogger().warn("未找到属性 [{}] 对应的蓝图：{} | No blueprint found for property [{}]: {}",
                        e.getKey(), e.getValue().getSchematicName(), e.getKey(), e.getValue().getSchematicName());
                continue;
            }
            map.put( e.getKey(), matched );
        }
        return map;
    }
    public static Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> loadBuffer() {
        Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> buffer = new Object2ObjectOpenHashMap<>();

        Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> registry = getBlueprintRegistry();
        Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> propertyMap = getPropertyBlueprintMap();
        if ( registry.isEmpty() || propertyMap.isEmpty() ) { return buffer; }

        int maxBlocks = BLUEPRINT_CACHE_MAX_BLOCKS.getAsInt();

        for ( BlueprintKey key : propertyMap.values() ) {
            if ( buffer.containsKey(key) ) { continue; }

            BlueprintEntry entry = registry.get(key);
            if ( entry == null ) { continue; }

            Pair<Class<?>, Object> result = loadBlueprintEntryAsObject(entry);
            if ( result == null ) {
                getLogger().warn("蓝图加载失败：{} | Failed to load blueprint: {}", key.name(), key.name());
                continue;
            }

            Object object = result.right();
            if ( object instanceof SableBlueprint blueprint && blueprint.blockCount() >= maxBlocks ) {
                continue;
            }

            buffer.put( key, BlueprintEntry.updateObject(entry, object) );
        }
        return buffer;
    }

    @Nullable public static Pair<Class<?>, Object> loadBlueprintEntryAsObject(BlueprintEntry entry) {
        if ( entry == null || entry.sourceMod() == null ) { return null; }

        return switch ( entry.sourceMod() ) {
            case sable_schematic_api -> loadSableBlueprint(entry);
            default -> null;
        };
    }
    @SuppressWarnings("DataFlowIssue") @Nullable private static Pair<Class<?>, Object> loadSableBlueprint(BlueprintEntry entry) {
        if ( !getModList().isLoaded("sable_schematic_api") ) {
            getLogger().error("缺失 modid：sable_schematic_api，蓝图加载已取消 | Missing mod id: sable_schematic_api, blueprint loading cancelled");
            return null;
        }
        CompoundTag tag;

        if ( entry.hasPathReference() ) {
            tag = FileIOUtil.readFile( Path.of(entry.path()), FileIOUtil::readAsNbt );
        } else if ( entry.hasDatapackPathReference() ) {
            tag = FileIOUtil.readDatapackFile( entry.datapackSource(), entry.path(), FileIOUtil::readAsNbt );
        } else {
            return null;
        }
        if ( tag == null ) { return null; }

        SableBlueprint blueprint = SableBlueprint.load(tag);
        if ( blueprint == null ) { return null; }

        return Pair.of( SableBlueprint.class, blueprint );
    }

    @Nullable private static BlueprintKey findKeyOfProperty(AbstractSchematicProperty property) {
        String name = property.getSchematicName();
        boolean isFolder = property.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.folder;

        for ( Map.Entry<BlueprintKey, BlueprintEntry> e : getBlueprintRegistry().entrySet() ) {
            BlueprintKey key = e.getKey();
            BlueprintEntry entry = e.getValue();

            if ( !Objects.equals( key.name(), name ) ) { continue; }

            if ( isFolder ) {
                if ( entry.hasPathReference() ) { return key; }
                continue;
            }

            DatapackSource datapack = entry.datapackSource();
            if ( datapack == null ) { continue; }
            if ( !Objects.equals( datapack.getPackMeta().packName(), property.getPackName() ) ) { continue; }

            return key;
        }
        return null;
    }

    private static ModList getModList() {
        return ModList.get();
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static ObjectSet<DatapackSource> getDatapackRegistry() {
        return SableSpawner.DATAPACK_MANAGER.getDATAPACK_REGISTRY();
    }
    private static Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> getPropertyManager() {
        return SableSpawner.DATAPACK_MANAGER.getPROPERTY_MANAGER();
    }
    private static Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getBlueprintRegistry() {
        return SableSpawner.BLUEPRINT_MANAGER.getBLUEPRINT_REGISTRY();
    }
    private static Object2ObjectOpenHashMap<PropertyKey, BlueprintKey> getPropertyBlueprintMap() {
        return SableSpawner.BLUEPRINT_MANAGER.getPROPERTY_BLUEPRINT_MAP();
    }


}
