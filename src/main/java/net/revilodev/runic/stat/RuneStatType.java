package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuneStatType implements StringRepresentable {
    ATTACK_SPEED("attack_speed", 10, 30, 0.0F),
    ATTACK_DAMAGE("attack_damage", 5, 15, 0.0F),
    ATTACK_RANGE("attack_range", 5, 15, 25.0F),
    MOVEMENT_SPEED("movement_speed", 25, 50, 100.0F),
    SWEEPING_RANGE("sweeping_range", 5, 10, 50.0F),
    DURABILITY("durability", 10, 150, 0.0F),
    RESISTANCE("resistance", 5, 10, 25.0F),
    FIRE_RESISTANCE("fire_resistance", 5, 10, 25.0F),
    BLAST_RESISTANCE("blast_resistance", 5, 10, 25.0F),
    PROJECTILE_RESISTANCE("projectile_resistance", 5, 10, 25.0F),
    KNOCKBACK_RESISTANCE("knockback_resistance", 5, 10, 25.0F),
    MINING_SPEED("mining_speed", 10, 100, 0.0F),
    SWIMMING_SPEED("swimming_speed", 10, 80, 0.0F),
    FALL_REDUCTION("fall_reduction", 10, 85, 300.0F),
    UNDEAD_DAMAGE("undead_damage", 10, 50, 0.0F),
    NETHER_DAMAGE("nether_damage", 10, 50, 0.0F),
    HEALTH("health", 5, 10, 0.0F),
    STUN_CHANCE("stun_chance", 5, 10, 0.0F),
    FLAME_CHANCE("flame_chance", 5, 10, 0.0F),
    BLEEDING_CHANCE("bleeding_chance", 5, 10, 0.0F),
    SHOCKING_CHANCE("shocking_chance", 5, 10, 0.0F),
    POISON_CHANCE("poison_chance", 5, 10, 0.0F),
    WEAKENING_CHANCE("weakening_chance", 5, 10, 0.0F),
    HEALING_EFFICIENCY("healing_efficiency", 10, 50, 0.0F),
    WATER_BREATHING("water_breathing", 50, 150, 0.0F),
    DRAW_SPEED("draw_speed", 10, 50, 0.0F),
    TOUGHNESS("toughness", 5, 25, 25.0F),
    FREEZING_CHANCE("freezing_chance", 5, 10, 0.0F),
    LEECHING_CHANCE("leeching_chance", 10, 25, 50.0F),
    LOOTING("looting", 15, 25, 100.0F),
    BONUS_CHANCE("bonus_chance", 20, 40, 100.0F),
    POWER("power", 5, 20, 0.0F);

    private static final Map<String, RuneStatType> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(RuneStatType::id, t -> t));

    private final String id;
    private final int minPercent;
    private final int maxPercent;
    private final float capPercent;

    RuneStatType(String id, int minPercent, int maxPercent, float capPercent) {
        this.id = id;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
        this.capPercent = capPercent;
    }

    public String id() {
        return this.id;
    }

    public int minPercent() {
        return this.minPercent;
    }

    public int maxPercent() {
        return this.maxPercent;
    }

    public float cap() {
        return this.capPercent;
    }

    public float roll(RandomSource random) {
        if (this.maxPercent <= this.minPercent) {
            return this.minPercent;
        }
        int bound = this.maxPercent - this.minPercent + 1;
        return this.minPercent + random.nextInt(bound);
    }

    public float toMultiplier(float percentValue) {
        return 1.0F + percentValue / 100.0F;
    }

    public static RuneStatType byId(String id) {
        return BY_ID.get(id);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public static Map<RuneStatType, Float> emptyMap() {
        return new EnumMap<>(RuneStatType.class);
    }
}
