package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.HashSet;
import java.util.Set;

public final class RuneAttributeApplier {

    private static final String DURABILITY_BASE_KEY = "runic_base_max_damage";

    private RuneAttributeApplier() {}

    private static boolean isRunicModifier(AttributeModifier mod) {
        ResourceLocation id = mod.id();
        return id != null && RunicMod.MOD_ID.equals(id.getNamespace());
    }

    private static String makeKey(Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                  AttributeModifier modifier,
                                  EquipmentSlotGroup slotGroup) {
        ResourceLocation attrKey = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
        ResourceLocation modId = modifier.id();
        String a = attrKey != null ? attrKey.toString() : "null";
        String m = modId != null ? modId.toString() : "null";
        return a + "|" + slotGroup.name() + "|" + m;
    }

    private static void addUnique(ItemAttributeModifiers.Builder builder,
                                  Set<String> keys,
                                  Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                  AttributeModifier modifier,
                                  EquipmentSlotGroup slotGroup) {
        String key = makeKey(attribute, modifier, slotGroup);
        if (keys.add(key)) {
            builder.add(attribute, modifier, slotGroup);
        }
    }

    public static void clearRunicAttributes(ItemStack stack) {
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (existing == ItemAttributeModifiers.EMPTY) return;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        boolean changed = false;

        for (ItemAttributeModifiers.Entry e : existing.modifiers()) {
            if (isRunicModifier(e.modifier())) {
                changed = true;
                continue;
            }
            builder.add(e.attribute(), e.modifier(), e.slot());
        }

        if (changed) {
            ItemAttributeModifiers result = builder.build();
            if (result.modifiers().isEmpty()) {
                stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
            } else {
                stack.set(DataComponents.ATTRIBUTE_MODIFIERS, result);
            }
        }
    }

