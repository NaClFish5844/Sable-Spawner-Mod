package dev.sable.sablespawner.player;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.WorldConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;

import static dev.sable.sablespawner.SableSpawnerConfig.PLAYER_PROTECTION_TIME;
import static dev.sable.sablespawner.player.PlayerDataAttachment.IN_PROTECTION_TIME;
import static dev.sable.sablespawner.player.PlayerDataAttachment.SCORE;

@Setter
@Getter
public class PlayerStatus {
    private ServerPlayer player;

    public PlayerStatus(ServerPlayer player) {
        this.player = player;
    }

    public void protect() {
        player.setData(IN_PROTECTION_TIME, getGameTime());
    }
    public void removeProtect() {
        player.setData(IN_PROTECTION_TIME, -1L);
    }
    public boolean isInProtection() {
        long protectionTime = player.getData(IN_PROTECTION_TIME);
        if ( protectionTime == -1 ) { return false; }
        return ( getGameTime() - protectionTime) <= (PLAYER_PROTECTION_TIME.getAsInt() );
    }

    public int getScore() {
        return player.getData(SCORE);
    }
    public void setScore( int score ) {
        player.setData(SCORE, Math.max(0, score));
    }
    public void addScore( int score ) {
        if ( score<=0 ) { return; }
        setScore( getScore() + score );
    }
    public void subScore( int score ) {
        if ( score<=0 ) { return; }
        setScore( getScore() - score );
    }
    public int getScoreLevel() {
        int result = 1;
        if ( getWorldConfig().getWorldLevel().isEmpty() ) { return result; }
        for (int i = 0; i < getWorldConfig().getWorldLevel().size(); i++) {
            if (getScore() >= getWorldConfig().getWorldLevel().get(i)) {
                result = i + 1;
            }
        }
        return result;
    }
    public void setScoreLevel( int level ) {
        if ( level > getWorldConfig().getWorldLevel().size() || level < 1 ) { return; }
        setScore( getWorldConfig().getWorldLevel().get( level - 1) );
    }


    private WorldConfig getWorldConfig() { return SableSpawner.DATAPACK_MANAGER.getWorldConfig(); }
    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }

}
