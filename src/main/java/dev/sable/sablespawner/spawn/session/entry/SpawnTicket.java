package dev.sable.sablespawner.spawn.session.entry;

import dev.sable.sablespawner.manager.datapack.property.sublevel.EnemyProperty;
import dev.sable.sablespawner.manager.datapack.property.sublevel.PropertyKey;
import dev.sable.sablespawner.player.PlayerStatus;

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
