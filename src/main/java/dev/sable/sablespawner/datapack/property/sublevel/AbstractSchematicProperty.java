package dev.sable.sablespawner.datapack.property.sublevel;

import dev.sable.sablespawner.datapack.DatapackManager;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;


@Getter @Setter
public abstract class AbstractSchematicProperty {
    public enum SublevelType{
        ally,
        enemy,
        prefab,
        invalid
    }
    public enum SublevelFunction{
        warship,
        cargo,
        invalid
    }

    @Nullable private DatapackManager.BlueprintSourceFileLocation schematicSource = null;
    @Nullable private DatapackManager.BlueprintSourceModId sourceModId = DatapackManager.BlueprintSourceModId.auto;

    @Nullable private String schematicName = null;
    @Nullable private String schematicPath = null;
    @Nullable private ResourceLocation schematicResourceLocation = null;

    @Nullable private SublevelType sublevelType = SublevelType.invalid;
    @Nullable private SublevelFunction sublevelFunction = SublevelFunction.invalid;

}
