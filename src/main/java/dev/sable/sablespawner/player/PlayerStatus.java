package dev.sable.sablespawner.player;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.WorldConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.level.ServerPlayer;

import static dev.sable.sablespawner.SableSpawnerConfig.PLAYER_PROTECTION_TIME;

public class PlayerStatus {
    @Getter private final ServerPlayer player;
    private boolean inProtection = false;
    private long inProtectionTime;
    @Getter @Setter private int playerScore = 0;

    public PlayerStatus(ServerPlayer player) {
        this.player = player;
    }

    public void protect() {
        this.inProtection = true;
        this.inProtectionTime = getGameTime();
    }
    public void removeProtect() {
        this.inProtection = false;
        this.inProtectionTime = -1;
    }
    public boolean isInProtection() {
        if ( this.inProtectionTime == -1 ) { return false; }
        this.inProtection = ( getGameTime() - inProtectionTime) <= (PLAYER_PROTECTION_TIME.getAsInt() );
        return this.inProtection;
    }

    public void addPlayerScore(int score) {
        if ( score<=0 ) { return; }
        this.playerScore += score;
    }
    public void subPlayerScore(int score) {
        if ( score<=0 ) { return; }
        this.playerScore -= score;
        if ( this.playerScore < 0 ) { this.playerScore = 0; }
    }

    public int getPlayerLevel() {
        int result = 1;
        if (getWorldConfig() == null || getWorldConfig().getWorldLevel() == null) { return result; }
        for (int i = 0; i < getWorldConfig().getWorldLevel().size(); i++) {
            if (this.playerScore >= getWorldConfig().getWorldLevel().get(i)) {
                result = i + 1;
            }
        }
        return result;
    }

    private WorldConfig getWorldConfig() { return SableSpawner.DATAPACK_MANAGER.getWorldConfig(); }
    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }

}
