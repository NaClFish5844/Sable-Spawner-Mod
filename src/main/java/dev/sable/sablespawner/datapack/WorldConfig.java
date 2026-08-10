package dev.sable.sablespawner.datapack;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter @Setter
public class WorldConfig {
    public record WorldLevelEntry(int level, int requiredScore) {}
    public ArrayList<WorldLevelEntry> worldLevel = new ArrayList<>();

    public String enemyPrefix = "[ENEMY] ";
    public String allyPrefix = "[ALLY] ";

    public static final String neutralPrefix = "[NEUTRAL] ";
}

