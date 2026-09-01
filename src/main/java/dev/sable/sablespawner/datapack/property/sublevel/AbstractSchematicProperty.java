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

    private DatapackManager.BlueprintSourceFileLocation schematicSource;
    private DatapackManager.BlueprintSourceModId sourceModId;

    @Nullable private String schematicName;
    @Nullable private String schematicPath;
    @Nullable private ResourceLocation schematicResourceLocation;

    private SublevelType sublevelType;
    private SublevelFunction sublevelFunction;

    protected AbstractSchematicProperty(
            DatapackManager.BlueprintSourceFileLocation schematicSource,
            DatapackManager.BlueprintSourceModId sourceModId,
            @Nullable String schematicName,
            @Nullable String schematicPath,
            @Nullable ResourceLocation schematicResourceLocation,
            SublevelType sublevelType,
            SublevelFunction sublevelFunction
    ) {
        this.schematicSource = schematicSource;
        this.sourceModId = sourceModId;
        this.schematicName = schematicName;
        this.schematicPath = schematicPath;
        this.schematicResourceLocation = schematicResourceLocation;
        this.sublevelType = sublevelType;
        this.sublevelFunction = sublevelFunction;
    }
    protected AbstractSchematicProperty() {
        this(
                DatapackManager.BlueprintSourceFileLocation.invalid,
                DatapackManager.BlueprintSourceModId.auto,
                null, null, null,
                SublevelType.invalid,
                SublevelFunction.invalid
        );
    }

    protected void copyBaseFrom(AbstractSchematicProperty base) {
        this.schematicSource = base.schematicSource;
        this.sourceModId = base.sourceModId;
        this.schematicName = base.schematicName;
        this.schematicPath = base.schematicPath;
        this.schematicResourceLocation = base.schematicResourceLocation;
        this.sublevelType = base.sublevelType;
        this.sublevelFunction = base.sublevelFunction;
    }

}
