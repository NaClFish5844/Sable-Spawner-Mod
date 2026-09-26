package dev.sablespawner.spawn.session.tracker.entry;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.sablespawner.manager.datapack.property.sublevel.AllyProperty;
import lombok.Getter;

@Getter
public class AllySubLevelEntry extends SubLevelEntry {
    private final AllyProperty property;

    // 预留区：owner（所属玩家 / 团队）、任务状态、编队引用、跟随目标……

    public AllySubLevelEntry(AllyProperty property, ServerSubLevel subLevel) {
        super(subLevel);
        this.property = property;
        this.initialized = this.initialize();
    }

    public boolean initialize() {
        return true;
    }

    public boolean isExpired() {
        return false;
    }


}
