package dev.sable.sablespawner.manager;

import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.blueprint.BlueprintManager;
import dev.sable.sablespawner.manager.datapack.DatapackManager;
import org.slf4j.Logger;

public final class DataManager {

    public static void reloadAll() {
        getLogger().info("开始加载数据包");
        getDatapackManager().reloadDatapack();

        getLogger().info("开始加载蓝图信息");
        getBlueprintManager().reloadBlueprint();
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static DatapackManager getDatapackManager() { return SableSpawner.DATAPACK_MANAGER; }
    private static BlueprintManager getBlueprintManager() {
        return SableSpawner.BLUEPRINT_MANAGER;
    }

}
