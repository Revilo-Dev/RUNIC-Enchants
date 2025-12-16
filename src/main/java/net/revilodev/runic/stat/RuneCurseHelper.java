package net.revilodev.runic.stat;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;

public final class RuneCurseHelper {

    private RuneCurseHelper() {}

    public static RuneCurseType.CurseTarget targetFor(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ArmorItem) {
            return RuneCurseType.CurseTarget.ARMOR;
        }

        if (item instanceof DiggerItem) {
            return RuneCurseType.CurseTarget.TOOL;
        }

        if (item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof TridentItem
                || item instanceof MaceItem
                || item instanceof BowItem
                || item instanceof CrossbowItem) {
            return RuneCurseType.CurseTarget.WEAPON;
        }

        return RuneCurseType.CurseTarget.TOOL;
    }

    public static RuneStats rollCurse(ItemStack stack, RandomSource random) {
        RuneCurseType[] values = RuneCurseType.values();
        RuneCurseType curse = values[random.nextInt(values.length)];
        return curse.createStats(random, targetFor(stack));
    }
}
