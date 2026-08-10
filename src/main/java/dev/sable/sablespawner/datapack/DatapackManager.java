package dev.sable.sablespawner.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.datapack.property.LoadDatapack;
import dev.sable.sablespawner.spawn.GlobalControl;
import dev.sable.sablespawner.spawn.Spawner;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter @Setter
public class DatapackManager {
    public static final DatapackManager INSTANCE = new DatapackManager();

    @Nullable public ArrayList<AbstractSchematicProperty> allProperties = new ArrayList<>();
    @Nullable public WorldConfig worldConfig = new WorldConfig();

    public void loadDatapack(
        Map<ResourceLocation, JsonElement> files,
        ResourceManager resourceManager)
    {
        ArrayList<AbstractSchematicProperty> properties = new ArrayList<>();

        for (var entry : files.entrySet()) {
            JsonObject root = entry.getValue().getAsJsonObject();
            properties.addAll(LoadDatapack.loadProperty(root));
        }
        allProperties = properties;

        Optional<Resource> wlResource = resourceManager
                .getResource(ResourceLocation.fromNamespaceAndPath("sablespawner", "sablespawner/worldconfig.json"));

        if (wlResource.isPresent()) {
            try (InputStream is = wlResource.get().open()) {
                String content = new String(is.readAllBytes());
                JsonObject root = JsonParser.parseString(content).getAsJsonObject();
                worldConfig = LoadDatapack.loadWorldLevel(root);
            } catch (IOException ignored) {
            }
        }
    }

    public int getLevelForScore(int score) {
        int result = 1;
        if (worldConfig == null || worldConfig.getWorldLevel() == null) { return result; }
        for (WorldConfig.WorldLevelEntry entry : worldConfig.getWorldLevel()) {
            if (score >= entry.requiredScore()) {
                result = entry.level();
            }
        }
        return result;
    }

}
