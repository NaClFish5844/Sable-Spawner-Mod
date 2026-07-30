package dev.sable.sablespawner.datapack.property;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Getter @Setter
public class EnemyProperty extends AbstractSchematicProperty {

    @Nullable public ArrayList<Integer> availableWorldLevel = new ArrayList<>();

    public boolean naturalSpawn = false;

    public int minSpawnDistance = -1;
    public int maxSpawnDistance = -1;

    public int minSpawnInterval = -1;
    public int maxSpawnInterval = -1;

    public int spawnAmount = -1;

    public int FTLChargeThreshold = -1;
    public int FTLChargeDuration = -1;

    public int value = -1;
}
