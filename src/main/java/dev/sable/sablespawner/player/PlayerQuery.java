package dev.sable.sablespawner.player;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerQuery {
    private final Collection<PlayerStatus> source;
    private Predicate<PlayerStatus> predicate = s -> true;

    public PlayerQuery(Collection<PlayerStatus> source) { this.source = source; }

    public PlayerQuery inLevel(ServerLevel level) {
        predicate = predicate.and(s -> s.getPlayer().level() == level);
        return this;
    }
    public PlayerQuery isProtected() {
        predicate = predicate.and(PlayerStatus::isInProtection);
        return this;
    }
    public PlayerQuery notProtected() {
        predicate = predicate.and(s -> !s.isInProtection());
        return this;
    }
    public PlayerQuery ofScoreLevel(int level) {
        predicate = predicate.and(s -> s.getScoreLevel() == level);
        return this;
    }

    public Object2ObjectOpenHashMap<UUID, PlayerStatus> collect() {
        Object2ObjectOpenHashMap<UUID, PlayerStatus> result = new Object2ObjectOpenHashMap<>();
        for (PlayerStatus s : source) {
            if (predicate.test(s)) { result.put(s.getPlayer().getUUID(), s); }
        }
        return result;
    }
}
