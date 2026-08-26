package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.datapack.DatapackManager;

public record BlueprintKey (
        String blueprintName,
        DatapackManager.BlueprintSourceModId sourceMod,
        int sequenceNumber
) {
    public static BlueprintKey of(String name, DatapackManager.BlueprintSourceModId modId, int sequence) {
        return new BlueprintKey(name, modId, sequence);
    }
    public static BlueprintKey of(String name, String modId, int sequence) {
        return new BlueprintKey(name, parseModId(modId), sequence);
    }

    private static DatapackManager.BlueprintSourceModId parseModId(String modId) {
        if (modId == null) { return DatapackManager.BlueprintSourceModId.auto; }
        return switch (modId) {
            case "sable_schematic_api" -> DatapackManager.BlueprintSourceModId.sable_schematic_api;
            default -> DatapackManager.BlueprintSourceModId.auto;
        };
    }

}
