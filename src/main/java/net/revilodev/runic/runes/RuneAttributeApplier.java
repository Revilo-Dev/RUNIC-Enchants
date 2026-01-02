package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
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
        return (attrKey != null ? attrKey : "null") + "|" + slotGroup.name() + "|" + (modId != null ? modId : "null");
    }

    private static void addUnique(ItemAttributeModifiers.Builder builder,
                                  Set<String> keys,
                                  Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                  AttributeModifier modifier,
                                  EquipmentSlotGroup slotGroup) {
        if (keys.add(makeKey(attribute, modifier, slotGroup))) {
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

        ItemAttributeModifiers proto =
                stack.getPrototype().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        for (ItemAttributeModifiers.Entry e : proto.modifiers()) {
            addUnique(builder, keys, e.attribute(), e.modifier(), e.slot());
        }

        if (item instanceof ArmorItem armor) {
            for (ItemAttributeModifiers.Entry e : armor.getDefaultAttributeModifiers().modifiers()) {
                addUnique(builder, keys, e.attribute(), e.modifier(), e.slot());
            }
        }

        ItemAttributeModifiers current =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        for (ItemAttributeModifiers.Entry e : current.modifiers()) {
            if (!isRunicModifier(e.modifier())) {
                addUnique(builder, keys, e.attribute(), e.modifier(), e.slot());
            }
        }

        EquipmentSlotGroup slotGroup = resolveSlotGroup(stack);

        if (slotGroup != null && stats != null && !stats.isEmpty()) {
            addPercent(builder, stats, RuneStatType.ATTACK_DAMAGE,        Attributes.ATTACK_DAMAGE,       "attack_damage",        slotGroup);
            addPercent(builder, stats, RuneStatType.ATTACK_SPEED,         Attributes.ATTACK_SPEED,        "attack_speed",         slotGroup);
            addPercent(builder, stats, RuneStatType.ATTACK_RANGE,         Attributes.ENTITY_INTERACTION_RANGE, "attack_range", slotGroup);

            addPercent(builder, stats, RuneStatType.MOVEMENT_SPEED,       Attributes.MOVEMENT_SPEED,      "movement_speed",       slotGroup);
            addPercent(builder, stats, RuneStatType.KNOCKBACK_RESISTANCE, Attributes.KNOCKBACK_RESISTANCE,"knockback_resistance", slotGroup);
            addPercent(builder, stats, RuneStatType.HEALTH,               Attributes.MAX_HEALTH,          "health",               slotGroup);
            addPercent(builder, stats, RuneStatType.TOUGHNESS,            Attributes.ARMOR_TOUGHNESS,     "toughness",            slotGroup);

            addPercent(builder, stats, RuneStatType.MINING_SPEED,         Attributes.BLOCK_BREAK_SPEED,   "mining_speed",         slotGroup);
            addRatio  (builder, stats, RuneStatType.SWEEPING_RANGE,       Attributes.SWEEPING_DAMAGE_RATIO, "sweeping_range", slotGroup);

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

        if (stats != null && !stats.isEmpty()) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        } else {
            stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
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

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                RunicMod.MOD_ID,
                "stat." + name + "." + slotGroup.name().toLowerCase()
        );

        builder.add(attribute,
                new AttributeModifier(id, percent / 100.0F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                slotGroup
        );
    }

    private static void addRatio(ItemAttributeModifiers.Builder builder,
                                 RuneStats stats,
                                 RuneStatType type,
                                 Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                 String name,
                                 EquipmentSlotGroup slotGroup) {

        float percent = stats.get(type);
        if (percent <= 0.0F) return;

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                RunicMod.MOD_ID,
                "stat." + name + "." + slotGroup.name().toLowerCase()
        );

        builder.add(attribute,
                new AttributeModifier(id, percent / 100.0F, AttributeModifier.Operation.ADD_VALUE),
                slotGroup
        );
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
        stack.set(DataComponents.MAX_DAMAGE, base);

        if (stack.getDamageValue() >= base) {
            stack.set(DataComponents.DAMAGE, base - 1);
        }

        root.remove(DURABILITY_BASE_KEY);
    }

    public static void applyDurability(ItemStack stack, RuneStats stats, CompoundTag root) {
        if (!stack.isDamageableItem()) return;

        float bonus = stats.get(RuneStatType.DURABILITY);
        if (bonus == 0.0F) return;

        int base = root.contains(DURABILITY_BASE_KEY)
                ? root.getInt(DURABILITY_BASE_KEY)
                : stack.getMaxDamage();

        root.putInt(DURABILITY_BASE_KEY, base);

        int newMax = Math.max(1, base + Math.round(base * (bonus / 100.0F)));
        stack.set(DataComponents.MAX_DAMAGE, newMax);

        if (stack.getDamageValue() >= newMax) {
            stack.set(DataComponents.DAMAGE, newMax - 1);
        }
    }
}
