package dev.sable.sablespawner.player;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.WorldConfig;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScoreStatus {
    private int score = 0;

    public void addScore( int score ) {
        if ( score<=0 ) { return; }
        this.score += score;
    }
    public void subScore( int score ) {
        if ( score<=0 ) { return; }
        this.score -= score;
        if ( this.score < 0 ) { this.score = 0; }
    }
    public int getScoreLevel() {
        int result = 1;
        if (getWorldConfig() == null || getWorldConfig().getWorldLevel() == null) { return result; }
        for (int i = 0; i < getWorldConfig().getWorldLevel().size(); i++) {
            if (this.score >= getWorldConfig().getWorldLevel().get(i)) {
                result = i + 1;
            }
        }
        return result;
    }
    public void setScoreLevel( int level ) {
        if ( level > getWorldConfig().getWorldLevel().size() || level < 1 ) { return; }
        this.setScore( getWorldConfig().getWorldLevel().get( level - 1) );
    }

    private WorldConfig getWorldConfig() { return SableSpawner.DATAPACK_MANAGER.getWorldConfig(); }
}
