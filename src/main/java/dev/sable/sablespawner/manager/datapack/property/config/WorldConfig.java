package dev.sable.sablespawner.manager.datapack.property.config;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

import static dev.sable.sablespawner.manager.datapack.property.config.WorldConfig.Pattern.invalid;

@Getter @Setter
public class WorldConfig extends DefaultConfig {
    private WorldConfig(DefaultConfig defaultConfig, @Nullable String dimension, Pattern spawnPattern) {
        super(
                defaultConfig.getWorldLevel(),
                defaultConfig.getEnemyPrefix(),
                defaultConfig.getAllyPrefix(),
                defaultConfig.getNeutralPrefix()
        );
        this.dimension = dimension;
        this.spawnPattern = spawnPattern;
    }

    public static WorldConfig of(DefaultConfig defaultConfig, String dimension, Pattern spawnPattern) {
        return new WorldConfig(defaultConfig, dimension, spawnPattern);
    }
    public static WorldConfig ofBasic(DefaultConfig defaultConfig){
        return WorldConfig.of(defaultConfig, null, invalid);
    }
    public static WorldConfig ofDefault() {
        return WorldConfig.of(
                DefaultConfig.ofDefault(),
                null,
                invalid
        );
    }

    public enum Pattern {
        space,
        ocean,
        land,
        invalid
    }

    @Nullable private String dimension;
    private Pattern spawnPattern;

    public int getScoreLevel(int score) {
        int result = 1;
        if ( this.getWorldLevel().isEmpty() ) { return result; }
        for ( int i = 0; i < this.getWorldLevel().size(); i++ ) {
            if ( score >= this.getWorldLevel().get(i) ) {
                result = i + 1;
            }
        }
        return result;
    }

}