    public static void rebuildAttributes(ItemStack stack, RuneStats stats) {
        Item item = stack.getItem();

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        Set<String> keys = new HashSet<>();

        ItemAttributeModifiers baseFromItem =
                stack.getPrototype().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        for (ItemAttributeModifiers.Entry e : baseFromItem.modifiers()) {
            addUnique(builder, keys, e.attribute(), e.modifier(), e.slot());
        }

        if (item instanceof ArmorItem armorItem) {
            ItemAttributeModifiers armorDefaults = armorItem.getDefaultAttributeModifiers();
            for (ItemAttributeModifiers.Entry e : armorDefaults.modifiers()) {
                addUnique(builder, keys, e.attribute(), e.modifier(), e.slot());
            }
        }

        ItemAttributeModifiers stackModifiers =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        for (ItemAttributeModifiers.Entry e : stackModifiers.modifiers()) {
            AttributeModifier mod = e.modifier();
            if (isRunicModifier(mod)) continue;
            addUnique(builder, keys, e.attribute(), mod, e.slot());
        }

        EquipmentSlotGroup slotGroup = resolveSlotGroup(stack);

        if (slotGroup != null && stats != null && !stats.isEmpty()) {
            addPercent(builder, stats, RuneStatType.ATTACK_DAMAGE,        Attributes.ATTACK_DAMAGE,             "attack_damage",        slotGroup);
            addPercent(builder, stats, RuneStatType.ATTACK_SPEED,         Attributes.ATTACK_SPEED,              "attack_speed",         slotGroup);
            addPercent(builder, stats, RuneStatType.ATTACK_RANGE,         Attributes.ENTITY_INTERACTION_RANGE,  "attack_range",         slotGroup);

            addPercent(builder, stats, RuneStatType.MOVEMENT_SPEED,       Attributes.MOVEMENT_SPEED,            "movement_speed",       slotGroup);
            addPercent(builder, stats, RuneStatType.KNOCKBACK_RESISTANCE, Attributes.KNOCKBACK_RESISTANCE,      "knockback_resistance", slotGroup);
            addPercent(builder, stats, RuneStatType.HEALTH,               Attributes.MAX_HEALTH,                "health",               slotGroup);
            addPercent(builder, stats, RuneStatType.TOUGHNESS,            Attributes.ARMOR_TOUGHNESS,           "toughness",            slotGroup);

            addPercent(builder, stats, RuneStatType.MINING_SPEED,         Attributes.BLOCK_BREAK_SPEED,         "mining_speed",         slotGroup);
            addPercent(builder, stats, RuneStatType.FALL_REDUCTION,       Attributes.SAFE_FALL_DISTANCE,        "fall_reduction",       slotGroup);

            addRatio(builder, stats, RuneStatType.SWIMMING_SPEED,   Attributes.WATER_MOVEMENT_EFFICIENCY, "swimming_speed",   slotGroup);
            addRatio(builder, stats, RuneStatType.WATER_BREATHING,  Attributes.OXYGEN_BONUS,              "water_breathing",  slotGroup);
            addRatio(builder, stats, RuneStatType.SWEEPING_RANGE,   Attributes.SWEEPING_DAMAGE_RATIO,     "sweeping_range",   slotGroup);

            if (item instanceof BowItem || item instanceof CrossbowItem) {
                addPercent(builder, stats, RuneStatType.DRAW_SPEED, Attributes.ATTACK_SPEED, "draw_speed", slotGroup);
            }
        }

        ItemAttributeModifiers result = builder.build();
        if (result.modifiers().isEmpty()) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        } else {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, result);
        }
    }

    private static void addPercent(ItemAttributeModifiers.Builder builder,
                                   RuneStats stats,
                                   RuneStatType type,
                                   Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                   String name,
                                   EquipmentSlotGroup slotGroup) {

        float percent = stats.get(type);
        if (percent == 0.0F) return;

        double amount = percent / 100.0F;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stat." + name);
        AttributeModifier mod = new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        builder.add(attribute, mod, slotGroup);
    }

    private static void addRatio(ItemAttributeModifiers.Builder builder,
                                 RuneStats stats,
                                 RuneStatType type,
                                 Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                 String name,
                                 EquipmentSlotGroup slotGroup) {

        float percent = stats.get(type);
        if (percent <= 0.0F) return;

        double amount = percent / 100.0F;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stat." + name);
        AttributeModifier mod = new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE);

        builder.add(attribute, mod, slotGroup);
    }

    private static EquipmentSlotGroup resolveSlotGroup(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof ArmorItem armor) {
            return switch (armor.getEquipmentSlot()) {
                case HEAD -> EquipmentSlotGroup.HEAD;
                case CHEST -> EquipmentSlotGroup.CHEST;
                case LEGS -> EquipmentSlotGroup.LEGS;
                case FEET -> EquipmentSlotGroup.FEET;
                default -> null;
            };
        }

        if (item instanceof TieredItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof ShieldItem
                || item instanceof FishingRodItem
                || item instanceof MaceItem) {
            return EquipmentSlotGroup.MAINHAND;
        }

        return null;
    }

    public static void clearDurability(ItemStack stack, CompoundTag root) {
        if (!root.contains(DURABILITY_BASE_KEY)) return;

        int base = root.getInt(DURABILITY_BASE_KEY);
        if (base > 0) {
            stack.set(DataComponents.MAX_DAMAGE, base);

            int dmg = stack.getDamageValue();
            if (dmg >= base) {
                stack.set(DataComponents.DAMAGE, Math.max(0, base - 1));
            }
        }
        root.remove(DURABILITY_BASE_KEY);
    }

    public static void applyDurability(ItemStack stack, RuneStats stats, CompoundTag root) {
        if (!stack.isDamageableItem()) return;

        float percent = stats.get(RuneStatType.DURABILITY);
        if (percent == 0.0F) return;

        int base = root.contains(DURABILITY_BASE_KEY)
                ? root.getInt(DURABILITY_BASE_KEY)
                : stack.getMaxDamage();

        root.putInt(DURABILITY_BASE_KEY, base);

        int newMax = base + Math.round(base * (percent / 100.0F));
        if (newMax <= 0) newMax = 1;

        stack.set(DataComponents.MAX_DAMAGE, newMax);

        int dmg = stack.getDamageValue();
        if (dmg >= newMax) {
            stack.set(DataComponents.DAMAGE, Math.max(0, newMax - 1));
        }
    }
}
