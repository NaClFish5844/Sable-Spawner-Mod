package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class WorldConfig {
    public enum Pattern {
        space,
        ocean,
        land,
        invalid
    }
    private String dimensionName;

    private ArrayList<Integer> worldLevel = new ArrayList<>();

    private Pattern spawnPattern = Pattern.space;

    private String enemyPrefix = "[ENEMY] ";
    private String allyPrefix = "[ALLY] ";
    private String neutralPrefix = "[NEUTRAL] ";

}

