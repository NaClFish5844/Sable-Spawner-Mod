package dev.sable.sablespawner.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sable.sablespawner.SableSpawner;
import dev.sable.sablespawner.manager.datapack.DatapackSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileIOUtil {
    public static byte[] readFileAsBytes(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch ( IOException e ) {
            getLogger().error("读取文件失败：{}", path, e);
            return null;
        }
    }
    public static JsonObject readFileAsJsonObject(Path path) {
        try ( InputStream stream = Files.newInputStream(path) ) {
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
        } catch ( IOException | RuntimeException e ) {
            getLogger().error("读取 JSON 文件失败：{}", path, e);
            return null;
        }
    }
    public static CompoundTag readFileAsCompoundTag(Path path) {
        try {
            return NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
        } catch ( IOException e ) {
            getLogger().error("读取 NBT 文件失败：{}", path, e);
            return null;
        }
    }

    public static byte[] readFileAsBytes(Path root, String validRoot, String filePath) {
        if ( root.toString().endsWith(".zip") ) {
            return readZipEntryAsBytes(root, concatPath(validRoot, filePath));
        }
        return readFileAsBytes( root.resolve(validRoot).resolve(filePath) );
    }
    public static JsonObject readFileAsJsonObject(Path root, String validRoot, String filePath) {
        if ( root.toString().endsWith(".zip") ) {
            return readZipEntryAsJsonObject(root, concatPath(validRoot, filePath));
        }
        return readFileAsJsonObject( root.resolve(validRoot).resolve(filePath) );
    }
    public static CompoundTag readFileAsCompoundTag(Path root, String validRoot, String filePath) {
        if ( root.toString().endsWith(".zip") ) {
            return readZipEntryAsCompoundTag(root, concatPath(validRoot, filePath));
        }
        return readFileAsCompoundTag( root.resolve(validRoot).resolve(filePath) );
    }

    public static byte[] readZipEntryAsBytes(Path zip, String entry) {
        try ( ZipFile zipFile = new ZipFile(zip.toFile()) ) {
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if ( zipEntry == null ) {
                getLogger().warn("压缩包内未找到条目：{} -> {}", zip, entry);
                return null;
            }
            try ( InputStream stream = zipFile.getInputStream(zipEntry) ) {
                return stream.readAllBytes();
            }
        } catch ( IOException e ) {
            getLogger().error("读取压缩包条目失败：{} -> {}", zip, entry, e);
            return null;
        }
    }
    public static JsonObject readZipEntryAsJsonObject(Path zip, String entry) {
        try ( ZipFile zipFile = new ZipFile(zip.toFile()) ) {
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if ( zipEntry == null ) {
                getLogger().warn("压缩包内未找到条目：{} -> {}", zip, entry);
                return null;
            }
            try ( InputStream stream = zipFile.getInputStream(zipEntry) ) {
                return JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                ).getAsJsonObject();
            }
        } catch ( IOException | RuntimeException e ) {
            getLogger().error("读取压缩包 JSON 文件失败：{} -> {}", zip, entry, e);
            return null;
        }
    }
    public static CompoundTag readZipEntryAsCompoundTag(Path zip, String entry) {
        try ( ZipFile zipFile = new ZipFile(zip.toFile()) ) {
            ZipEntry zipEntry = zipFile.getEntry(entry);
            if ( zipEntry == null ) {
                getLogger().warn("压缩包内未找到条目：{} -> {}", zip, entry);
                return null;
            }
            try ( InputStream stream = zipFile.getInputStream(zipEntry) ) {
                return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            }
        } catch ( IOException e ) {
            getLogger().error("读取压缩包 NBT 文件失败：{} -> {}", zip, entry, e);
            return null;
        }
    }

    public static byte[] readDatapackFileAsBytes(DatapackSource datapackSource, String filePath) {
        return readFileAsBytes(datapackSource.getRoot(), datapackSource.getRelativeValidRoot(), filePath);
    }
    public static JsonObject readDatapackFileAsJsonObject(DatapackSource datapackSource, String filePath) {
        return readFileAsJsonObject(datapackSource.getRoot(), datapackSource.getRelativeValidRoot(), filePath);
    }
    public static CompoundTag readDatapackFileAsCompoundTag(DatapackSource datapackSource, String filePath) {
        return readFileAsCompoundTag(datapackSource.getRoot(), datapackSource.getRelativeValidRoot(), filePath);
    }

    public static Set<String> listZipEntries(Path zip) {
        Set<String> entryNames = new HashSet<>();
        try ( ZipFile zipFile = new ZipFile(zip.toFile()) ) {
            zipFile.stream()
                    .map(ZipEntry::getName)
                    .forEach(entryNames::add);
        } catch ( IOException e ) {
            getLogger().error("列出压缩包条目失败：{}", zip, e);
        }
        return entryNames;
    }

    private static String concatPath(String validRoot, String path) {
        return validRoot.isEmpty() ? path : validRoot + "/" + path;
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
}
