package dev.sablespawner.manager;

import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.blueprint.BlueprintManager;
import dev.sablespawner.manager.datapack.DatapackManager;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DataManager {

    public static void initSideLoadedFiles() {
        Path root = getSableSpawnerDir();

        if ( !Files.isDirectory(root) ) {
            try {
                Files.createDirectories(root);
                getLogger().info("未找到数据包目录，已自动创建：{} | Datapack directory not found, created: {}", root, root);
            } catch ( IOException e ) {
                getLogger().error("创建数据包目录失败：{} | Failed to create datapack directory: {}", root, root, e);
                return;
            }
        }

        Path defaultConfig = root.resolve("default.json");
        if ( Files.isRegularFile(defaultConfig) ) { return; }

        try {
            Files.createDirectories(root);
            try ( InputStream source = SableSpawner.class.getResourceAsStream("/assets/sablespawner/defaultconfig/default.json") ) {
                if ( source == null ) {
                    getLogger().error("mod 内置 default.json 缺失（assets/sablespawner/defaultconfig/default.json） | Built-in default.json missing (assets/sablespawner/defaultconfig/default.json)");
                    return;
                }
                Files.copy(source, defaultConfig);
                getLogger().info("已从 mod 内置资源复制 default.json 到 {} | Copied built-in default.json to {}", defaultConfig, defaultConfig);
            }
        } catch ( IOException e ) {
            getLogger().error("初始化 default.json 失败 | Failed to initialize default.json", e);
        }
    }

    public static void reloadAll() {
        getLogger().info("开始加载数据包 | Loading datapacks");
        getDatapackManager().reloadDatapack();

        getLogger().info("开始加载蓝图 | Loading blueprints");
        getBlueprintManager().reloadBlueprint();
    }

    public static int getDatapackAmount() {
        return getDatapackManager().getDATAPACK_REGISTRY().size();
    }
    public static int getBlueprintAmount() {
        return getBlueprintManager().getBLUEPRINT_REGISTRY().size();
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static DatapackManager getDatapackManager() {
        return SableSpawner.DATAPACK_MANAGER;
    }
    private static BlueprintManager getBlueprintManager() {
        return SableSpawner.BLUEPRINT_MANAGER;
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
    private static Path getSableSpawnerDir() {
        return getGameDir().resolve("sablespawner");
    }

}
