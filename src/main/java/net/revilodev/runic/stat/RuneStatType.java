package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuneStatType implements StringRepresentable {
    ATTACK_SPEED("attack_speed", 10, 30, 0.0F, false),
    ATTACK_DAMAGE("attack_damage", 5, 15, 0.0F, false),
    ATTACK_RANGE("attack_range", 5, 15, 25.0F, false),
    MOVEMENT_SPEED("movement_speed", 25, 50, 100.0F, false),
    SWEEPING_RANGE("sweeping_range", 5, 10, 50.0F, false),
    DURABILITY("durability", 10, 150, 0.0F, false),
    RESISTANCE("resistance", 5, 10, 25.0F, false),
    FIRE_RESISTANCE("fire_resistance", 5, 10, 25.0F, false),
    BLAST_RESISTANCE("blast_resistance", 5, 10, 25.0F, false),
    PROJECTILE_RESISTANCE("projectile_resistance", 5, 10, 25.0F, false),
    KNOCKBACK_RESISTANCE("knockback_resistance", 5, 10, 25.0F, false),
    MINING_SPEED("mining_speed", 10, 100, 0.0F, false),
    SWIMMING_SPEED("swimming_speed", 10, 80, 0.0F, false),
    FALL_REDUCTION("fall_reduction", 10, 85, 300.0F, false),
    UNDEAD_DAMAGE("undead_damage", 10, 50, 0.0F, false),
    NETHER_DAMAGE("nether_damage", 10, 50, 0.0F, false),
    HEALTH("health", 5, 10, 0.0F, false),
    STUN_CHANCE("stun_chance", 5, 10, 50.0F, false),
    FLAME_CHANCE("flame_chance", 5, 10, 50.0F, false),
    BLEEDING_CHANCE("bleeding_chance", 5, 10, 100.0F, false),
    SHOCKING_CHANCE("shocking_chance", 5, 10, 100.0F, false),
    POISON_CHANCE("poison_chance", 5, 10, 100.0F, false),
    WITHERING_CHANCE("withering_chance", 5, 10, 100.0F, false),
    WEAKENING_CHANCE("weakening_chance", 5, 10, 100.0F, false),
    HEALING_EFFICIENCY("healing_efficiency", 10, 50, 0.0F, false),
    WATER_BREATHING("water_breathing", 50, 150, 0.0F, false),
    DRAW_SPEED("draw_speed", 10, 50, 0.0F, false),
    TOUGHNESS("toughness", 5, 25, 25.0F, false),
    FREEZING_CHANCE("freezing_chance", 5, 10, 0.0F, false),
    LEECHING_CHANCE("leeching_chance", 10, 25, 50.0F, false),
    BONUS_CHANCE("bonus_chance", 20, 40, 100.0F, false),
    JUMP_HEIGHT("jump_height", 5, 25, 50.0F, false),
    POWER("power", 5, 20, 0.0F, false),

    CURSE_DAMAGED("curse_damaged", 10, 20, 0.0F, true),
    CURSE_WEAKENED("curse_weakened", 10, 20, 0.0F, true),
    CURSE_HEAVY("curse_heavy", 5, 15, 0.0F, true);

    private static final Map<String, RuneStatType> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(RuneStatType::id, t -> t));

    private final String id;
    private final int minPercent;
    private final int maxPercent;
    private final float capPercent;
    private final boolean curse;

    RuneStatType(String id, int minPercent, int maxPercent, float capPercent, boolean curse) {
        this.id = id;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
        this.capPercent = capPercent;
        this.curse = curse;
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

    public boolean isCurse() {
        return this.curse;
    }

    public float roll(RandomSource random) {
        if (this.maxPercent <= this.minPercent) {
            return this.minPercent;
        }
        int bound = this.maxPercent - this.minPercent + 1;
        return this.minPercent + random.nextInt(bound);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public static RuneStatType byId(String id) {
        return BY_ID.get(id);
    }

    public static Map<RuneStatType, Float> emptyMap() {
        return new EnumMap<>(RuneStatType.class);
    }
}
