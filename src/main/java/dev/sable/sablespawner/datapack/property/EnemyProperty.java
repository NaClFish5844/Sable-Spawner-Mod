package dev.sable.sablespawner.datapack.property;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Getter @Setter
public class EnemyProperty extends AbstractSchematicProperty {

    @Nullable private ArrayList<Integer> availableWorldLevel = new ArrayList<>();
    @Nullable private ArrayList<String> availableDimension = new ArrayList<>();

    private boolean naturalSpawn = false;
    private int weight = 0;

    private int minSpawnDistance = -1;
    private int maxSpawnDistance = -1;

    private int minSpawnInterval = -1;
    private int maxSpawnInterval = -1;

    private int maxSpawnAmount = 1;

    private int destroyThreshold = -1;

    private int lifeTime = -1;

    @SerializedName("ftl_charge_threshold")
    private int FTLChargeThreshold = -1;
    @SerializedName("ftl_charge_duration")
    private int FTLChargeDuration = -1;

    private int value = 0;
}
