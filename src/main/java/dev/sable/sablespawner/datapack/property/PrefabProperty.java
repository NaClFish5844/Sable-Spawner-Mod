package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrefabProperty extends AbstractSchematicProperty {
    private int price = 0;
    private boolean reusable = false;
}
