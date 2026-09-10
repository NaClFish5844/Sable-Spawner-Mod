package dev.sable.sablespawner.manager.datapack;

import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.blueprint.BlueprintEntry;
import dev.sable.sablespawner.manager.blueprint.BlueprintProvider;
import dev.sable.sablespawner.manager.blueprint.BlueprintKey;
import dev.sable.sablespawner.manager.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.manager.datapack.property.sublevel.*;
import dev.sable.sablespawner.manager.blueprint.BlueprintBuffer;
import dev.sable.sablespawner.manager.datapack.query.PropertyQuery;
import dev.sable.sablespawner.manager.datapack.query.WorldConfigQuery;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static dev.sable.sablespawner.SableSpawnerConfig.BLUEPRINT_CACHE_MAX_BLOCKS;
import static dev.sable.sablespawner.manager.datapack.DatapackManager.BlueprintSourceModId.invalid;
import static dev.sable.sablespawner.manager.datapack.DatapackManager.BlueprintSourceModId.sable_schematic_api;


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

    @Getter private final Object2ObjectOpenHashMap<String, Path> PACK_REGISTRY = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<PropertyKey, AbstractSchematicProperty> PROPERTY_MANAGER = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<String, DefaultConfig> WORLDCONFIG_MANAGER = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BLUEPRINT_BUFFER = getBlueprintBuffer().getBuffer();

    public void reloadAll() {
        reloadDatapack();
        reloadBlueprint();
    }

    public void reloadDatapack() {
        PACK_REGISTRY.clear();
        PROPERTY_MANAGER.clear();
        WORLDCONFIG_MANAGER.clear();

        DefaultConfig defaultConfig = DatapackLoader.parseDefaultWorldConfig();
        WORLDCONFIG_MANAGER.put("default", defaultConfig);

        Set<Path> datapackRoots = DatapackScanner.scanDatapackRoots();
        if ( datapackRoots.isEmpty() ) {
            getLogger().info("未检测到数据包");
            return;
        }


        if ( datapackRoots.size()<=10 ) { getLogger().info( "发现 {} 个数据包：{}", datapackRoots.size(), datapackRoots ); }
        else { getLogger().info( "发现 {} 个数据包", datapackRoots.size()); }

        for ( Path root : datapackRoots ) {
            Path validRoot = DatapackScanner.scanRelativeValidRoot(root);
            if ( validRoot == null ) { continue; }
            String packName = DatapackScanner.getPackNameOfValidRoot(validRoot);

            PACK_REGISTRY.put(packName, root);

            Path meta = DatapackScanner.readPackMeta(validRoot);
            if ( meta == null ) { continue; }

            Set<Path> properties = DatapackScanner.scanPropertiesOfPack(validRoot);
            Set<Path> worldConfigs = DatapackScanner.scanWorldConfigsOfPack(validRoot);

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
    public void reloadBlueprint() {
        BLUEPRINT_BUFFER.clear();

        // 扫描所有位置的蓝图
        Set<Path> blueprints = new HashSet<>();

        for ( Path root : PACK_REGISTRY.values() ) {
            Path validRoot = DatapackScanner.scanRelativeValidRoot(root);
            if ( validRoot == null ) { continue; }

            blueprints.addAll( DatapackScanner.scanBlueprintsOfPack(validRoot) );
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

        // 需要把hash也捅进property

        // 筛选进入缓存
        for ( AbstractSchematicProperty property : PROPERTY_MANAGER.values() ) {
            int maxBlocks = BLUEPRINT_CACHE_MAX_BLOCKS.getAsInt();

            String packName = property.getPackName();
            String blueprintPath = property.getSchematicPath();
            String blueprintName = property.getSchematicName();
            BlueprintSourceModId blueprintSourceModId = property.getSourceModId();

            String objectType;
            Object blueprintObject;

            // 将蓝图加载为obj
            Pair<String, Object> result;

            if ( property.getSchematicSource() == BlueprintSourceFileLocation.folder ) {
                result = BlueprintProvider.getBlueprintObjectOfRef( Path.of(blueprintPath), blueprintSourceModId );

            } else if ( property.getSchematicSource() == BlueprintSourceFileLocation.datapack ) {
                Path root = PACK_REGISTRY.get(packName);
                if ( root == null ) { continue; }

                if ( root.toString().endsWith(".zip") ) {

                    try ( ZipFile zipFile = new ZipFile( root.toFile() ) ) {
                        ZipEntry entry = zipFile.getEntry(blueprintPath);
                        if ( entry == null ) {
                            getLogger().warn("压缩包内未找到蓝图：{} -> {}", root, blueprintName);
                            continue;
                        }
                        result = BlueprintProvider.getBlueprintObjectOfRef( zipFile.getInputStream(entry), blueprintSourceModId );

                    } catch ( IOException e ) {
                        getLogger().error("读取压缩包蓝图失败：{}", root, e);
                        continue;
                    }
                } else {
                    Path relativePath = Path.of( blueprintPath.substring( blueprintPath.indexOf('/') + 1 ) );
                    result = BlueprintProvider.getBlueprintObjectOfRef( root.resolve(relativePath), blueprintSourceModId );
                }
            } else {
                continue;
            }

            objectType = result.left();
            blueprintObject = result.right();

            if ( blueprintObject == null ) { continue; }

            BlueprintKey blueprintKey;
            BlueprintEntry blueprintEntry;

            // 检测蓝图大小，决定是否进入缓存
            // 不同mod的处理方法不同
            switch ( objectType ) {
                case "SableBlueprint" -> {
                    if ( blueprintObject instanceof SableBlueprint sableBp && blueprintSourceModId == sable_schematic_api ) {

                        if ( sableBp.blockCount() >= maxBlocks ) {
                            blueprintEntry = new BlueprintEntry( blueprintPath );
                            blueprintKey = BlueprintKey.of( blueprintPath, packName, blueprintSourceModId);
                            break;
                        }

                        blueprintEntry = new BlueprintEntry( sableBp );
                        blueprintKey = BlueprintKey.of( blueprintPath, packName, sable_schematic_api );
                    } else {
                        blueprintEntry = new BlueprintEntry( blueprintPath );
                        blueprintKey = BlueprintKey.of( blueprintPath, packName, invalid );
                    }
                }
                case "Path" -> {
                    blueprintEntry = new BlueprintEntry( blueprintPath );
                    blueprintKey = BlueprintKey.of( blueprintPath, packName, blueprintSourceModId );
                }
                default -> { continue; }
            }

            if ( blueprintKey == null ) { continue; }

            String target = blueprintPath.replace('\\', '/');
            blueprints.removeIf(p -> p.toString().replace('\\', '/').endsWith(target));

            property.setSchematicHash( blueprintKey.fileHash() );
            BLUEPRINT_BUFFER.put(blueprintKey, blueprintEntry);
        }

        // 把剩下的蓝图归拢 自动生成bpK
        for ( Path blueprintPath : blueprints ) {
            BlueprintSourceModId blueprintSourceModId = sourceModIdOf(blueprintPath);

            BlueprintEntry blueprintEntry = new BlueprintEntry( blueprintPath);
            BlueprintKey blueprintKey = BlueprintKey.of( blueprintPath, null, blueprintSourceModId );

            BLUEPRINT_BUFFER.put(blueprintKey, blueprintEntry);
        }
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
    private static BlueprintSourceModId sourceModIdOf(Path blueprintPath) {
        for ( BlueprintSourceModId candidate : BlueprintSourceModId.values() ) {
            if ( candidate == BlueprintSourceModId.invalid || candidate == BlueprintSourceModId.auto ) { continue; }
            if ( matchesPathOf( candidate, blueprintPath ) ) { return candidate; }
        }
        return BlueprintProvider.interpretBlueprintSource(blueprintPath);
    }
    private static boolean matchesPathOf(BlueprintSourceModId modId, Path blueprintPath) {
        String pathString = blueprintPath.toString().replace('\\', '/');
        return switch ( modId ) {
            case sable_schematic_api -> pathString.contains( getSableSchematicApiFolder().toString().replace('\\', '/') );
            // 未来支持的来源mod
            default -> false;
        };
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
    private static Path getSableSchematicApiFolder() {
        return getGameDir().resolve("Sable-Schematics");
    }


}
