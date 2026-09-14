package dev.sable.sablespawner.manager.datapack.property.sublevel;

import dev.sable.sablespawner.manager.datapack.DatapackManager;
import lombok.Getter;
import lombok.Setter;


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

    private String packName;

    private DatapackManager.BlueprintSourceFileLocation schematicSource;
    private DatapackManager.BlueprintSourceModId sourceModId;

    private String schematicName;

    private SublevelType sublevelType;
    private SublevelFunction sublevelFunction;

    protected AbstractSchematicProperty(
            String packName,
            DatapackManager.BlueprintSourceFileLocation schematicSource,
            DatapackManager.BlueprintSourceModId sourceModId,
            String schematicName,
            SublevelType sublevelType,
            SublevelFunction sublevelFunction
    ) {
        this.packName = packName;
        this.schematicSource = schematicSource;
        this.sourceModId = sourceModId;
        this.schematicName = schematicName;
        this.sublevelType = sublevelType;
        this.sublevelFunction = sublevelFunction;
    }

    protected AbstractSchematicProperty() {
        this(
                null,
                DatapackManager.BlueprintSourceFileLocation.invalid,
                null,
                "unknown",
                SublevelType.invalid,
                SublevelFunction.invalid
        );
    }

    protected void copyBaseFrom(AbstractSchematicProperty base) {
        this.packName = base.packName;
        this.schematicSource = base.schematicSource;
        this.sourceModId = base.sourceModId;
        this.schematicName = base.schematicName;
        this.sublevelType = base.sublevelType;
        this.sublevelFunction = base.sublevelFunction;
    }

}
