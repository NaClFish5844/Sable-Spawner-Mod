package dev.sable.sablespawner.datapack.property.config;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Getter
@Setter
public class DefaultConfig {
    @Nullable private String dimensionName = null;
    private ArrayList<Integer> worldLevel = new ArrayList<>();

    private String enemyPrefix = "[ENEMY] ";
    private String allyPrefix = "[ALLY] ";
    private String neutralPrefix = "[NEUTRAL] ";
}
