package dev.sable.sablespawner.datapack.property;

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
        neutral
    }
    public enum SublevelFunction{
        warship,
        cargo,
        placeholder
    }

    @Nullable private DatapackManager.BlueprintSourceFileLocation schematicSource = null;
    @Nullable private DatapackManager.BlueprintSourceModId sourceModId = DatapackManager.BlueprintSourceModId.auto;

    @Nullable private String schematicName = null;
    @Nullable private String schematicPath = null;
    @Nullable private ResourceLocation schematicResourceLocation = null;

    @Nullable private SublevelType sublevelType = SublevelType.neutral;
    @Nullable private SublevelFunction sublevelFunction = SublevelFunction.placeholder;

}
