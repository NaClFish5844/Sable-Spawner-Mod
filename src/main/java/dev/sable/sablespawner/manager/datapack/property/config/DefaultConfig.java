package dev.sable.sablespawner.manager.datapack.property.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class DefaultConfig {
    protected DefaultConfig(List<Integer> worldLevel, String enemyPrefix, String allyPrefix, String neutralPrefix) {
        this.worldLevel = worldLevel;
        this.enemyPrefix = enemyPrefix;
        this.allyPrefix = allyPrefix;
        this.neutralPrefix = neutralPrefix;
    }

    public static DefaultConfig of(List<Integer> worldLevel, String enemyPrefix, String allyPrefix, String neutralPrefix) {
        return new DefaultConfig(worldLevel, enemyPrefix, allyPrefix, neutralPrefix);
    }
    public static DefaultConfig ofDefault() {
        return new DefaultConfig(
                List.of(0),
                "[ENEMY] ",
                "[ALLY] ",
                "[NEUTRAL] "
        );
    }

    private List<Integer> worldLevel;

    private String enemyPrefix;
    private String allyPrefix;
    private String neutralPrefix;
}
