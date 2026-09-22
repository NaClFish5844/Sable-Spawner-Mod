package dev.sablespawner.manager.datapack;

import com.google.gson.JsonObject;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.util.FileIOUtil;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipFile;

public final class DatapackScanner {
    // 此处的所有scan开头的public方法只返回路径
    // 此处所有方法只应该在DatapackLoader中使用
    public static Set<Path> scanDatapackRoots() {
        Set<Path> roots = new HashSet<>();
        Path start = getSableSpawnerDir();

        if ( !Files.isDirectory(start) ) {
            try {
                Files.createDirectories(start);
                getLogger().info("未找到数据包目录，已自动创建：{} | Datapack directory not found, created: {}", start, start);
            } catch ( IOException e ) {
                getLogger().error("创建数据包目录失败：{} | Failed to create datapack directory: {}", start, start, e);
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
                    getLogger().warn("访问文件失败，已跳过：{} | Failed to visit file, skipped: {}", file, file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch ( IOException e ) {
            getLogger().error("扫描数据包目录失败：{} | Failed to scan datapack directory: {}", start, start, e);
        }
        return roots;
    }
    public static Set<String> scanRelativeValidRoot(Path zip) {
        Set<String> roots = new HashSet<>();
        Set<String> entries = FileIOUtil.listZipEntries(zip);

        for ( String name : entries ) {
            if ( name.endsWith(".zip") ) {
                getLogger().error("压缩包内不允许嵌套压缩包，跳过整个包：{} -> {} | Nested zip is not allowed, skipping entire pack: {} -> {}", zip, name, zip, name);
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
            try ( ZipFile zip = FileIOUtil.openZip(root) ) {
                if ( FileIOUtil.findEntry(zip, metaPath) == null ) {
                    getLogger().warn("数据包缺少 meta.json：{} | Datapack missing meta.json: {}", root, root);
                    return null;
                }
            } catch ( IOException e ) {
                getLogger().error("打开压缩包失败：{} | Failed to open zip: {}", root, root, e);
                return null;
            }
        } else {
            if ( !Files.isRegularFile( root.resolve(metaPath) ) ) {
                getLogger().warn("数据包缺少 meta.json：{} | Datapack missing meta.json: {}", root, root);
                return null;
            }
        }

        JsonObject object = FileIOUtil.readFile(root, validRootPath, "meta.json", FileIOUtil::readAsJson);

        if ( object == null ) {
            getLogger().error("读取数据包元信息失败：{} | Failed to read pack meta: {}", root, root);
            return null;
        }
        if ( !DatapackChecker.checkMetaFormat(object) ) {
            getLogger().error("数据包元信息格式非法：{} | Invalid pack meta format: {}", root, root);
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
                    getLogger().error("mod 内置 default.json 缺失（assets/sablespawner/defaultconfig/default.json） | Built-in default.json missing (assets/sablespawner/defaultconfig/default.json)");
                    return null;
                }
                Files.copy(source, target);
                getLogger().info("已从 mod 内置资源复制 default.json 到 {} | Copied built-in default.json to {}", target, target);
            }
            return target;
        } catch ( IOException e ) {
            getLogger().error("初始化 default.json 失败 | Failed to initialize default.json", e);
            return null;
        }
    }

    public static Set<String> scanPropertiesOfPack(DatapackSource datapack) {
        getLogger().debug("正在扫描蓝图属性文件 | Scanning property files");

        return FileIOUtil.treeDatapackFileDir(datapack, "data/properties");
    }
    public static Set<String> scanWorldConfigsOfPack(DatapackSource datapack) {
        getLogger().debug("正在扫描维度配置文件 | Scanning worldconfig files");

        return FileIOUtil.treeDatapackFileDir(datapack, "data/worldconfig");
    }

    private static boolean isValidPackRoot(Path path) {
        return Files.isDirectory(path.resolve("data")) && Files.isRegularFile(path.resolve("meta.json"));
    }
    private static String concatPath(String validRoot, String path) {
        if ( validRoot == null || validRoot.isEmpty() ) { return path; }
        return validRoot + "/" + path;
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
