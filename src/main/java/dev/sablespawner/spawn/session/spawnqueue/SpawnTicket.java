package dev.sablespawner.spawn.session.spawnqueue;

import dev.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sablespawner.player.PlayerStatus;

import java.util.UUID;

public record SpawnTicket(
        PropertyKey propertyKey,
        EnemyProperty property,
        UUID targetPlayer,
        int amount,
        double distanceFromTarget,
        long spawnDelay
) {

    public long getScheduledSpawnTime( PlayerStatus playerStatus ) {
        return playerStatus.getOutProtectionTime() + this.spawnDelay;
    }

}
