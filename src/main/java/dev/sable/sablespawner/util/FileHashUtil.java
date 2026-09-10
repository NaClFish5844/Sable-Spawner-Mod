package dev.sable.sablespawner.util;

import dev.sable.sablespawner.SableSpawner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

public final class FileHashUtil {
    private FileHashUtil() {}

    public static String getFileMD5(Path path) {
        try ( InputStream stream = Files.newInputStream(path) ) {
            return hashStream(stream);
        } catch ( IOException e ) {
            getLogger().error("计算文件 MD5 失败：{}", path, e);
            return null;
        }
    }
    public static String getFileMD5(ResourceLocation resourceLocation) {
        if ( getResourceManager() == null ) {
            getLogger().error("计算资源 MD5 失败：资源管理器尚未就绪");
            return null;
        }

        Optional<Resource> resource = getResourceManager().getResource(resourceLocation);
        if ( resource.isEmpty() ) {
            getLogger().error("计算资源 MD5 失败，资源不存在：{}", resourceLocation);
            return null;
        }
        try ( InputStream stream = resource.get().open() ) {
            return hashStream(stream);
        } catch ( IOException e ) {
            getLogger().error("计算资源 MD5 失败：{}", resourceLocation, e);
            return null;
        }
    }
    public static String getFileMD5(InputStream stream) {
        if ( stream == null ) { return null; }
        return hashStream(stream);
    }

    private static String hashStream(InputStream stream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;
            while ( (read = stream.read(buffer)) != -1 ) {
                digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex( digest.digest() );
        } catch ( IOException | NoSuchAlgorithmException e ) {
            getLogger().error("计算 MD5 失败", e);
            return null;
        }
    }
    private static ResourceManager getResourceManager() {
        return SableSpawner.RESOURCE_MANAGER;
    }
    private static Logger getLogger() {
        return SableSpawner.LOGGER;
    }
}
