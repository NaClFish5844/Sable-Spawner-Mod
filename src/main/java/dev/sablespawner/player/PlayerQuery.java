package dev.sablespawner.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerQuery {
    private final Object2ObjectOpenHashMap<UUID, PlayerStatus> source;
    private Predicate<PlayerStatus> statPredicate = s -> true;

    public PlayerQuery(Object2ObjectOpenHashMap<UUID, PlayerStatus> source) { this.source = source; }

    public PlayerQuery ofUUID(UUID uuid) {
        statPredicate = statPredicate.and(s -> s.getPlayer().getUUID().equals(uuid) );
        return this;
    }
    public PlayerQuery inLevel(ServerLevel level) {
        statPredicate = statPredicate.and(s -> s.getPlayer().level() == level);
        return this;
    }
    public PlayerQuery isProtected() {
        statPredicate = statPredicate.and(PlayerStatus::isInProtection);
        return this;
    }
    public PlayerQuery notProtected() {
        statPredicate = statPredicate.and(s -> !s.isInProtection());
        return this;
    }

    public Object2ObjectOpenHashMap<UUID, PlayerStatus> collect() {
        Object2ObjectOpenHashMap<UUID, PlayerStatus> result = new Object2ObjectOpenHashMap<>();
        for ( Map.Entry<UUID, PlayerStatus> entry : source.entrySet() ) {
            PlayerStatus status = entry.getValue();
            if (statPredicate.test(status)) { result.put( entry.getKey(), entry.getValue() ); }
        }
        return result;
    }
}
