package dev.sablespawner.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;
import java.util.UUID;

import static dev.sablespawner.SableSpawnerConfig.PLAYER_SPAWN_PROTECTION_TIME;

@Getter
public class PlayerManager {
    public static final PlayerManager INSTANCE = new PlayerManager();

    private final Object2ObjectOpenHashMap<UUID, PlayerStatus> PLAYER_TRACKER = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus playerStatus = new PlayerStatus(player);
        playerStatus.protect( PLAYER_SPAWN_PROTECTION_TIME.getAsInt() );

        PLAYER_TRACKER.put(player.getUUID(), playerStatus);
    }
    @SubscribeEvent public void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PLAYER_TRACKER.remove(player.getUUID());
    }
    @SubscribeEvent public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus status = PLAYER_TRACKER.get(player.getUUID());
        if (status != null) {
            status.setPlayer(player);
            status.protect();
        }
    }
    @SubscribeEvent public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus status = PLAYER_TRACKER.get(player.getUUID());
        if (status != null) {
            status.setPlayer(player);
            status.protect( PLAYER_SPAWN_PROTECTION_TIME.getAsInt() );
        }
    }

    public PlayerQuery query() {
        return new PlayerQuery(PLAYER_TRACKER);
    }

    @Nullable public PlayerStatus getStatus(ServerPlayer player) {
        return PLAYER_TRACKER.get(player.getUUID());
    }

}
