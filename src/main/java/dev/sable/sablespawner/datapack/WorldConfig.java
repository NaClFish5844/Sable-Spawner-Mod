package dev.sable.sablespawner.datapack;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class WorldConfig {
    private ArrayList<Integer> worldLevel = new ArrayList<>();

    private String enemyPrefix = "[ENEMY] ";
    private String allyPrefix = "[ALLY] ";
    private String neutralPrefix = "[NEUTRAL] ";

}

