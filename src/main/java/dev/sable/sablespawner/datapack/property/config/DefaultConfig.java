package dev.sable.sablespawner.datapack.property.config;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Getter
@Setter
public class DefaultConfig {
    @Nullable private String dimensionName = null;
    private IntArrayList worldLevel = new IntArrayList();

    private String enemyPrefix = "[ENEMY] ";
    private String allyPrefix = "[ALLY] ";
    private String neutralPrefix = "[NEUTRAL] ";
}
