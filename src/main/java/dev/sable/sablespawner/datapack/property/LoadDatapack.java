package dev.sable.sablespawner.datapack.property;

import com.google.gson.JsonObject;

import java.nio.file.Path;

public class LoadDatapack {
    public boolean load(Path datapackPath) {
        //一个蓝图可以支持多个SublevelType 记得检查
        return false;
    }

    public boolean loadAlly(JsonObject json) {
        return false;
    }

    public boolean loadEnemy(JsonObject json) {
        return false;
    }

    public boolean loadPrefabricated(JsonObject json) {
        return false;
    }
}
