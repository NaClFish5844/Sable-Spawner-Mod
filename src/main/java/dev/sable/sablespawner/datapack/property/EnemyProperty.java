package dev.sable.sablespawner.datapack.property;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Getter @Setter
public class EnemyProperty extends AbstractSchematicProperty {

    @Nullable public ArrayList<Integer> availableWorldLevel = new ArrayList<>();
    @Nullable public ArrayList<String> availableDimension = new ArrayList<>();

    public boolean naturalSpawn = false;

    public int minSpawnDistance = -1;
    public int maxSpawnDistance = -1;

    public int minSpawnInterval = -1;
    public int maxSpawnInterval = -1;

    public int spawnAmount = -1;

    public int destroyThreshold = -1;

    public int lifeTime = -1;

    @SerializedName("ftl_charge_threshold")
    public int FTLChargeThreshold = -1;
    @SerializedName("ftl_charge_duration")
    public int FTLChargeDuration = -1;

    public int value = -1;
}
