package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;
import javax.annotation.Nullable;


@Getter @Setter
public abstract class AbstractSchematicProperty {
    public enum SchematicSource{
        datapack,
        folder
    }
    public enum SublevelType{
        ally,
        enemy,
        prefab,
        neutral
    }
    public enum SublevelFunction{
        warship,
        cargo
    }

    @Nullable public SchematicSource schematicSource = null;
    @Nullable public String sourceModId = null;
    @Nullable public String schematicPath = null;
    @Nullable public SublevelType sublevelType = SublevelType.neutral;
    @Nullable public SublevelFunction sublevelFunction = null;

}
