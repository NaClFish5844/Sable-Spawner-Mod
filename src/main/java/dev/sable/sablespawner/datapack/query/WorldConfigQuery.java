package dev.sable.sablespawner.datapack.query;

import dev.sable.sablespawner.datapack.property.config.DefaultConfig;
import dev.sable.sablespawner.datapack.property.sublevel.AbstractSchematicProperty;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Random;
import java.util.function.Predicate;

public class WorldConfigQuery {
    private final Object2ObjectOpenHashMap<String, DefaultConfig> source;
    private Predicate<AbstractSchematicProperty> keyPredicate = k -> true;
    private Predicate<AbstractSchematicProperty> propertyPredicate = p -> true;

    private final Random RANDOM = new Random();

    public WorldConfigQuery(Object2ObjectOpenHashMap<String, DefaultConfig> source) { this.source = source; }







}

