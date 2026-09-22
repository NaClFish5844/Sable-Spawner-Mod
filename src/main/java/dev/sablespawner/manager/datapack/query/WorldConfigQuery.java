package dev.sablespawner.manager.datapack.query;

import dev.sablespawner.manager.datapack.property.config.DefaultConfig;
import dev.sablespawner.manager.datapack.property.config.WorldConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class WorldConfigQuery {
    private final ObjectList<WorldConfig> source;
    private final DefaultConfig defaultConfig;
    private Predicate<WorldConfig> Predicate = cfg -> true;

    public WorldConfigQuery(Object2ObjectOpenHashMap<String, DefaultConfig> source) {
        ObjectList<WorldConfig> worldConfig = new ObjectArrayList<>();

        for ( Map.Entry<String, DefaultConfig> entry : source.entrySet() ) {
            if ( entry.getValue() instanceof WorldConfig cfg ) {
                worldConfig.add(cfg);
            }
        }

        this.source = worldConfig;
        this.defaultConfig = source.get("default");
    }

    public WorldConfigQuery ofDimension(Level level) {
        String targetDim = level.dimension().location().toString();
        Predicate = Predicate.and(cfg -> Objects.equals(cfg.getDimension(), targetDim) );
        return this;
    }
    public WorldConfigQuery ofDimension(String dimension) {
        Predicate = Predicate.and(cfg -> Objects.equals(cfg.getDimension(), dimension) );
        return this;
    }

    @Nullable public WorldConfig collect() {
        for ( WorldConfig config : source ) {
            if ( Predicate.test(config) ) {
                return config;
            }
        }
        return null;
    }

    @Nullable public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }





}

