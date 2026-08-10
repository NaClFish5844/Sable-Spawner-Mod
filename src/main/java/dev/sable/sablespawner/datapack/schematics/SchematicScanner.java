package dev.sable.sablespawner.datapack.schematics;

import java.util.ArrayList;
import java.util.List;

import dev.sable.sablespawner.SableSpawner;
import net.neoforged.fml.ModList;


public class SchematicScanner {
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
            case "sable_schematic_api" -> schematics = SchematicProvider.getSableSchematicApiSchematics();
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

