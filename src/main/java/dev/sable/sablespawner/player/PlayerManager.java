package dev.sable.sablespawner.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.UUID;

public class PlayerManager {
    public static final PlayerManager INSTANCE = new PlayerManager();

    private final Object2ObjectOpenHashMap<UUID, PlayerStatus> PlayerTracker = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent public void onPlayerJoinServer(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerTracker.put(player.getUUID(), new PlayerStatus(player));
    }
    @SubscribeEvent public void onPlayerLeaveServer(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerTracker.remove(player.getUUID());
    }
    @SubscribeEvent public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        PlayerStatus status = PlayerTracker.get(player.getUUID());

        if (status != null) { status.setPlayer(player); }
    }

    public PlayerQuery query() {
        return new PlayerQuery(PlayerTracker.values().stream().toList());
    }

}
