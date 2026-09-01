package dev.sable.sablespawner.datapack.property.sublevel;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AllyProperty extends AbstractSchematicProperty {
    private boolean placeholder;

    private AllyProperty() {
        super();
        setSublevelType(SublevelType.ally);

        this.placeholder = true;
    }

    public static AllyProperty ofBasic(AbstractSchematicProperty base) {
        AllyProperty prop = new AllyProperty();
        prop.copyBaseFrom(base);
        return prop;
    }
    public static AllyProperty ofDefault() {
        return new AllyProperty();
    }
}
