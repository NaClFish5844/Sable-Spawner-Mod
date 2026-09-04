package dev.sable.sablespawner.datapack;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.blueprint.BlueprintEntry;
import dev.sable.sablespawner.datapack.blueprint.BlueprintProvider;
import dev.sable.sablespawner.datapack.blueprint.BlueprintKey;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.datapack.property.sublevel.*;
import dev.sable.sablespawner.datapack.blueprint.BlueprintBuffer;
import dev.sable.sablespawner.datapack.query.PropertyQuery;
import dev.sable.sablespawner.datapack.query.WorldConfigQuery;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static dev.sable.sablespawner.SableSpawnerConfig.BLUEPRINT_CACHE_MAX_BLOCKS;


@Getter @Setter
public class DatapackManager {
    public static final DatapackManager INSTANCE = new DatapackManager();

    protected enum PackFormat {
        directory,
        zip,
        invalid;

        protected static PackFormat of(Path path) {
            if ( Files.isDirectory(path) ) { return directory; }
            if ( Files.isRegularFile(path) && path.toString().endsWith(".zip") ) { return zip; }
            return invalid;
        }
    }

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

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BLUEPRINT_BUFFER = getBlueprintBuffer().getBuffer();
    private final Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> PROPERTY_MANAGER = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, DefaultConfig> WORLDCONFIG_MANAGER = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectOpenHashMap<String, PackFormat> PackFormatRegistry = new Object2ObjectOpenHashMap<>();

    public void loadDatapack() {
        PROPERTY_MANAGER.clear();
        WORLDCONFIG_MANAGER.clear();

        DefaultConfig defaultConfig = DatapackLoader.parseDefaultWorldConfig();
        WORLDCONFIG_MANAGER.put("default", defaultConfig);

        Set<Path> datapackDirs = DatapackScanner.scanDatapacks();
        if ( datapackDirs.isEmpty() ) {
            getLogger().info("未检测到数据包");
            return;
        }

        if ( datapackDirs.size()<=10 ) { getLogger().info( "发现 {} 个数据包：{}", datapackDirs.size(), datapackDirs ); }
        else { getLogger().info( "发现 {} 个数据包", datapackDirs.size()); }

        for ( Path datapack : datapackDirs ) {
            String packName = datapack.getFileName().toString();
            PackFormatRegistry.put(packName, PackFormat.of(datapack));

            Path meta = DatapackScanner.getMetaJsonOfPack(datapack);
            if ( meta == null ) { return; }

            Set<Path> properties = DatapackScanner.scanPropertyOfPack(datapack);
            Set<Path> worldConfigs = DatapackScanner.scanWorldConfigOfPack(datapack);

            for ( Path property : properties ) {
                String key = packName + "/" + stripExtension( property.getFileName().toString() );

                for ( AbstractSchematicProperty p : DatapackLoader.parseProperty(property, packName) ) {
                    PROPERTY_MANAGER.put( PropertyKey.of(key, p.getSublevelType()), p );
                }
            }
            for ( Path config : worldConfigs ) {
                WorldConfig worldConfig = DatapackLoader.parseWorldConfig(config, defaultConfig);
                if ( worldConfig != null ) { WORLDCONFIG_MANAGER.put(worldConfig.getDimension(), worldConfig); }
            }

        }

    }
    public void loadBlueprints() {
        BLUEPRINT_BUFFER.clear(); //更新考虑做成增量式的？

        Set<Path> datapackDirs = DatapackScanner.scanDatapacks();
        if ( datapackDirs.isEmpty() ) { return; }
        Set<Path> blueprints = new HashSet<>();

        for ( Path datapack : datapackDirs ) {
            blueprints.addAll(
                    DatapackScanner.scanBlueprintOfPack(datapack)
            );
        }

        for ( BlueprintSourceModId modId : BlueprintSourceModId.values() ) {
            if ( modId == BlueprintSourceModId.invalid || modId == BlueprintSourceModId.auto ) { continue; }
            if ( !getModList().isLoaded( modId.toString() ) ) { continue; }

            Set<Path> bp = BlueprintProvider.scanFolderBlueprints(modId);

            if ( !bp.isEmpty() ) {
                getLogger().info("发现 [{}] 的蓝图", modId);
                blueprints.addAll(bp);
            }
        }

        for ( AbstractSchematicProperty property : PROPERTY_MANAGER.values() ) {
            int maxBlocks = BLUEPRINT_CACHE_MAX_BLOCKS.getAsInt();

            String packName = property.getPackName();
            String bp = property.getSchematicPath();
            BlueprintSourceModId sourceModId = property.getSourceModId();

            Object blueprintObject = null;
            BlueprintKey blueprintKey;
            BlueprintEntry blueprintEntry;

            switch ( PackFormatRegistry.get(packName) ) {
                case directory -> {
                    // 尝试加载为对应的obj (BlueprintProvider.getBlueprintObject)
                }
                case zip -> {
                    // 打开zip
                    // 尝试加载为对应的obj (BlueprintProvider.getBlueprintObject)
                }
                default -> {
                    continue;
                }
            }
            if ( blueprintObject == null ) { continue; }

            switch ( sourceModId ) {
                case sable_schematic_api -> {
                    if ( blueprintObject instanceof SableBlueprint sableBp ) {
                        // 检测蓝图大小，决定是否进入缓存
                    }
                }
                default -> {
                    if ( blueprintObject instanceof Path ) { blueprintEntry = new BlueprintEntry( blueprintObject ); }
                }
            }



            // 按prop从set里pop蓝图 并且加入缓存器
        }




        // 把剩下的蓝图归拢 自动生成bpK

    }

    public PropertyQuery propertyQuery() {
        return new PropertyQuery(PROPERTY_MANAGER);
    }
    public WorldConfigQuery worldConfigQuery() {
        return new WorldConfigQuery(WORLDCONFIG_MANAGER);
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static ResourceManager getResourceManager() {
        return SableSpawner.RESOURCE_MANAGER;
    }
    private static BlueprintBuffer getBlueprintBuffer() {
        return BlueprintBuffer.INSTANCE;
    }
    private static ModList getModList() {
        return ModList.get();
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

}
