package dev.sable.sablespawner.manager.blueprint;


import dev.rew1nd.sableschematicapi.blueprint.SableBlueprint;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.DatapackScanner;
import dev.sable.sablespawner.manager.datapack.property.sublevel.AbstractSchematicProperty;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static dev.sable.sablespawner.SableSpawnerConfig.BLUEPRINT_CACHE_MAX_BLOCKS;
import static dev.sable.sablespawner.manager.datapack.DatapackManager.BlueprintSourceModId.invalid;
import static dev.sable.sablespawner.manager.datapack.DatapackManager.BlueprintSourceModId.sable_schematic_api;

public class BlueprintManager {
    public static final BlueprintManager INSTANCE = new BlueprintManager();

    private final Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> BLUEPRINT_REGISTRY = new Object2ObjectOpenHashMap<>();
    // 小的直接存对象 大的存路径或ResLoc
    // 需要在使用文档里提示“加载大东西可能相对较慢”
    // 以后是否能重写 使得巨型东西加载变快？？？


    @Deprecated
    public void reloadBlueprint() {
        BLUEPRINT_BUFFER.clear();

        // 扫描所有位置的蓝图
        Set<Path> blueprints = new HashSet<>();

        for ( Path root : DATAPACK_REGISTRY.values() ) {
            Path validRoot = DatapackScanner.scanRelativeValidRoot(root);
            if ( validRoot == null ) { continue; }

            blueprints.addAll( DatapackScanner.scanBlueprintsOfPack(validRoot) );
        }

        for ( DatapackManager.BlueprintSourceModId modId : DatapackManager.BlueprintSourceModId.values() ) {
            if ( modId == DatapackManager.BlueprintSourceModId.invalid || modId == DatapackManager.BlueprintSourceModId.auto ) { continue; }
            if ( !getModList().isLoaded( modId.toString() ) ) { continue; }

            Set<Path> bp = BlueprintScanner.scanModFolder(modId);

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
            DatapackManager.BlueprintSourceModId blueprintSourceModId = property.getSourceModId();

            String objectType;
            Object blueprintObject;

            // 将蓝图加载为obj
            Pair<String, Object> result;

            if ( property.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.folder ) {
                result = BlueprintScanner.getBlueprintObjectOfRef( Path.of(blueprintPath), blueprintSourceModId );

            } else if ( property.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.datapack ) {
                Path root = DATAPACK_REGISTRY.get(packName);
                if ( root == null ) { continue; }

                if ( root.toString().endsWith(".zip") ) {

                    try ( ZipFile zipFile = new ZipFile( root.toFile() ) ) {
                        ZipEntry entry = zipFile.getEntry(blueprintPath);
                        if ( entry == null ) {
                            getLogger().warn("压缩包内未找到蓝图：{} -> {}", root, blueprintName);
                            continue;
                        }
                        result = BlueprintScanner.getBlueprintObjectOfRef( zipFile.getInputStream(entry), blueprintSourceModId );

                    } catch ( IOException e ) {
                        getLogger().error("读取压缩包蓝图失败：{}", root, e);
                        continue;
                    }
                } else {
                    Path relativePath = Path.of( blueprintPath.substring( blueprintPath.indexOf('/') + 1 ) );
                    result = BlueprintScanner.getBlueprintObjectOfRef( root.resolve(relativePath), blueprintSourceModId );
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
                            blueprintKey = BlueprintKey.of( blueprintPath, blueprintSourceModId);
                            break;
                        }

                        blueprintEntry = new BlueprintEntry( sableBp );
                        blueprintKey = BlueprintKey.of( blueprintPath, sable_schematic_api );
                    } else {
                        blueprintEntry = new BlueprintEntry( blueprintPath );
                        blueprintKey = BlueprintKey.of( blueprintPath, invalid );
                    }
                }
                case "path" -> {
                    blueprintEntry = new BlueprintEntry( blueprintPath );
                    blueprintKey = BlueprintKey.of( blueprintPath, blueprintSourceModId );
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
            DatapackManager.BlueprintSourceModId blueprintSourceModId = sourceModIdOf(blueprintPath);

            BlueprintEntry blueprintEntry = new BlueprintEntry( blueprintPath);
            BlueprintKey blueprintKey = BlueprintKey.of( blueprintPath, blueprintSourceModId );

            BLUEPRINT_BUFFER.put(blueprintKey, blueprintEntry);
        }
    }

    @Nullable public Pair<String, Object> getBlueprintObject(BlueprintEntry blueprintEntry) {
        return null;
        // 根据entry直接确定蓝图
    }
    @Nullable public Pair<String, Object> getBlueprintObject(AbstractSchematicProperty property) {
        // 根据hash直接确定蓝图
        return null;
    }



    public BlueprintQuery query() {
        return new BlueprintQuery( getBuffer() );
    }

    public Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getBuffer() { return BLUEPRINT_REGISTRY; }
}
