package dev.sablespawner.manager.datapack.property.sublevel;


public record PropertyKey(
        String propertyName,
        String packName,
        AbstractSchematicProperty.SublevelType type
) {
    public static PropertyKey of(String propertyName, String packName, AbstractSchematicProperty.SublevelType type) {
        return new PropertyKey(propertyName, packName, type);
    }
}
