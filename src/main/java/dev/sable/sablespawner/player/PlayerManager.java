package dev.sable.sablespawner.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

import static dev.sable.sablespawner.SableSpawnerConfig.PLAYER_SPAWN_PROTECTION_TIME;

public class PlayerManager {
    public static final PlayerManager INSTANCE = new PlayerManager();

    private final Object2ObjectOpenHashMap<UUID, PlayerStatus> PlayerTracker = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus playerStatus = new PlayerStatus(player);
        playerStatus.protect( PLAYER_SPAWN_PROTECTION_TIME.getAsInt() );

        PlayerTracker.put(player.getUUID(), playerStatus);
    }
    @SubscribeEvent public void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerTracker.remove(player.getUUID());
    }
    @SubscribeEvent public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus status = PlayerTracker.get(player.getUUID());
        if (status != null) {
            status.setPlayer(player);
            status.protect();
        }
    }
    @SubscribeEvent public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus status = PlayerTracker.get(player.getUUID());
        if (status != null) {
            status.setPlayer(player);
            status.protect( PLAYER_SPAWN_PROTECTION_TIME.getAsInt() );
        }
    }

    public PlayerQuery query() {
        return new PlayerQuery(PlayerTracker.values().stream().toList());
    }

}
