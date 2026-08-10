package dev.sable.sablespawner.datapack.schematics;

import dev.sable.sablespawner.SableSpawner;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SchematicProvider {
    public static List<String> getSableSchematicApiSchematics() {
        Path schematicDir = FMLPaths.GAMEDIR.get().resolve("Sable-Schematics");

        List<String> schematics = new ArrayList<>();
        if(!Files.isDirectory(schematicDir)) { return schematics; }

        try (var stream = Files.list(schematicDir)) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".nbt"))
                    .map(path -> path.getFileName().toString())
                    .toList();

        }catch (IOException exception){
            SableSpawner.LOGGER.error("Failed to scan sable-schematic-api schematics in {}", schematicDir, exception);
            return schematics;
        }
    }

    public static String getSableSchematicApiFullPath(String filename) {
        return FMLPaths.GAMEDIR.get().resolve("Sable-Schematics").resolve(filename).toString();
    }
}
