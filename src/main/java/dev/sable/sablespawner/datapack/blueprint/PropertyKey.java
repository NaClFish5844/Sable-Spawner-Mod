package dev.sable.sablespawner.datapack.blueprint;

import dev.sable.sablespawner.datapack.DatapackManager;

public record PropertyKey(
        String name,
        DatapackManager.BlueprintSourceModId sourceMod,
        int sequenceNumber
) {
    public static PropertyKey of(String name, DatapackManager.BlueprintSourceModId modId, int sequence) {
        return new PropertyKey(name, modId, sequence);
    }
    public static PropertyKey of(String name, String modId, int sequence) {
        return new PropertyKey(name, parseModId(modId), sequence);
    }

    public static PropertyKey of(DatapackManager.BlueprintSourceModId modId, int sequence) {
        return new PropertyKey("未知", modId, sequence);
    }
    public static PropertyKey of(String modId, int sequence) {
        return new PropertyKey("未知", parseModId(modId), sequence);
    }

    private static DatapackManager.BlueprintSourceModId parseModId(String modId) {
        if (modId == null) { return DatapackManager.BlueprintSourceModId.auto; }
        return switch (modId) {
            case "sable_schematic_api" -> DatapackManager.BlueprintSourceModId.sable_schematic_api;
            default -> DatapackManager.BlueprintSourceModId.auto;
        };
    }

}
