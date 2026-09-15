package dev.sable.sablespawner.spawn.session;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import dev.sable.sablespawner.manager.datapack.property.config.WorldConfig;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sable.sablespawner.player.PlayerManager;
import dev.sable.sablespawner.player.PlayerStatus;
import dev.sable.sablespawner.spawn.session.entry.SpawnTicket;
import dev.sable.sablespawner.spawn.session.entry.SpawnTicketBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

@Getter
public class SpawnQueue {
    private final ServerLevel LEVEL;
    private final Object2ObjectOpenHashMap<UUID, SpawnTicket> queue = new Object2ObjectOpenHashMap<>();

    public SpawnQueue(ServerLevel level) {
        this.LEVEL = level;
    }

    public void updateQueue() {
        Object2ObjectOpenHashMap<UUID, PlayerStatus> allPlayers = getPlayerManager().query()
                .inLevel(LEVEL)
                .collect();

        for ( PlayerStatus playerStat : allPlayers.values() ) {
            ServerPlayer player = playerStat.getPlayer();
            UUID playerUUID = player.getUUID();
            boolean inQueue = this.queue.containsKey(playerUUID);
            boolean inLevel = this.LEVEL.getPlayers(p -> true).contains(player);

            if ( inQueue && inLevel ) { continue; }
            if ( !inQueue && inLevel ) { push(playerUUID); }
            if ( !inLevel ) { pop(playerUUID); }
        }
    }

    private void push(UUID playerUUID) {
        PropertyKey propertyKey = selectEnemy(playerUUID);
        if ( propertyKey == null ) { return; }

        SpawnTicket ticket = SpawnTicketBuilder.of( propertyKey, playerUUID );
        this.queue.put( playerUUID, ticket );
    }
    @Nullable public SpawnTicket pop(UUID playerUUID) {
        return this.queue.remove( playerUUID );
    }
    @Nullable public SpawnTicket pop(ServerPlayer player) {
        return this.queue.remove( player.getUUID() );
    }

    @Nullable private PropertyKey selectEnemy(UUID playerUUID) {
        Object2ObjectOpenHashMap<UUID, PlayerStatus> player =
                getPlayerManager().query()
                        .ofUUID(playerUUID)
                        .collect();
        if ( player.isEmpty() ) { return null; }

        int playerScore = player.values().iterator().next().getScore();
        int playerScoreLevel = getScoreLevel(playerScore);

        return getDatapackManager().propertyQuery()
                .isEnemy()
                .isNaturalSpawn()
                .ofDimension(LEVEL)
                .ofWorldLevel(playerScoreLevel)
                .pickEnemy();
    }

    private int getScoreLevel(int score) {
        WorldConfig worldConfig = getDatapackManager().worldConfigQuery()
                .ofDimension(LEVEL)
                .collect();

        if ( worldConfig == null ) { return -1; }
        return worldConfig.getScoreLevel(score);
    }
    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }
}
