package dev.sable.sablespawner.manager.datapack;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.manager.datapack.property.sublevel.*;
import dev.sable.sablespawner.manager.datapack.query.PropertyQuery;
import dev.sable.sablespawner.manager.datapack.query.WorldConfigQuery;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.Setter;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;


@Getter @Setter
public class DatapackManager {
    public static final DatapackManager INSTANCE = new DatapackManager();

    public enum BlueprintSourceFileLocation {
        datapack,
        folder,
        invalid
    }
    public enum BlueprintSourceModId {
        sable_schematic_api,
        invalid
    }

    private final ObjectSet<DatapackSource> DATAPACK_REGISTRY = new ObjectArraySet<>();
    private final Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> PROPERTY_MANAGER = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, DefaultConfig> WORLDCONFIG_MANAGER = new Object2ObjectOpenHashMap<>();
    @Nullable private DefaultConfig DEFAULT_CONFIG = null;

    public void reloadDatapack() {
        DATAPACK_REGISTRY.clear();
        PROPERTY_MANAGER.clear();
        WORLDCONFIG_MANAGER.clear();

        DEFAULT_CONFIG = DatapackLoader.loadDefaultConfig();

        DATAPACK_REGISTRY.addAll( DatapackLoader.loadDatapackSources() );
        int datapackAmount = DATAPACK_REGISTRY.size();

        if ( DATAPACK_REGISTRY.isEmpty() ) {
            getLogger().info("未检测到数据包");
            return;
        }

        if ( datapackAmount <= 10 ) { getLogger().info( "发现 {} 个数据包：{}", datapackAmount, DATAPACK_REGISTRY); }
        else { getLogger().info( "发现 {} 个数据包", datapackAmount ); }

        for ( DatapackSource datapack : DATAPACK_REGISTRY) {
            Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> properties = DatapackLoader.loadProperties(datapack);
            Object2ObjectOpenHashMap<String, DefaultConfig> worldConfigs = DatapackLoader.loadWorldConfigs(datapack, DEFAULT_CONFIG);

            PROPERTY_MANAGER.putAll(properties);
            WORLDCONFIG_MANAGER.putAll(worldConfigs);
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
    private static ModList getModList() {
        return ModList.get();
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }


}
