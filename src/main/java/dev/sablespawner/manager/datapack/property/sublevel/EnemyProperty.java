package dev.sablespawner.manager.datapack.property.sublevel;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Getter @Setter
public class EnemyProperty extends AbstractSchematicProperty {

    @Nullable private ArrayList<Integer> availableWorldLevel;
    @Nullable private ArrayList<String> availableDimension;

    private boolean naturalSpawn;
    private int weight;

    private int minSpawnDistance;
    private int maxSpawnDistance;

    private int minSpawnInterval;
    private int maxSpawnInterval;

    private int maxSpawnAmount;

    private int destroyThreshold;

    private int lifeTime;

    @SerializedName("ftl_charge_threshold")
    private int FTLChargeThreshold;
    @SerializedName("ftl_charge_duration")
    private int FTLChargeDuration;

    private int value;

    private EnemyProperty() {
        super();
        setSublevelType(SublevelType.enemy);

        this.availableWorldLevel = new ArrayList<>();
        this.availableDimension = new ArrayList<>();
        this.naturalSpawn = false;
        this.weight = 0;
        this.minSpawnDistance = -1;
        this.maxSpawnDistance = -1;
        this.minSpawnInterval = -1;
        this.maxSpawnInterval = -1;
        this.maxSpawnAmount = 1;
        this.destroyThreshold = -1;
        this.lifeTime = -1;
        this.FTLChargeThreshold = -1;
        this.FTLChargeDuration = -1;
        this.value = 0;
    }

    public static EnemyProperty ofBasic(AbstractSchematicProperty base) {
        EnemyProperty prop = new EnemyProperty();
        prop.copyBaseFrom(base);
        return prop;
    }
    public static EnemyProperty ofDefault() {
        return new EnemyProperty();
    }
}
