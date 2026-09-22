package dev.sablespawner.manager.datapack;

public record PackMeta(
        String packName
) {
    public static PackMeta of(String packName) {
        return new PackMeta(packName);
    }

}
