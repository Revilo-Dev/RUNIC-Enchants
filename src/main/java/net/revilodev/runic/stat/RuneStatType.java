package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum RuneStatType implements StringRepresentable {
    ATTACK_SPEED("attack_speed", 15, 45, 8, 23, 0.0F),
    ATTACK_DAMAGE("attack_damage", 8, 20, 4, 10, 0.0F),
    ATTACK_RANGE("attack_range", 8, 20, 4, 10, 25.0F),
    MOVEMENT_SPEED("movement_speed", 35, 70, 18, 35, 100.0F),
    SWEEPING_RANGE("sweeping_range", 8, 15, 4, 8, 50.0F),
    DURABILITY("durability", 15, 200, 8, 100, 0.0F),
    RESISTANCE("resistance", 8, 15, 4, 8, 25.0F),
    FIRE_RESISTANCE("fire_resistance", 8, 15, 4, 8, 25.0F),
    BLAST_RESISTANCE("blast_resistance", 8, 15, 4, 8, 25.0F),
    PROJECTILE_RESISTANCE("projectile_resistance", 8, 15, 4, 8, 25.0F),
    KNOCKBACK_RESISTANCE("knockback_resistance", 8, 15, 4, 8, 25.0F),
    MINING_SPEED("mining_speed", 20, 150, 10, 75, 0.0F),
    UNDEAD_DAMAGE("undead_damage", 15, 75, 8, 38, 0.0F),
    NETHER_DAMAGE("nether_damage", 15, 75, 8, 38, 0.0F),
    HEALTH("health", 8, 15, 4, 8, 0.0F),
    STUN_CHANCE("stun_chance", 8, 15, 4, 8, 50.0F),
    FLAME_CHANCE("flame_chance", 8, 15, 4, 8, 50.0F),
    BLEEDING_CHANCE("bleeding_chance", 8, 15, 4, 8, 100.0F),
    SHOCKING_CHANCE("shocking_chance", 8, 15, 4, 8, 100.0F),
    POISON_CHANCE("poison_chance", 8, 15, 4, 8, 100.0F),
    WITHERING_CHANCE("withering_chance", 8, 15, 4, 8, 100.0F),
    WEAKENING_CHANCE("weakening_chance", 8, 15, 4, 8, 100.0F),
    HEALING_EFFICIENCY("healing_efficiency", 15, 75, 8, 38, 0.0F),
    DRAW_SPEED("draw_speed", 15, 75, 8, 38, 0.0F),
    TOUGHNESS("toughness", 8, 35, 4, 18, 35.0F),
    FREEZING_CHANCE("freezing_chance", 8, 15, 4, 8, 0.0F),
    LEECHING_CHANCE("leeching_chance", 15, 35, 8, 18, 50.0F),
    BONUS_CHANCE("bonus_chance", 30, 60, 15, 30, 100.0F),
    JUMP_HEIGHT("jump_height", 8, 35, 4, 18, 50.0F),
    POWER("power", 8, 25, 4, 13, 0.0F);

    private static final Map<String, RuneStatType> BY_ID =
            Arrays.stream(values()).collect(Collectors.toMap(RuneStatType::id, t -> t));

    private final String id;
    private final int minPercent;
    private final int maxPercent;
    private final int etchingMinPercent;
    private final int etchingMaxPercent;
    private final float capPercent;

    RuneStatType(String id, int minPercent, int maxPercent, int etchingMinPercent, int etchingMaxPercent, float capPercent) {
        this.id = id;
        this.minPercent = minPercent;
        this.maxPercent = Math.max(maxPercent, minPercent);
        this.etchingMinPercent = etchingMinPercent;
        this.etchingMaxPercent = Math.max(etchingMaxPercent, etchingMinPercent);
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

    public int etchingMinPercent() {
        return this.etchingMinPercent;
    }

    public int etchingMaxPercent() {
        return this.etchingMaxPercent;
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

    public float rollEtching(RandomSource random) {
        if (this.etchingMaxPercent <= this.etchingMinPercent) {
            return this.etchingMinPercent;
        }
        int bound = this.etchingMaxPercent - this.etchingMinPercent + 1;
        return this.etchingMinPercent + random.nextInt(bound);
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
