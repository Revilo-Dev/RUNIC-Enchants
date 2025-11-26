package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuneStatType implements StringRepresentable {
    ATTACK_SPEED("attack_speed", 10, 50),
    ATTACK_DAMAGE("attack_damage", 25, 125),
    ATTACK_RANGE("attack_range", 10, 50),
    MOVEMENT_SPEED("movement_speed", 25, 100),
    SWEEPING_RANGE("sweeping_range", 20, 120),
    DURABILITY("durability", 10, 150),
    RESISTANCE("resistance", 10, 100),
    FIRE_RESISTANCE("fire_resistance", 10, 100),
    BLAST_RESISTANCE("blast_resistance", 10, 100),
    PROJECTILE_RESISTANCE("projectile_resistance", 10, 100),
    KNOCKBACK_RESISTANCE("knockback_resistance", 10, 100),
    MINING_SPEED("mining_speed", 10, 100),
    FORTUNE("fortune", 10, 50),
    SWIMMING_SPEED("swimming_speed", 10, 100),
    FALL_REDUCTION("fall_reduction", 10, 100),
    UNDEAD_DAMAGE("undead_damage", 10, 150),
    NETHER_DAMAGE("nether_damage", 10, 150),
    THORNS("thorns", 5, 40),
    HEALTH("health", 5, 50),
    STUN_CHANCE("stun_chance", 5, 40),
    FLAME_CHANCE("flame_chance", 5, 40),
    BLEEDING_CHANCE("bleeding_chance", 5, 40),
    SHOCKING_CHANCE("shocking_chance", 5, 40),
    POISON_CHANCE("poison_chance", 5, 40),
    WEAKENING_CHANCE("weakening_chance", 5, 40),
    HEALING_EFFICIENCY("healing_efficiency", 10, 100),
    WATER_BREATHING("water_breathing", 10, 100),
    SOUL_SPEED("soul_speed", 10, 100),
    DRAW_SPEED("draw_speed", 10, 50),
    SWIFT_SNEAK("swift_sneak", 10, 100);

    private static final Map<String, RuneStatType> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(RuneStatType::id, t -> t));

    private final String id;
    private final int minPercent;
    private final int maxPercent;

    RuneStatType(String id, int minPercent, int maxPercent) {
        this.id = id;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
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
