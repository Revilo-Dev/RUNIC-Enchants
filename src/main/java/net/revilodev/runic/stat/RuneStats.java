package net.revilodev.runic.stat;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.revilodev.runic.runes.RuneAttributeApplier;

import java.util.EnumMap;
import java.util.Map;

public final class RuneStats {

    public static final String NBT_KEY = "runic_stats";
    private static final RuneStats EMPTY = new RuneStats(new EnumMap<>(RuneStatType.class));

    final EnumMap<RuneStatType, Float> values;

    public RuneStats(EnumMap<RuneStatType, Float> values) {
        this.values = values;
    }

    public float get(RuneStatType type) {
        return values.getOrDefault(type, 0.0F);
    }

    public boolean has(RuneStatType type) {
        return values.containsKey(type);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Map<RuneStatType, Float> view() {
        return Map.copyOf(values);
    }

    /** ---------------------------------------------------
     *  FIX FOR -1% TOOLTIP VALUES
     * --------------------------------------------------- */
    public RuneStats rolledForTooltip() {
        return rollForApplication(this, RandomSource.create());
    }

    /** ---------------------------------------------------
     *  SAVE / LOAD
     * --------------------------------------------------- */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<RuneStatType, Float> e : values.entrySet()) {
            float v = e.getValue();
            if (v != 0.0F) {
                tag.putFloat(e.getKey().id(), v);
            }
        }
        return tag;
    }

    public static RuneStats load(CompoundTag tag) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (String key : tag.getAllKeys()) {
            RuneStatType type = RuneStatType.byId(key);
            if (type != null) {
                map.put(type, tag.getFloat(key));
            }
        }
        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    public static RuneStats empty() {
        return EMPTY;
    }

    /** ---------------------------------------------------
     *  GENERATION
     * --------------------------------------------------- */
    public static RuneStats single(RuneStatType type, float value) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.put(type, value);
        return new RuneStats(map);
    }

    public static RuneStats singleUnrolled(RuneStatType type) {
        return single(type, -1.0F); // placeholder for rolled stat
    }

    public static RuneStats rollForApplication(RuneStats template, RandomSource random) {
        if (template == null || template.isEmpty()) {
            return EMPTY;
        }
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (Map.Entry<RuneStatType, Float> e : template.values.entrySet()) {
            RuneStatType type = e.getKey();
            float v = e.getValue();

            if (v < 0.0F) { // unrolled stat
                v = type.roll(random);
            }
            if (v != 0.0F) {
                map.put(type, v);
            }
        }
        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    /** ---------------------------------------------------
     *  COMBINE
     * --------------------------------------------------- */
    public static RuneStats combine(RuneStats base, RuneStats add) {
        if ((base == null || base.isEmpty()) && (add == null || add.isEmpty())) {
            return EMPTY;
        }
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        if (base != null) {
            map.putAll(base.values);
        }
        if (add != null) {
            for (Map.Entry<RuneStatType, Float> e : add.values.entrySet()) {
                map.merge(e.getKey(), e.getValue(), Float::sum);
            }
        }
        return map.isEmpty() ? EMPTY : new RuneStats(map);
    }

    /** ---------------------------------------------------
     *  GET / SET ON STACK
     * --------------------------------------------------- */
    public static RuneStats get(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = data.copyTag();
        if (root == null || !root.contains(NBT_KEY)) {
            return EMPTY;
        }
        return load(root.getCompound(NBT_KEY));
    }

    public static void set(ItemStack stack, RuneStats stats) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = existing.copyTag();
        if (root == null) {
            root = new CompoundTag();
        }

        if (stats == null || stats.isEmpty()) {
            root.remove(NBT_KEY);
        } else {
            root.put(NBT_KEY, stats.save());
        }

        RuneAttributeApplier.clearRunicAttributes(stack);
        RuneAttributeApplier.clearDurability(stack, root);

        if (stats != null && !stats.isEmpty()) {
            RuneAttributeApplier.rebuildAttributes(stack, stats);
            RuneAttributeApplier.applyDurability(stack, stats, root);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }
}
