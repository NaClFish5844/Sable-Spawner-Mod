package dev.sable.sablespawner.datapack;

import com.google.gson.JsonElement;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.blueprint.BlueprintEntry;
import dev.sable.sablespawner.datapack.blueprint.BlueprintProvider;
import dev.sable.sablespawner.datapack.blueprint.PropertyKey;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.blueprint.BlueprintManager;
import dev.sable.sablespawner.datapack.query.PropertyQuery;
import dev.sable.sablespawner.datapack.query.WorldConfigQuery;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;


@Getter @Setter
public class DatapackManager {
    public static final DatapackManager INSTANCE = new DatapackManager();

    public enum BlueprintSourceFileLocation {
        datapack,
        folder,
        auto,
        invalid
    }
    public enum BlueprintSourceModId {
        sable_schematic_api,
        auto,
        invalid
    }

    public static final BlueprintManager BLUEPRINT_MANAGER = getBlueprintManager();
    private Object2ObjectOpenHashMap<PropertyKey, BlueprintEntry> BLUEPRINT_BUFFER = getBlueprintBuffer();
    private Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> PROPERTY_MANAGER = new Object2ObjectOpenHashMap<>();
    private Object2ObjectOpenHashMap<String, DefaultConfig> WORLDCONFIG_MANAGER = new Object2ObjectOpenHashMap<>();

    public void loadDatapack( Map<ResourceLocation, JsonElement> datapackFiles ) {
        BLUEPRINT_BUFFER.clear();
        PROPERTY_MANAGER.clear();
        WORLDCONFIG_MANAGER.clear();
        //更新考虑做成增量式的？

        for ( Map.Entry<ResourceLocation, JsonElement> fileEntry : datapackFiles.entrySet() ) {
            String key = fileEntry.getKey().toString();
            JsonElement jsonElement = fileEntry.getValue();
            getLogger().info("正在加载文件：{}", key);

            if ( key.startsWith("sablespawner:properties") ) {
                Object2ObjectMap.Entry<PropertyKey, AbstractSchematicProperty> entry = LoadDatapack.parseProperty( jsonElement );
                if ( entry == null ) {
                    getLogger().warn("文件加载失败：{}", key);
                    continue;
                }
                PROPERTY_MANAGER.put( entry.getKey(), entry.getValue() );
            }
            else if ( key.startsWith("sablespawner:worldconfig") ) {
                Object2ObjectMap.Entry<String, DefaultConfig> entry = LoadDatapack.parseWorldConfig( jsonElement );
                if ( entry == null ) {
                    getLogger().warn("文件加载失败：{}", key);
                    continue;
                }
                WORLDCONFIG_MANAGER.put( entry.getKey(), entry.getValue() );
            }
            else { getLogger().warn("发现未知文件：{}", key); }
        }
    }

    public static void loadBlueprints() {
        // 筛选进缓存的
        ObjectList<PropertyKey> fromDatapack = new ObjectArrayList<>();
        ObjectList<PropertyKey> fromFolder = new ObjectArrayList<>();

        Map<ResourceLocation, Resource> datapackBlueprintFiles =
                getResourceManager().listResources(
                        "sablespawner/schematics",
                        f -> f.toString().endsWith(".nbt")
                );
        ObjectList<Path> folderBlueprintFiles = new ObjectArrayList<>();

        // 还需要从PROPERTY_MANAGER里查有效的prop
        // 以节省时间
        for ( BlueprintSourceModId modId : BlueprintSourceModId.values() ) {
            if ( modId == BlueprintSourceModId.invalid || modId == BlueprintSourceModId.auto ) { continue; }

            Set<Path> paths = BlueprintProvider.readFolderBlueprints( modId );
            if ( paths == null ) { continue; }

            folderBlueprintFiles.addAll(paths);
        }


        if ( getModList().isLoaded("sable_schematic_api") ) {
            Set<Path> folderBlueprints =  BlueprintProvider.readFolderBlueprints( DatapackManager.BlueprintSourceModId.sable_schematic_api );

        }


    }

    public PropertyQuery propertyQuery() {
        return new PropertyQuery(PROPERTY_MANAGER);
    }
    public WorldConfigQuery worldConfigQuery() {
        return new WorldConfigQuery(WORLDCONFIG_MANAGER);
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static BlueprintManager getBlueprintManager() {
        return BlueprintManager.INSTANCE;
    }
    private static ResourceManager getResourceManager() {
        return SableSpawner.RESOURCE_MANAGER;
    }
    private static Object2ObjectOpenHashMap<PropertyKey, BlueprintEntry> getBlueprintBuffer() {
        return BlueprintManager.getBuffer();
    }
    private static ModList getModList() {
        return ModList.get();
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

}
