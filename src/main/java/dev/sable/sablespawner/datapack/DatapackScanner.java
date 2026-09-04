package dev.sable.sablespawner.datapack;

import dev.sable.sablespawner.SableSpawner;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class DatapackScanner {
    public static final List<FileSystem> zipHolder = new ArrayList<>();

    public static Set<Path> scanDatapacks() {
        Set<Path> packRoots = new HashSet<>();
        Path root = getSableSpawnerDir();
        if ( !Files.isDirectory(root) ) {
            try {
                Files.createDirectories(root);
                getLogger().info("未找到数据包目录，已自动创建：{}", root);
            } catch ( IOException e ) {
                getLogger().error("创建数据包目录失败：{}", root, e);
                return packRoots;
            }
        }

        try ( Stream<Path> entries = Files.list(root) ) {
            entries.forEach(entry -> packRoots.addAll(findValidPackRoots(entry)));
        } catch ( IOException e ) {
            getLogger().error("扫描数据包目录失败：{}", root, e);
        }
        return packRoots;
    }

    @Nullable public static Path getDefaultConfig() {
        Path root = getSableSpawnerDir();
        Path target = root.resolve("default.json");
        if ( Files.isRegularFile(target) ) { return target; }

        try {
            Files.createDirectories(root);
            try ( InputStream source = SableSpawner.class.getResourceAsStream("/assets/sablespawner/defaultconfig/default.json") ) {
                if ( source == null ) {
                    getLogger().error("mod 内置 default.json 缺失（assets/sablespawner/defaultconfig/default.json）");
                    return null;
                }
                Files.copy(source, target);
                getLogger().info("已从 mod 内置资源复制 default.json 到 {}", target);
            }
            return target;
        } catch ( IOException e ) {
            getLogger().error("初始化 default.json 失败", e);
            return null;
        }
    }

    @Nullable public static Path getMetaJsonOfPack(Path packDir) {
        Path meta = packDir.resolve("meta.json");
        if ( Files.isRegularFile(meta) ) { return meta; }
        getLogger().warn("数据包缺少 meta.json：{}", packDir);
        return null;
    }

    public static Set<Path> scanPropertyOfPack(Path packDir) {
        getLogger().info("正在扫描蓝图属性文件");

        return scanFiles(packDir.resolve("data/properties"), ".json");
    }
    public static Set<Path> scanWorldConfigOfPack(Path packDir) {
        getLogger().info("正在扫描维度配置文件");

        Set<Path> files = scanFiles(packDir.resolve("data/worldconfig"), ".json");
        Set<Path> defaultConfigs = new HashSet<>();

        for ( Path p : files ) {
            if ( p.getFileName().toString().equals("default.json") ) { defaultConfigs.add(p); }
        }
        if ( !defaultConfigs.isEmpty() ) {
            getLogger().warn("请勿在数据包中加入 default.json");
            for ( Path d : defaultConfigs ) { files.remove( d ); }
        }

        return files;
    }
    public static Set<Path> scanBlueprintOfPack(Path packDir) {
        getLogger().info("正在扫描包内蓝图文件");

        Set<Path> files = new HashSet<>(scanFiles(packDir.resolve("data/blueprints"), ".nbt"));
        files.addAll(scanFiles(packDir.resolve("data/schematics"), ".nbt"));
        return files;
    }

    private static Set<Path> findValidPackRoots(Path entry) {
        Set<Path> roots = new HashSet<>();
        if ( Files.isDirectory(entry) ) {
            walkForRoots(entry, roots);
            return roots;
        }
        if ( Files.isRegularFile(entry) && entry.toString().endsWith(".zip") ) {
            try {
                FileSystem zipFileSystem = FileSystems.newFileSystem(entry, Map.of());
                zipHolder.add(zipFileSystem);
                for ( Path root : zipFileSystem.getRootDirectories() ) {
                    walkForRoots(root, roots);
                }
            } catch ( IOException e ) {
                getLogger().error("打开数据包压缩包失败：{}", entry, e);
            }
        }
        return roots;
    }
    private static void walkForRoots(Path start, Set<Path> roots) {
        try ( Stream<Path> walk = Files.walk(start, 3) ) {
            walk.filter(DatapackScanner::isValidPackRoot).forEach(roots::add);
        } catch ( IOException e ) {
            getLogger().error("在此目录中未发现有效文件：{}", start, e);
        }
    }
    private static boolean isValidPackRoot(Path path) {
        return Files.isDirectory(path.resolve("data")) && Files.isRegularFile(path.resolve("meta.json"));
    }
    private static Set<Path> scanFiles(Path directory, String suffix) {
        Set<Path> files = new HashSet<>();
        if ( !Files.isDirectory(directory) ) { return files; }

        try ( Stream<Path> stream = Files.list(directory) ) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .forEach(files::add);
        } catch ( IOException e ) {
            getLogger().error("扫描目录失败：{}", directory, e);
        }

        return files;
    }


    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
    private static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
    private static Path getSableSpawnerDir() {
        return getGameDir().resolve("sablespawner");
    }
}
