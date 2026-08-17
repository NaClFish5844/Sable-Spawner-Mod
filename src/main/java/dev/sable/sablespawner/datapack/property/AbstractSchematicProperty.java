package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


@Getter @Setter
public abstract class AbstractSchematicProperty {
    public enum SchematicSource{
        datapack,
        folder
    }
    public enum SourceModId{
        sable_schematic_api
    }
    public enum SublevelType{
        ally,
        enemy,
        prefab,
        neutral
    }
    public enum SublevelFunction{
        warship,
        cargo,
        placeholder
    }

    @Nullable private SchematicSource schematicSource = null;
    @Nullable private SourceModId sourceModId = null;

    @Nullable private String schematicPath = null;
    @Nullable private ResourceLocation schematicResourceLocation = null;

    @Nullable private SublevelType sublevelType = SublevelType.neutral;
    @Nullable private SublevelFunction sublevelFunction = SublevelFunction.placeholder;

}
