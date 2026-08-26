package dev.sable.sablespawner.datapack.blueprint;


import dev.sable.sablespawner.datapack.BlueprintQuery;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class BlueprintManager {
    public static final BlueprintManager INSTANCE = new BlueprintManager();
    public static final BlueprintBuffer BUFFER = BlueprintBuffer.INSTANCE;

    public void loadBlueprints() {

    }

    public BlueprintQuery query() {
        return new BlueprintQuery( getContainer() );
    }

    private BlueprintBuffer getBuffer() { return BlueprintBuffer.INSTANCE; }
    private Object2ObjectOpenHashMap<BlueprintKey, BlueprintEntry> getContainer() { return BlueprintBuffer.getContainer(); }
}
