package net.revilodev.runic.gear;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.EnumMap;
import java.util.Map;

public final class GearAttributes {
    public static final int MAX_LEVEL = 10;

    private static final String ROOT = "runic";
    private static final String ATTRS = "gear_attributes";
    private static final String CURSED_APPLIED = "cursed_applied";

    private GearAttributes() {}

    public static int getLevel(ItemStack stack, GearAttribute attr) {
        CompoundTag runic = getRunic(stack);
        if (runic == null) return 0;
        if (!runic.contains(ATTRS, Tag.TAG_COMPOUND)) return 0;
        CompoundTag a = runic.getCompound(ATTRS);
        return Math.max(0, a.getInt(attr.id()));
    }

    public static boolean has(ItemStack stack, GearAttribute attr) {
        return getLevel(stack, attr) > 0;
    }

    public static Map<GearAttribute, Integer> getAll(ItemStack stack) {
        EnumMap<GearAttribute, Integer> out = new EnumMap<>(GearAttribute.class);
        for (GearAttribute a : GearAttribute.values()) {
            int lvl = getLevel(stack, a);
            if (lvl > 0) out.put(a, lvl);
        }
        return out;
    }

    public static void addLevel(ItemStack stack, GearAttribute attr, int amount) {
        if (amount == 0) return;

        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();
        CompoundTag attrs = runic.contains(ATTRS, Tag.TAG_COMPOUND) ? runic.getCompound(ATTRS) : new CompoundTag();

        int curr = Math.max(0, attrs.getInt(attr.id()));
        int next = curr + amount;

        if (next < 0) next = 0;
        if (next > MAX_LEVEL) next = MAX_LEVEL;

        if (next == 0) attrs.remove(attr.id());
        else attrs.putInt(attr.id(), next);

        if (attrs.isEmpty()) runic.remove(ATTRS);
        else runic.put(ATTRS, attrs);

        if (runic.isEmpty()) root.remove(ROOT);
        else root.put(ROOT, runic);

        setRoot(stack, root);
    }

    public static int getCursedAppliedLevel(ItemStack stack) {
        CompoundTag runic = getRunic(stack);
        if (runic == null) return 0;
        return Math.max(0, runic.getInt(CURSED_APPLIED));
    }

    public static void setCursedAppliedLevel(ItemStack stack, int level) {
        if (level < 0) level = 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;

        CompoundTag root = getRootCopy(stack);
        CompoundTag runic = root.contains(ROOT, Tag.TAG_COMPOUND) ? root.getCompound(ROOT) : new CompoundTag();

        if (level <= 0) runic.remove(CURSED_APPLIED);
        else runic.putInt(CURSED_APPLIED, level);

        if (runic.isEmpty()) root.remove(ROOT);
        else root.put(ROOT, runic);

        setRoot(stack, root);
    }

    public static float cursedMultiplier(ItemStack stack) {
        int lvl = getLevel(stack, GearAttribute.CURSED);
        if (lvl <= 0) return 1.0F;
        return (float) Math.pow(0.95D, lvl);
    }

    private static CompoundTag getRootCopy(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return cd.copyTag();
    }

    private static void setRoot(ItemStack stack, CompoundTag root) {
        if (root == null || root.isEmpty()) stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static CompoundTag getRunic(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = cd.copyTag();
        if (!root.contains(ROOT, Tag.TAG_COMPOUND)) return null;
        return root.getCompound(ROOT);
    }
}
