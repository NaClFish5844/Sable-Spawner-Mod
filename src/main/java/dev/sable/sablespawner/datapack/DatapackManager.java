package dev.sable.sablespawner.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Getter @Setter
public class DatapackManager {
    public static final DatapackManager INSTANCE = new DatapackManager();
    static Logger LOGGER = SableSpawner.LOGGER;

    private ArrayList<AbstractSchematicProperty> allProperties = new ArrayList<>();
    private WorldConfig worldConfig = new WorldConfig();

    public void loadDatapack( Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager ) {
        ArrayList<AbstractSchematicProperty> properties = new ArrayList<>();

        for (var entry : files.entrySet()) {
            if ( !LoadDatapack.checkFormat(entry) ) { continue; }

            try {
                properties.addAll(LoadDatapack.loadProperty(entry.getValue().getAsJsonObject()));
            } catch (RuntimeException e) {
                LOGGER.warn("跳过无效数据包文件 {}: {}", entry.getKey(), e.toString());
            }
        }
        allProperties = properties;

        Optional<Resource> wlResource = resourceManager
                .getResource(ResourceLocation.fromNamespaceAndPath("sablespawner", "sablespawner/worldconfig.json"));

        if (wlResource.isPresent()) {
            try (InputStream is = wlResource.get().open()) {
                String content = new String(is.readAllBytes());
                JsonObject root = JsonParser.parseString(content).getAsJsonObject();
                worldConfig = LoadDatapack.loadWorldLevel(root);
            } catch (IOException | RuntimeException ignored) {
            }
        }
    }

    public PropertyQuery query() {
        return new PropertyQuery(allProperties.stream().toList());
    }

}
