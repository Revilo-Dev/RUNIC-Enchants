package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;

public enum RuneCurseType {

    DAMAGED(RuneStatType.DURABILITY, 10, 20),
    WEAKENED(null, 10, 20),
    HEAVY(null, 5, 15);

    private final RuneStatType fixedStat;
    private final int min;
    private final int max;

    RuneCurseType(RuneStatType fixedStat, int min, int max) {
        this.fixedStat = fixedStat;
        this.min = min;
        this.max = max;
    }

    public int roll(RandomSource random) {
        return min + random.nextInt(max - min + 1);
    }

    public RuneStats createStats(RandomSource random, CurseTarget target) {
        int value = roll(random);

        return switch (this) {
            case DAMAGED ->
                    RuneStats.single(RuneStatType.DURABILITY, -value);

            case WEAKENED -> switch (target) {
                case WEAPON ->
                        RuneStats.single(RuneStatType.ATTACK_DAMAGE, -value);
                case TOOL ->
                        RuneStats.single(RuneStatType.MINING_SPEED, -value);
                case ARMOR ->
                        RuneStats.single(RuneStatType.RESISTANCE, -value);
            };

            case HEAVY -> switch (target) {
                case WEAPON, TOOL ->
                        RuneStats.single(RuneStatType.ATTACK_SPEED, -value);
                case ARMOR ->
                        RuneStats.single(RuneStatType.MOVEMENT_SPEED, -value);
            };
        };
    }

    public enum CurseTarget {
        WEAPON,
        TOOL,
        ARMOR
    }
}
