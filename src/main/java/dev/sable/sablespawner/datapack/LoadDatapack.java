package dev.sable.sablespawner.datapack;

import com.google.gson.*;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.sublevel.AllyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.PrefabProperty;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class LoadDatapack {
    private LoadDatapack() {}

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected static DefaultConfig parseDefaultWorldConfig( Map<ResourceLocation, JsonElement> datapackFiles ) {
        DefaultConfig defaultConfig = DefaultConfig.ofDefault();

        if ( Checker.checkDefaultWorldConfig(datapackFiles) ) {
            JsonObject object = datapackFiles
                    .get( ResourceLocation.fromNamespaceAndPath( "sablespawner", "worldconfig/default" ) )
                    .getAsJsonObject();
            defaultConfig.setWorldLevel( Checker.getNoDuplicatedSortedArrList(object.get("world_level")) );
            defaultConfig.setEnemyPrefix( object.get("enemy_prefix").getAsString() );
            defaultConfig.setAllyPrefix( object.get("ally_prefix").getAsString() );
            defaultConfig.setNeutralPrefix( object.get("neutral_prefix").getAsString() );
        }

        return defaultConfig;
    }
    protected static WorldConfig parseWorldConfig( JsonElement json, DefaultConfig defaultConfig ) {
        JsonObject object = json.getAsJsonObject();
        Checker.WorldConfigCheckResult result = Checker.checkWorldConfigFormat(object);

        WorldConfig worldConfig = WorldConfig.ofBasic(defaultConfig);

        if ( result.isDimensionValid() ) {
            String dimension = object.get("dimension").getAsString();
            worldConfig.setDimension(dimension);
        } else {
            return null;
        }
        if ( result.isSpawnPatternValid() ) { worldConfig.setSpawnPattern( WorldConfig.Pattern.valueOf( object.get("spawn_pattern").getAsString() ) ); }

        if ( result.isWorldLevelValid() ) { worldConfig.setWorldLevel( Checker.getNoDuplicatedSortedArrList(object.get("world_level")) ); }
        if ( result.isEnemyPrefixValid() ) { worldConfig.setEnemyPrefix( object.get("enemy_prefix").getAsString() ); }
        if ( result.isAllyPrefixValid() ) { worldConfig.setAllyPrefix( object.get("ally_prefix").getAsString() ); }
        if ( result.isNeutralPrefixValid() ) { worldConfig.setNeutralPrefix( object.get("neutral_prefix").getAsString() ); }

        return worldConfig;
    }
    // protected record PropertyCheckResult
    //         boolean isTopLevelKeysValid,
    //         boolean isTypeKeysValid,
    //         boolean isAllyPropertyValid,
    //         boolean isEnemyPropertyValid,
    //         boolean isPrefabPropertyValid
    protected static List<AbstractSchematicProperty> parseProperty(JsonElement json ) {
        List<AbstractSchematicProperty> properties = new ArrayList<>();

        JsonObject object = json.getAsJsonObject();
        Checker.PropertyCheckResult result = Checker.checkPropertyFormat(object);

        if ( !result.isTopLevelKeysValid() || !result.isTypeKeysValid() ) { return properties; }

        if ( result.isEnemyPropertyValid() ) { parseBlock(properties, object, "enemy_property", EnemyProperty.class); }
        if ( result.isAllyPropertyValid() ) { parseBlock(properties, object, "ally_property", AllyProperty.class); }
        if ( result.isPrefabPropertyValid() ) { parseBlock(properties, object, "prefab_property", PrefabProperty.class); }

        return properties;
    }


    // 一个防呆机制 防止有人把config放进property之类的操作
    // “开启强力检测”
    // 直接从Path侧重读压缩包
    // 以后再说
    // 会新建一个文件夹 专门放数据 方便版本间迁移
    // 以后会把数据包尝试迁移到这里？
    private enum Type {
        placeholder
    }

    private static Type interpretType(JsonObject object) {
        return null;
    }
    private static JsonObject loadFile(Path path) {
        // .json -> JsonObject
        return null;
    }
    // 读列表
    private static ObjectList<Path> readProperties() {
        return null;
    }
    private static ObjectList<Path> readWorldConfigs() {
        return null;
    }

    private static void parseBlock(List<AbstractSchematicProperty> list, JsonObject object, String blockKey, Class<? extends AbstractSchematicProperty> propertyClass) {
        AbstractSchematicProperty prop;
        try {
            prop = GSON.fromJson( object.get(blockKey), propertyClass );
        } catch ( RuntimeException e ) {
            getLogger().warn("[{}] 反序列化失败，跳过该类型: {}", blockKey, e.toString());
            return;
        }
        parseBaseProperty(object, prop);
        list.add(prop);
    }
    private static void parseBaseProperty(JsonObject object, AbstractSchematicProperty prop) {
        prop.setSchematicSource(DatapackManager.BlueprintSourceFileLocation.valueOf(object.get("schematic_source").getAsString()));
        prop.setSourceModId(DatapackManager.BlueprintSourceModId.valueOf(object.get("source_mod_id").getAsString()));

        String name = object.get("schematic_name").getAsString();
        prop.setSchematicName(name);
        if ( prop.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.folder ) {
            String path = getGameDir().resolve("Sable-Schematics").resolve(name).toString();
            prop.setSchematicPath(path);
        } else {
            prop.setSchematicPath(name);
            prop.setSchematicResourceLocation(ResourceLocation.fromNamespaceAndPath("sablespawner", "sablespawner/schematics/" + name));
        }

        if ( object.has("sublevel_function") ) {
            prop.setSublevelFunction(AbstractSchematicProperty.SublevelFunction.valueOf(object.get("sublevel_function").getAsString()));
        }
    }


    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
    private static ResourceManager getResourceManager() {
        return SableSpawner.RESOURCE_MANAGER;
    }

}
