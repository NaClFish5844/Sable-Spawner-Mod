package dev.sablespawner.player;

import dev.sablespawner.SableSpawner;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;

import static dev.sablespawner.SableSpawnerConfig.PLAYER_PROTECTION_TIME;
import static dev.sablespawner.player.PlayerDataAttachment.OUT_PROTECTION_TIME;
import static dev.sablespawner.player.PlayerDataAttachment.SCORE;

@Setter
@Getter
public class PlayerStatus {
    private ServerPlayer player;

    public PlayerStatus(ServerPlayer player) {
        this.player = player;
    }

    public void protect() {
        player.setData(
                OUT_PROTECTION_TIME.get(),
                getGameTime() + PLAYER_PROTECTION_TIME.getAsInt()
        );
    }
    public void protect(long time) {
        player.setData(
                OUT_PROTECTION_TIME.get(),
                getGameTime() + time
        );
    }
    public void removeProtect() {
        player.setData(
                OUT_PROTECTION_TIME.get(),
                getGameTime()
        );
    }
    public boolean isInProtection() {
        return getGameTime() <= player.getData(OUT_PROTECTION_TIME.get());
    }
    public long getOutProtectionTime() {
        return player.getData(OUT_PROTECTION_TIME.get());
    }
    public long getProtectionExpireTime() {
        return Math.max(0, player.getData(OUT_PROTECTION_TIME.get()) - getGameTime());
    }

    public int getScore() {
        return player.getData(SCORE.get());
    }
    public void setScore( int score ) {
        player.setData(SCORE.get(), Math.max(0, score));
    }
    public void addScore( int score ) {
        if ( score<=0 ) { return; }
        setScore( getScore() + score );
    }
    public void subScore( int score ) {
        if ( score<=0 ) { return; }
        setScore( getScore() - score );
    }

    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }

}
