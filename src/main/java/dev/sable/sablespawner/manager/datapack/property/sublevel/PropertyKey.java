package dev.sable.sablespawner.manager.datapack.property.sublevel;


public record PropertyKey(
        String propertyName,
        String packName,
        AbstractSchematicProperty.SublevelType type
) {
    public static PropertyKey of(String path, String packName, AbstractSchematicProperty.SublevelType type) {
        return new PropertyKey(path, packName, type);
    }
}
