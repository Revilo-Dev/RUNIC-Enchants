package net.revilodev.runic.stat;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.revilodev.runic.RunicMod;

import java.util.EnumMap;
import java.util.Map;

public final class RuneStats {
    public static final String NBT_KEY = "runic_stats";
    private static final RuneStats EMPTY = new RuneStats(new EnumMap<>(RuneStatType.class));
    private static final String BASE_DURABILITY_KEY = "runic_base_max_damage";

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
        if (map.isEmpty()) {
            return EMPTY;
        }
        return new RuneStats(map);
    }

    public static RuneStats empty() {
        return EMPTY;
    }

    public static RuneStats single(RuneStatType type, float value) {
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        map.put(type, value);
        return new RuneStats(map);
    }

    public static RuneStats singleUnrolled(RuneStatType type) {
        return single(type, -1.0F);
    }

    public static RuneStats rollForApplication(RuneStats template, RandomSource random) {
        if (template == null || template.isEmpty()) {
            return EMPTY;
        }
        EnumMap<RuneStatType, Float> map = new EnumMap<>(RuneStatType.class);
        for (Map.Entry<RuneStatType, Float> e : template.values.entrySet()) {
            RuneStatType type = e.getKey();
            float v = e.getValue();
            if (v < 0.0F) {
                v = type.roll(random);
            }
            if (v != 0.0F) {
                map.put(type, v);
            }
        }
        if (map.isEmpty()) {
            return EMPTY;
        }
        return new RuneStats(map);
    }

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
        if (map.isEmpty()) {
            return EMPTY;
        }
        return new RuneStats(map);
    }

    public static RuneStats get(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = data.copyTag();
        if (root == null || !root.contains(NBT_KEY)) {
            return EMPTY;
        }
        CompoundTag statsTag = root.getCompound(NBT_KEY);
        return load(statsTag);
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

        clearRunicAttributeModifiers(stack);
        clearDurabilityBonus(stack, root);

        if (stats != null && !stats.isEmpty()) {
            rebuildRunicAttributeModifiers(stack, stats);
            applyDurabilityBonus(stack, stats, root);
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
    }

    private static void clearRunicAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (existing == ItemAttributeModifiers.EMPTY) {
            return;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        boolean changed = false;

        for (ItemAttributeModifiers.Entry e : existing.modifiers()) {
            AttributeModifier mod = e.modifier();
            ResourceLocation id = mod.id();
            if (id != null && RunicMod.MOD_ID.equals(id.getNamespace())) {
                changed = true;
                continue;
            }
            builder.add(e.attribute(), mod, e.slot());
        }

        if (changed) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }
    }

    private static void rebuildRunicAttributeModifiers(ItemStack stack, RuneStats stats) {
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        for (ItemAttributeModifiers.Entry e : existing.modifiers()) {
            builder.add(e.attribute(), e.modifier(), e.slot());
        }

        addPercentModifier(builder, stats, RuneStatType.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE, "stat.attack_damage");
        addPercentModifier(builder, stats, RuneStatType.ATTACK_SPEED, Attributes.ATTACK_SPEED, "stat.attack_speed");

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static void addPercentModifier(ItemAttributeModifiers.Builder builder,
                                           RuneStats stats,
                                           RuneStatType type,
                                           Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                           String pathId) {
        float percent = stats.get(type);
        if (percent == 0.0F) return;

        double amount = percent / 100.0D;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, pathId);

        AttributeModifier modifier = new AttributeModifier(
                id,
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        builder.add(attribute, modifier, EquipmentSlotGroup.MAINHAND);
    }

    private static void clearDurabilityBonus(ItemStack stack, CompoundTag root) {
        if (!root.contains(BASE_DURABILITY_KEY)) {
            return;
        }
        int base = root.getInt(BASE_DURABILITY_KEY);
        if (base > 0 && stack.getMaxDamage() != base) {
            stack.set(DataComponents.MAX_DAMAGE, base);
            int damage = stack.getDamageValue();
            if (damage >= base) {
                stack.set(DataComponents.DAMAGE, Math.max(0, base - 1));
            }
        }
        root.remove(BASE_DURABILITY_KEY);
    }

    private static void applyDurabilityBonus(ItemStack stack, RuneStats stats, CompoundTag root) {
        if (!stack.isDamageableItem()) return;

        int base;
        if (root.contains(BASE_DURABILITY_KEY)) {
            base = root.getInt(BASE_DURABILITY_KEY);
        } else {
            base = stack.getMaxDamage();
            root.putInt(BASE_DURABILITY_KEY, base);
        }

        float percent = stats.get(RuneStatType.DURABILITY);
        if (percent == 0.0F) {
            return;
        }

        int newMax = base + Math.round(base * (percent / 100.0F));
        if (newMax <= 0) {
            newMax = 1;
        }

        stack.set(DataComponents.MAX_DAMAGE, newMax);

        int damage = stack.getDamageValue();
        if (damage >= newMax) {
            stack.set(DataComponents.DAMAGE, Math.max(0, newMax - 1));
        }
    }
}
