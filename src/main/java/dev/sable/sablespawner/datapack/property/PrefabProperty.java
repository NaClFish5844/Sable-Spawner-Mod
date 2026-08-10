package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PrefabProperty extends AbstractSchematicProperty {
    public int price = -1;
    public boolean reusable = false;
}
