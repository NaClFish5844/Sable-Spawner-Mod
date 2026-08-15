package dev.sable.sablespawner.spawn.session;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sable.sablespawner.spawn.session.entry.EnemySubLevelEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.UUID;

@Getter
public class EnemySubLevelTracker {
    private final Object2ObjectOpenHashMap<UUID, EnemySubLevelEntry> entries = new Object2ObjectOpenHashMap<>();

    public void push(EnemySubLevelEntry entry) {
        this.entries.put( entry.getUuid(), entry);
    }

    public EnemySubLevelEntry pop( UUID uuid ) {
        return this.entries.remove(uuid);
    }
    public EnemySubLevelEntry pop( ServerSubLevel subLevel ) {
        return pop(subLevel.getUniqueId());
    }

}
