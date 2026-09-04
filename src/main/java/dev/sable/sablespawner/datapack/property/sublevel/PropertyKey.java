package dev.sable.sablespawner.datapack.property.sublevel;


public record PropertyKey(
        String path, // packname/filename  | no suffix
        AbstractSchematicProperty.SublevelType type
) {
    public static PropertyKey of(String path, AbstractSchematicProperty.SublevelType type) {
        return new PropertyKey(path, type);
    }
    public String getName() {
        return this.path.substring(this.path.lastIndexOf('/') + 1);
    }
}
