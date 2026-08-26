package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.DatapackManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BlueprintProvider {
    private BlueprintProvider(){}

    public void scanAllBlueprints() {

    }
    public void readAllBlueprints() {

    }
    private DatapackManager.BlueprintSourceModId parseAutoSource() {
        // 这是未填写源mod的蓝图的自动解析 预计非常复杂 以后再说
        return DatapackManager.BlueprintSourceModId.invalid;
    }

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

    ArrayList<String> supportedMods = new ArrayList<>(List.of(
            "sable_schematic_api"
    ));
    ArrayList<String> availableMods = new ArrayList<>();


    public void getAvailable(){
        for (String modID : supportedMods ) {
            if ( ModList.get().isLoaded(modID) ) { availableMods.add(modID); }
        }
        SableSpawner.LOGGER.info("Successfully scanned supported mods:{}",availableMods.toString());
    }

    public List<String> getSchematics(String modID){
        List<String> schematics = new ArrayList<>();

        switch (modID){
            case "sable_schematic_api" -> schematics = BlueprintProvider.getSableSchematicApiSchematics();
            // may add more schematic mods
        }
        SableSpawner.LOGGER.info("Successfully scanned schematics of {}",modID);

        return schematics;
    }

    public List<String> getAllSchematics() {
        List<String> schematics = new ArrayList<>();
        getAvailable();

        for (String modID : availableMods){
            schematics.addAll(getSchematics(modID));
        }
        return schematics;
    }

}
