package dev.sable.sablespawner.manager.datapack;

import com.google.gson.JsonObject;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.util.FileIOUtil;
import net.neoforged.fml.loading.FMLPaths;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

public final class DatapackScanner {
    public static final List<FileSystem> zipHolder = new ArrayList<>();

    // 此处的所有scan开头的public方法只返回路径
    public static Set<Path> scanDatapackRoots() {
        Set<Path> roots = new HashSet<>();
        Path start = getSableSpawnerDir();

        if ( !Files.isDirectory(start) ) {
            try {
                Files.createDirectories(start);
                getLogger().info("未找到数据包目录，已自动创建：{}", start);
            } catch ( IOException e ) {
                getLogger().error("创建数据包目录失败：{}", start, e);
                return roots;
            }
        }

        try {
            Files.walkFileTree(start, new SimpleFileVisitor<>() {
                @Override @NotNull
                public FileVisitResult preVisitDirectory(Path dir, @NotNull BasicFileAttributes attrs) {
                    if ( dir.equals(start) ) { return FileVisitResult.CONTINUE; }

                    if ( isValidPackRoot(dir) ) {
                        roots.add(dir);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override @NotNull public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) {
                    if ( file.toString().endsWith(".zip") ) { roots.add(file); }
                    return FileVisitResult.CONTINUE;
                }
                @Override @NotNull public  FileVisitResult visitFileFailed(Path file, @NotNull IOException exc) {
                    getLogger().warn("访问文件失败，已跳过：{}", file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch ( IOException e ) {
            getLogger().error("扫描数据包目录失败：{}", start, e);
        }
        return roots;
    }
    public static Set<String> scanRelativeValidRoot(Path zip) {
        Set<String> roots = new HashSet<>();
        Set<String> entries = FileIOUtil.listZipEntries(zip);

        for ( String name : entries ) {
            if ( name.endsWith(".zip") ) {
                getLogger().error("压缩包内不允许嵌套压缩包，跳过整个包：{} -> {}", zip, name);
                return new HashSet<>();
            }
        }

        for ( String name : entries ) {
            if ( !name.endsWith("meta.json") ) { continue; }

            String prefix = name.substring(0, name.length() - "meta.json".length());
            if ( !prefix.isEmpty() && !prefix.endsWith("/") ) { continue; }

            if ( entries.stream().anyMatch( e -> e.startsWith(prefix + "data/") ) ) {
                roots.add( trimTrailingSlash(prefix) );
            }
        }
        return roots;
    }
    @Nullable public static PackMeta readPackMeta(Path root, @Nullable String validRoot) {
        String validRootPath = validRoot == null ? "" : validRoot;
        String metaPath = concatPath(validRootPath, "meta.json");

        if ( root.toString().endsWith(".zip") ) {
            try ( ZipFile zip = new ZipFile(root.toFile()) ) {
                if ( zip.getEntry(metaPath) == null ) {
                    getLogger().warn("数据包缺少 meta.json：{}", root);
                    return null;
                }
            } catch ( IOException e ) {
                getLogger().error("打开压缩包失败：{}", root, e);
                return null;
            }
        } else {
            if ( !Files.isRegularFile( root.resolve(metaPath) ) ) {
                getLogger().warn("数据包缺少 meta.json：{}", root);
                return null;
            }
        }

        JsonObject object = FileIOUtil.readFileAsJsonObject(root, validRootPath, "meta.json");

        if ( object == null ) {
            getLogger().error("读取数据包元信息失败：{}", root);
            return null;
        }
        if ( !DatapackChecker.checkMetaFormat(object) ) {
            getLogger().error("数据包元信息格式非法：{}", root);
            return null;
        }

        return PackMeta.of( object.get("packname").getAsString() );
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

    public static Set<String> scanPropertiesOfPack(DatapackSource datapack) {
        getLogger().info("正在扫描蓝图属性文件");

        return scanFiles(validRoot.resolve("data/properties"), ".json");
    }
    public static Set<String> scanWorldConfigsOfPack(DatapackSource datapack) {
        getLogger().info("正在扫描维度配置文件");

        Set<Path> files = scanFiles(validRoot.resolve("data/worldconfig"), ".json");
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
    public static Set<String> scanBlueprintsOfPack(DatapackSource datapack) {
        getLogger().info("正在扫描包内蓝图文件");

        Set<Path> files = new HashSet<>(scanFiles(validRoot.resolve("data/blueprints"), ".nbt"));
        files.addAll(scanFiles(validRoot.resolve("data/schematics"), ".nbt"));
        return files;
    }

    @Nullable public static String getBlueprintInValidRoot(String schematicPath, Path validRoot) {
        String packPrefix = validRoot.getFileName().toString() + "/";
        String relative = schematicPath.startsWith(packPrefix)
                ? schematicPath.substring(packPrefix.length())
                : schematicPath;

        Path target = validRoot.resolve(relative);
        if ( Files.isRegularFile(target) ) { return target; }

        getLogger().warn("此位置未找到蓝图：{}", schematicPath);
        return null;
    }


    private static boolean isValidPackRoot(Path path) {
        return Files.isDirectory(path.resolve("data")) && Files.isRegularFile(path.resolve("meta.json"));
    }
    private static String concatPath(String validRoot, String path) {
        return validRoot.isEmpty() ? path : validRoot + "/" + path;
    }
    private static String trimTrailingSlash(String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
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
