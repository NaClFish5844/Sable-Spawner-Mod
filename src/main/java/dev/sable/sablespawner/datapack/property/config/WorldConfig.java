package dev.sable.sablespawner.datapack.property.config;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorldConfig extends DefaultConfig {
    public enum Pattern {
        space,
        ocean,
        land,
        invalid
    }

    private Pattern spawnPattern = Pattern.space;

}

