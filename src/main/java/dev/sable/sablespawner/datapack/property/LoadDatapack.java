package dev.sable.sablespawner.datapack.property;

import com.google.gson.*;
import dev.sable.sablespawner.datapack.schematics.SchematicProvider;
import dev.sable.sablespawner.datapack.WorldConfig;

import java.util.ArrayList;


public class LoadDatapack {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    public static ArrayList<AbstractSchematicProperty> loadProperty(JsonObject root) {
        ArrayList<AbstractSchematicProperty> properties = new ArrayList<>();

        JsonArray types = root.getAsJsonArray("sublevel_types");
        for (JsonElement element : types) {
            String type = element.getAsString();
            switch (type) {
                case "enemy" -> {
                    EnemyProperty ep = loadEnemy(root);
                    if (ep != null) properties.add(ep);
                }
                case "ally" -> {
                    AllyProperty ap = loadAlly(root);
                    if (ap != null) properties.add(ap);
                }
                case "prefabricated" -> {
                    PrefabProperty pp = loadPrefab(root);
                    if (pp != null) properties.add(pp);
                }
            }
        }

        return properties;
    }

    public static WorldConfig loadWorldLevel(JsonObject root) {
        WorldConfig config = new WorldConfig();
        JsonArray levelsArray = root.getAsJsonArray("levels");

        for (JsonElement e : levelsArray) {
            config.getWorldLevel().add(e.getAsInt());
        }

        if (root.has("enemy_prefix")) {
            config.setEnemyPrefix("[" + root.get("enemy_prefix").getAsString() + "] ");
        }
        if (root.has("ally_prefix")) {
            config.setAllyPrefix("[" + root.get("ally_prefix").getAsString() + "] ");
        }

        return config;
    }

    private static EnemyProperty loadEnemy(JsonObject root) {
        JsonObject block = root.getAsJsonObject("enemy_property");
        EnemyProperty prop = GSON.fromJson(block, EnemyProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.enemy);
        return prop;
    }

    private static AllyProperty loadAlly(JsonObject root) {
        JsonObject block = root.getAsJsonObject("ally_property");
        AllyProperty prop = GSON.fromJson(block, AllyProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.ally);
        return prop;
    }

    private static PrefabProperty loadPrefab(JsonObject root) {
        JsonObject block = root.getAsJsonObject("prefab_property");
        PrefabProperty prop = GSON.fromJson(block, PrefabProperty.class);
        fillCommon(root, prop, AbstractSchematicProperty.SublevelType.prefab);
        return prop;
    }

    private static void fillCommon(JsonObject root, AbstractSchematicProperty prop, AbstractSchematicProperty.SublevelType type) {
        prop.setSublevelType(type);

        if (root.has("schematic_source")) {
            prop.setSchematicSource(AbstractSchematicProperty.SchematicSource.valueOf(root.get("schematic_source").getAsString()));
        }

        if (root.has("source_mod_id")) {
            prop.setSourceModId(root.get("source_mod_id").getAsString());
        }

        if (root.has("schematic_path")) {
            String path = root.get("schematic_path").getAsString();
            if (prop.getSchematicSource() == AbstractSchematicProperty.SchematicSource.folder) {
                path = SchematicProvider.getSableSchematicApiFullPath(path);
            }
            prop.setSchematicPath(path);
        }

        if (root.has("sublevel_function")) {
            prop.setSublevelFunction(AbstractSchematicProperty.SublevelFunction.valueOf(root.get("sublevel_function").getAsString()));
        }
    }
}
