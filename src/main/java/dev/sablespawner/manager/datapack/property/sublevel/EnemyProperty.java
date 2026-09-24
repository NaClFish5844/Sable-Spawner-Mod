package dev.sablespawner.manager.datapack.property.sublevel;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.Pair;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;

@Setter
public class EnemyProperty extends AbstractSchematicProperty {

    // query
    @Getter @Nullable private ArrayList<Integer> availableWorldLevel;
    @Getter @Nullable private ArrayList<String> availableDimension;

    @Getter private boolean naturalSpawn;
    @Getter private int weight;

    // ticket building
    private int minSpawnDistance;
    private int maxSpawnDistance;

    private int minSpawnInterval;
    private int maxSpawnInterval;

    private int maxSpawnAmount;

    @Nullable public Pair<Integer, Integer> getSpawnDistanceRange() {
        return getSafeRange(minSpawnDistance, maxSpawnDistance);
    }
    @Nullable public Pair<Integer, Integer> getSpawnIntervalRange() {
        return getSafeRange(minSpawnInterval, maxSpawnInterval);
    }
    public int getMaxSpawnAmount() {
        return getSafeValue(this.maxSpawnAmount);
    }

    // runtime status
    private int destroyThreshold;

    private int lifeTime;

    private int value;

    @SerializedName("ftl_charge_threshold") private int FTLChargeThreshold;
    @SerializedName("ftl_charge_duration") private int FTLChargeDuration;

    public int getDestroyThreshold() {
        return getSafeValue(this.destroyThreshold);
    }
    public int getLifeTime() {
        return getSafeValue(this.lifeTime);
    }
    public int getFTLChargeThreshold() {
        return getSafeValue(this.FTLChargeThreshold);
    }
    public int getFTLChargeDuration() {
        return getSafeValue(this.FTLChargeDuration);
    }
    public int getValue() {
        return Math.max(0, this.value);
    }


    private int getSafeValue(int number) {
        boolean valid = number > 0 && number < Integer.MAX_VALUE;
        return valid ? number : -1;
    }
    @Nullable private Pair<Integer, Integer> getSafeRange(int a, int b) {
        int upper = Math.max(a, b);
        int lower = Math.min(a, b);

        boolean maxValid = upper > 0 && upper < Integer.MAX_VALUE;
        boolean minValid = lower > 0 && lower < Integer.MAX_VALUE;

        if (!minValid && !maxValid) { return null; }

        if (minValid && maxValid) {
            if (lower == upper) { return Pair.of(lower, lower + 1); }
            return Pair.of(lower, upper);
        }

        if (minValid) { return Pair.of(lower, lower + 1); }

        return Pair.of(0, upper);

    }

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
