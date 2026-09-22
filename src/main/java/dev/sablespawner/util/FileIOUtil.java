package dev.sablespawner.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sablespawner.SableSpawner;
import dev.sablespawner.manager.datapack.DatapackSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileIOUtil {

    public static <T> T readFile(Path path, Function<InputStream, T> reader) {
        try ( InputStream stream = Files.newInputStream(path) ) {
            return reader.apply(stream);
        } catch ( IOException e ) {
            getLogger().error("读取文件失败：{} | Failed to read file: {}", path, path, e);
            return null;
        }
    }
    public static <T> T readFile(String path, Function<InputStream, T> reader) {
        try ( InputStream stream = Files.newInputStream( Path.of(path) ) ) {
            return reader.apply(stream);
        } catch ( IOException e ) {
            getLogger().error("读取文件失败：{} | Failed to read file: {}", path, path, e);
            return null;
        }
    }
    public static <T> T readFile(Path root, String validRoot, String filePath, Function<InputStream, T> reader) {
        if ( root.toString().endsWith(".zip") ) {
            return readZipEntry(root, concatPath(validRoot, filePath), reader);
        }
        return readFile( folderPathOf(root, validRoot, filePath), reader );
    }
    public static <T> T readZipEntry(Path zip, String entry, Function<InputStream, T> reader) {
        try ( ZipFile zipFile = openZip(zip) ) {
            ZipEntry zipEntry = findEntry(zipFile, entry);
            if ( zipEntry == null ) {
                getLogger().warn("压缩包内未找到条目：{} -> {} | Entry not found in zip: {} -> {}", zip, entry, zip, entry);
                return null;
            }
            try ( InputStream stream = zipFile.getInputStream(zipEntry) ) {
                return reader.apply(stream);
            }
        } catch ( IOException e ) {
            getLogger().error("读取压缩包条目失败：{} -> {} | Failed to read zip entry: {} -> {}", zip, entry, zip, entry, e);
            return null;
        }
    }
    public static <T> T readDatapackFile(DatapackSource datapack, String filePath, Function<InputStream, T> reader) {
        return readFile(datapack.getRoot(), datapack.getValidRootEntry(), filePath, reader);
    }

    public static byte[] readAsBytes(InputStream stream) {
        try {
            return stream.readAllBytes();
        } catch ( IOException e ) {
            getLogger().error("读取 Stream 失败 | Failed to read stream", e);
            return null;
        }
    }
    public static JsonObject readAsJson(InputStream stream) {
        try {
            return JsonParser.parseReader( new InputStreamReader(stream, StandardCharsets.UTF_8) ).getAsJsonObject();
        } catch ( RuntimeException e ) {
            getLogger().error("读取 JSON 失败 | Failed to parse JSON", e);
            return null;
        }
    }
    public static CompoundTag readAsNbt(InputStream stream) {
        try {
            return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
        } catch ( IOException e ) {
            getLogger().error("读取 NBT 失败 | Failed to read NBT", e);
            return null;
        }
    }


    public static <T> T readDatapackFileStream(DatapackSource datapack, String filePath, Function<InputStream, T> reader) {
        if ( datapack.isZipFile() ) {
            String validRoot = datapack.getValidRootEntry();

            try ( ZipFile zipFile = openZip(datapack.getRoot()) ) {
                ZipEntry entry = findEntry( zipFile, concatPath(validRoot, filePath) );
                if ( entry == null ) {
                    getLogger().warn("压缩包内未找到条目：{} -> {} | Entry not found in zip: {} -> {}", datapack.getRoot(), filePath, datapack.getRoot(), filePath);
                    return null;
                }
                try ( InputStream stream = zipFile.getInputStream(entry) ) {
                    return reader.apply(stream);
                }
            } catch ( IOException e ) {
                getLogger().error("读取压缩包文件失败：{} -> {} | Failed to read zip file: {} -> {}", datapack.getRoot(), filePath, datapack.getRoot(), filePath, e);
                return null;
            }
        }

        Path file = folderPathOf(datapack.getRoot(), datapack.getValidRootEntry(), filePath);
        try ( InputStream stream = Files.newInputStream(file) ) {
            return reader.apply(stream);
        } catch ( IOException e ) {
            getLogger().error("读取数据包文件失败：{} | Failed to read datapack file: {}", file, file, e);
            return null;
        }
    }

    public static Set<String> treeDir(Path path) {
        Set<String> files = new HashSet<>();
        if ( !Files.isDirectory(path) ) { return files; }

        try ( Stream<Path> walk = Files.walk(path) ) {
            walk.filter(Files::isRegularFile)
                    .forEach( p -> files.add( pathToString(path.relativize(p)) ) );
        } catch ( IOException e ) {
            getLogger().error("扫描目录树失败：{} | Failed to walk directory tree: {}", path, path, e);
        }
        return files;
    }
    public static Set<String> listZipEntries(Path zip) {
        Set<String> entryNames = new HashSet<>();
        try ( ZipFile zipFile = openZip(zip) ) {
            zipFile.stream()
                    .map( e -> pathToString(e.getName()) )
                    .forEach(entryNames::add);
        } catch ( IOException e ) {
            getLogger().error("列出压缩包条目失败：{} | Failed to list zip entries: {}", zip, zip, e);
        }
        return entryNames;
    }
    public static Set<String> listZipEntriesOfDirEntry(Path zip, String dirEntry) {
        Set<String> files = new HashSet<>();

        String prefix = dirEntry.isEmpty()
                ? ""
                : ( dirEntry.endsWith("/") ? dirEntry : dirEntry + "/" );

        for ( String name : listZipEntries(zip) ) {
            if ( !name.startsWith(prefix) ) { continue; }
            if ( name.endsWith("/") ) { continue; }

            files.add(name);
        }
        return files;
    }

    public static Set<String> treeDatapackFileDir(DatapackSource datapackSource, String dir) {
        if ( datapackSource.isZipFile() ) {
            String validRoot = datapackSource.getValidRootEntry();
            String prefix = validRoot == null ? "" : validRoot + "/";

            Set<String> files = new HashSet<>();
            for ( String entry : listZipEntriesOfDirEntry(datapackSource.getRoot(), concatPath(validRoot, dir)) ) {
                files.add( entry.substring(prefix.length()) );
            }
            return files;
        }

        Set<String> files = new HashSet<>();
        for ( String name : treeDir( datapackSource.getRoot().resolve(dir) ) ) {
            files.add( dir + "/" + name );
        }
        return files;
    }

    public static String pathToString(String path) {
        if ( path == null || path.isEmpty() ) { return path; }

        String normalized = path.replace('\\', '/');
        while ( normalized.contains("//") ) { normalized = normalized.replace("//", "/"); }
        return normalized;
    }
    public static String pathToString(Path path) {
        if ( path == null ) { return null; }
        return pathToString( path.toString() );
    }

    public static ZipFile openZip(Path zipPath) throws IOException {
        ZipFile utf8 = new ZipFile(zipPath.toFile());
        if ( !hasBrokenNames(utf8) ) { return utf8; }
        utf8.close();

        ZipFile gb = new ZipFile(zipPath.toFile(), Charset.forName("GB18030"));
        if ( !hasBrokenNames(gb) ) { return gb; }
        gb.close();

        return new ZipFile(zipPath.toFile());
    }
    @Nullable public static ZipEntry findEntry(ZipFile zip, String name) {
        ZipEntry entry = zip.getEntry(name);
        if ( entry != null ) { return entry; }

        String target = pathToString(name);
        for ( ZipEntry candidate : zip.stream().toList() ) {
            if ( pathToString(candidate.getName()).equals(target) ) { return candidate; }
        }
        return null;
    }

    private static boolean hasBrokenNames(ZipFile zip) {
        return zip.stream().anyMatch( e -> e.getName().indexOf('\uFFFD') >= 0 );
    }

    private static String concatPath(String validRoot, String path) {
        if ( validRoot == null || validRoot.isEmpty() ) { return path; }
        return validRoot + "/" + path;
    }
    private static Path folderPathOf(Path root, String validRoot, String filePath) {
        if ( validRoot == null || validRoot.isEmpty() ) { return root.resolve(filePath); }
        return root.resolve(validRoot).resolve(filePath);
    }

    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }


}
