package dev.sable.sablespawner.datapack.property.sublevel;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrefabProperty extends AbstractSchematicProperty {
    private int price;
    private boolean reusable;

    private PrefabProperty() {
        super();
        setSublevelType(SublevelType.prefab);

        this.price = 0;
        this.reusable = false;
    }

    public static PrefabProperty ofBasic(AbstractSchematicProperty base) {
        PrefabProperty prop = new PrefabProperty();
        prop.copyBaseFrom(base);
        return prop;
    }
    public static PrefabProperty ofDefault() {
        return new PrefabProperty();
    }
}
