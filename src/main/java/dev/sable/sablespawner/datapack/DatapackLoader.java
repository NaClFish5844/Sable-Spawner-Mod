package dev.sable.sablespawner.datapack;

import com.google.gson.*;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.sublevel.AllyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.datapack.property.sublevel.PrefabProperty;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class DatapackLoader {
    private DatapackLoader() {}

    protected static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    protected static DefaultConfig parseDefaultWorldConfig() {
        DefaultConfig defaultConfig = DefaultConfig.ofDefault();

        Path path = DatapackScanner.getDefaultConfig();
        if ( path == null ) { return defaultConfig; }

        JsonObject object = loadFile(path);
        if ( object == null ) { return defaultConfig; }

        if ( DatapackChecker.checkDefaultWorldConfigFormat(object) ) {
            defaultConfig.setWorldLevel( DatapackChecker.getNoDuplicatedSortedArrList(object.get("level")) );
            defaultConfig.setEnemyPrefix( object.get("enemy_prefix").getAsString() );
            defaultConfig.setAllyPrefix( object.get("ally_prefix").getAsString() );
            defaultConfig.setNeutralPrefix( object.get("neutral_prefix").getAsString() );
        }

        return defaultConfig;
    }

    protected static WorldConfig parseWorldConfig(Path worldConfigPath, DefaultConfig defaultConfig) {
        WorldConfig worldConfig = WorldConfig.ofBasic(defaultConfig);

        JsonObject object = loadFile(worldConfigPath);
        if ( object == null ) { return worldConfig; }

        DatapackChecker.WorldConfigCheckResult result = DatapackChecker.checkWorldConfigFormat(object);

        if ( result.isDimensionValid() ) {
            String dimension = object.get("dimension").getAsString();
            worldConfig.setDimension(dimension);
        } else {
            return null;
        }
        if ( result.isSpawnPatternValid() ) { worldConfig.setSpawnPattern( WorldConfig.Pattern.valueOf( object.get("spawn_pattern").getAsString() ) ); }

        if ( result.isWorldLevelValid() ) { worldConfig.setWorldLevel( DatapackChecker.getNoDuplicatedSortedArrList(object.get("world_level")) ); }
        if ( result.isEnemyPrefixValid() ) { worldConfig.setEnemyPrefix( object.get("enemy_prefix").getAsString() ); }
        if ( result.isAllyPrefixValid() ) { worldConfig.setAllyPrefix( object.get("ally_prefix").getAsString() ); }
        if ( result.isNeutralPrefixValid() ) { worldConfig.setNeutralPrefix( object.get("neutral_prefix").getAsString() ); }

        return worldConfig;
    }
    protected static List<AbstractSchematicProperty> parseProperty( Path propertyPath, String packName ) {
        List<AbstractSchematicProperty> properties = new ArrayList<>();

        JsonObject object = loadFile(propertyPath);
        if ( object == null ) { return properties; }

        DatapackChecker.PropertyCheckResult result = DatapackChecker.checkPropertyFormat(object);

        if ( !result.isTopLevelKeysValid() || !result.isTypeKeysValid() ) { return properties; }

        if ( result.isEnemyPropertyValid() ) { parseBlock(packName, properties, object, "enemy_property", EnemyProperty.class); }
        if ( result.isAllyPropertyValid() ) { parseBlock(packName, properties, object, "ally_property", AllyProperty.class); }
        if ( result.isPrefabPropertyValid() ) { parseBlock(packName, properties, object, "prefab_property", PrefabProperty.class); }

        return properties;
    }

    private static JsonObject loadFile(Path path) {
        try ( InputStream stream = Files.newInputStream(path) ) {
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();

        } catch ( IOException | RuntimeException e ) {
            getLogger().error("读取数据包文件失败：{}", path, e);
            return null;
        }
    }

    private static void parseBlock(String packName, List<AbstractSchematicProperty> destList, JsonObject object, String blockKey, Class<? extends AbstractSchematicProperty> propertyClass) {
        AbstractSchematicProperty prop;
        try {
            prop = GSON.fromJson( object.get(blockKey), propertyClass );
        } catch ( RuntimeException e ) {
            getLogger().warn("[{}] 反序列化失败，跳过该类型: {}", blockKey, e.toString());
            return;
        }
        parseBaseProperty(packName, object, prop);
        destList.add(prop);
    }
    private static void parseBaseProperty(String packName, JsonObject object, AbstractSchematicProperty prop) {
        prop.setPackName(packName);

        prop.setSchematicSource(DatapackManager.BlueprintSourceFileLocation.valueOf(object.get("schematic_source").getAsString()));
        prop.setSourceModId(DatapackManager.BlueprintSourceModId.valueOf(object.get("source_mod_id").getAsString()));

        String name = object.get("schematic_name").getAsString();
        String datapackPath = packName + "/data/blueprints/" + name;
        prop.setSchematicName(name);

        if ( prop.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.folder ) {
            String path = getSableSchematicApiFolder().resolve(name).toString();
            prop.setSchematicPath(path);

        } else if ( prop.getSchematicSource() == DatapackManager.BlueprintSourceFileLocation.datapack ) {
            prop.setSchematicPath(datapackPath);
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
    private static Path getSableSchematicApiFolder() {
        return getGameDir().resolve("Sable-Schematics");
    }


}
