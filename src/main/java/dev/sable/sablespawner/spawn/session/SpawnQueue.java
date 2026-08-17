package dev.sable.sablespawner.spawn.session;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.datapack.DatapackManager;
import dev.sable.sablespawner.datapack.property.AbstractSchematicProperty;
import dev.sable.sablespawner.datapack.property.EnemyProperty;
import dev.sable.sablespawner.player.PlayerManager;
import dev.sable.sablespawner.player.PlayerStatus;
import dev.sable.sablespawner.spawn.session.entry.SpawnTicket;
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
        EnemyProperty property = selectEnemy(playerUUID);
        if ( property == null ) { return; }

        SpawnTicket ticket = new SpawnTicket( property, playerUUID );
        this.queue.put( playerUUID, ticket );
    }
    @Nullable public SpawnTicket pop(UUID playerUUID) {
        return this.queue.remove( playerUUID );
    }
    @Nullable public SpawnTicket pop(ServerPlayer player) {
        return this.queue.remove( player.getUUID() );
    }

    @Nullable private EnemyProperty selectEnemy(UUID playerUUID) {
        Object2ObjectOpenHashMap<UUID, PlayerStatus> hashMap = getPlayerManager().query().ofUUID(playerUUID).collect();
        if ( hashMap.isEmpty() ) { return null; }

        int playerScoreLevel = hashMap.values().iterator().next().getScoreLevel();

        AbstractSchematicProperty picked = getDatapackManager().query()
                .isEnemy()
                .isNaturalSpawn()
                .ofDimension(LEVEL)
                .ofWorldLevel(playerScoreLevel)
                .pickEnemy();
        return picked instanceof EnemyProperty enemy ? enemy : null;
    }

    private long getGameTime() { return SableSpawner.SERVER.overworld().getGameTime(); }
    private DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private PlayerManager getPlayerManager() { return SableSpawner.PLAYER_MANAGER; }
}
