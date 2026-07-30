package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;


@Getter @Setter
public abstract class AbstractSchematicProperty {
    public enum SchematicSource{
        datapack,
        folder
    }
    public enum SublevelType{
        ally,
        enemy,
        prefabricated,
        neutral
    }
    public enum SublevelFunction{
        warship,
        cargo
    }

    @Nullable public SchematicSource schematicSource = null;
    @Nullable public String modID = null;
    @Nullable public String schematicPath = null;
    @Nullable public SublevelType sublevelType = SublevelType.neutral;
    @Nullable public SublevelFunction sublevelFunction = null;

}
