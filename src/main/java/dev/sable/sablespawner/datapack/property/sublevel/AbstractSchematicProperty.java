package dev.sable.sablespawner.datapack.property.sublevel;

import dev.sable.sablespawner.datapack.DatapackManager;
import lombok.Getter;
import lombok.Setter;

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

    @Nullable private String packName;

    private DatapackManager.BlueprintSourceFileLocation schematicSource;
    private DatapackManager.BlueprintSourceModId sourceModId;

    @Nullable private String schematicName;
    @Nullable private String schematicPath;

    private SublevelType sublevelType;
    private SublevelFunction sublevelFunction;

    protected AbstractSchematicProperty(
            @Nullable String packName,
            DatapackManager.BlueprintSourceFileLocation schematicSource,
            DatapackManager.BlueprintSourceModId sourceModId,
            @Nullable String schematicName,
            @Nullable String schematicPath,
            SublevelType sublevelType,
            SublevelFunction sublevelFunction
    ) {
        this.packName = packName;
        this.schematicSource = schematicSource;
        this.sourceModId = sourceModId;
        this.schematicName = schematicName;
        this.schematicPath = schematicPath;
        this.sublevelType = sublevelType;
        this.sublevelFunction = sublevelFunction;
    }

    protected AbstractSchematicProperty() {
        this(
                null,
                DatapackManager.BlueprintSourceFileLocation.invalid,
                DatapackManager.BlueprintSourceModId.auto,
                null, null,
                SublevelType.invalid,
                SublevelFunction.invalid
        );
    }

    protected void copyBaseFrom(AbstractSchematicProperty base) {
        this.packName = base.packName;
        this.schematicSource = base.schematicSource;
        this.sourceModId = base.sourceModId;
        this.schematicName = base.schematicName;
        this.schematicPath = base.schematicPath;
        this.sublevelType = base.sublevelType;
        this.sublevelFunction = base.sublevelFunction;
    }

}
