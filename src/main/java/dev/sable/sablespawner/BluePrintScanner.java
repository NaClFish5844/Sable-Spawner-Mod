package dev.sable.sablespawner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;


public class BluePrintScanner {
    ArrayList<String> supportedMods = new ArrayList<>(List.of(
            "sable_schematic_api"
    ));
    ArrayList<String> availableMods = new ArrayList<>();

    public void getAvailable(){
        for (String modID : supportedMods){
            if (ModList.get().isLoaded(modID)){
                availableMods.add(modID);
            }
        }
        SableSpawner.LOGGER.info("Successfully scanned supported mods:{}",availableMods.toString());
    }

    public List<Path> getBlueprints(String modID){
        List<Path> blueprints = new ArrayList<>();

        switch (modID){
            case "sable_schematic_api" -> blueprints = BluePrintProvider.getSableSchematicBlueprints();
            // may add more schematic mods
        }
        SableSpawner.LOGGER.info("Successfully scanned schematics of {}",modID);
        return blueprints;
    }

    public List<Path> getAllBlueprints() {
        List<Path> blueprints = new ArrayList<>();
        getAvailable();

        for (String modID : availableMods){
            blueprints.addAll(getBlueprints(modID));
        }
        return blueprints;
    }

}

class BluePrintProvider {
    public static List<Path> getSableSchematicBlueprints() {
        Path gameDir = FMLPaths.GAMEDIR.get();
        Path schematicDir = gameDir.resolve("Sable-Schematics");

        List<Path> blueprints = new ArrayList<>();
        if(!Files.isDirectory(schematicDir)) { return blueprints; }

        try (var stream = Files.list(schematicDir)) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".nbt"))
                    .toList();

        }catch (IOException exception){
            SableSpawner.LOGGER.error("Failed to scan sable-schematic blueprints in {}", schematicDir, exception);
            return blueprints;
        }
    }
}

