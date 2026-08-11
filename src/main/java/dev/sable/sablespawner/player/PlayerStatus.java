package dev.sable.sablespawner.player;

import dev.sable.sablespawner.SableSpawner;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import static dev.sable.sablespawner.SableSpawnerConfig.PLAYER_PROTECTION_TIME;

public class PlayerStatus extends ScoreStatus {
    @Getter private final ServerPlayer player;
    private boolean inProtection = false;
    private long inProtectionTime = -1;

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

    public Level getLevel() {
        return player.level();
    }


    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }

}
