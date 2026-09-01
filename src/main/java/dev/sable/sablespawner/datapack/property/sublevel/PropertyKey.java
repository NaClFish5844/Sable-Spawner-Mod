package dev.sable.sablespawner.datapack.property.sublevel;


public record PropertyKey(
        String name,
        AbstractSchematicProperty.SublevelType type
) {
    public static PropertyKey of(String name, AbstractSchematicProperty.SublevelType type) {
        return new PropertyKey(name, type);
    }
}
