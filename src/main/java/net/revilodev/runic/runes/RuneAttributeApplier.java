package net.revilodev.runic.runes;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

public final class RuneAttributeApplier {

    private static final String DURABILITY_BASE_KEY = "runic_base_max_damage";

    private RuneAttributeApplier() {}

    // ─────────────────────────────────────────────────────────────
    // ATTRIBUTE MODIFIERS
    // ─────────────────────────────────────────────────────────────

    /** Remove all runic attribute modifiers (namespace = your mod id) */
    public static void clearRunicAttributes(ItemStack stack) {
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (existing == ItemAttributeModifiers.EMPTY) return;

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        boolean changed = false;

        for (ItemAttributeModifiers.Entry e : existing.modifiers()) {
            AttributeModifier mod = e.modifier();
            ResourceLocation id = mod.id();
            if (id != null && RunicMod.MOD_ID.equals(id.getNamespace())) {
                changed = true;
                continue; // drop our old modifier
            }
            builder.add(e.attribute(), mod, e.slot());
        }

        if (changed) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
        }
    }

    /** Rebuild runic attribute modifiers from the current stats */
    public static void rebuildAttributes(ItemStack stack, RuneStats stats) {
        ItemAttributeModifiers existing =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // keep non-runic modifiers as-is
        for (ItemAttributeModifiers.Entry e : existing.modifiers()) {
            builder.add(e.attribute(), e.modifier(), e.slot());
        }

        // Core combat
        addPercentAttribute(builder, stats, RuneStatType.ATTACK_DAMAGE, Attributes.ATTACK_DAMAGE, "attack_damage");
        addPercentAttribute(builder, stats, RuneStatType.ATTACK_SPEED, Attributes.ATTACK_SPEED, "attack_speed");
        addPercentAttribute(builder, stats, RuneStatType.ATTACK_RANGE, Attributes.ENTITY_INTERACTION_RANGE, "attack_range");

        // Movement / defense / health
        addPercentAttribute(builder, stats, RuneStatType.MOVEMENT_SPEED, Attributes.MOVEMENT_SPEED, "movement_speed");
        addPercentAttribute(builder, stats, RuneStatType.KNOCKBACK_RESISTANCE, Attributes.KNOCKBACK_RESISTANCE, "knockback_resistance");
        addPercentAttribute(builder, stats, RuneStatType.HEALTH, Attributes.MAX_HEALTH, "health");

        // Mining & underwater movement
        addPercentAttribute(builder, stats, RuneStatType.MINING_SPEED, Attributes.BLOCK_BREAK_SPEED, "mining_speed");
        addPercentAttribute(builder, stats, RuneStatType.SWIMMING_SPEED, Attributes.WATER_MOVEMENT_EFFICIENCY, "swimming_speed");

        // Fall & breathing
        addPercentAttribute(builder, stats, RuneStatType.FALL_REDUCTION, Attributes.SAFE_FALL_DISTANCE, "safe_fall_distance");
        addPercentAttribute(builder, stats, RuneStatType.WATER_BREATHING, Attributes.OXYGEN_BONUS, "oxygen_bonus");

        // Souls / sneak speed
        addPercentAttribute(builder, stats, RuneStatType.SOUL_SPEED, Attributes.MOVEMENT_EFFICIENCY, "soul_speed");
        addPercentAttribute(builder, stats, RuneStatType.SWIFT_SNEAK, Attributes.SNEAKING_SPEED, "swift_sneak");

        // (FORTUNE is not attribute based; handled via loot/event if you want later.)

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    /** Helper: ADD_MULTIPLIED_TOTAL style percent buff */
    private static void addPercentAttribute(ItemAttributeModifiers.Builder builder,
                                            RuneStats stats,
                                            RuneStatType type,
                                            Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                            String pathId) {
        float percent = stats.get(type);
        if (percent == 0.0F) return;

        double amount = percent / 100.0D;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stat." + pathId);

        AttributeModifier modifier = new AttributeModifier(
                id,
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        // For simplicity: mainhand; if you want armor stats, you can branch by item later
        builder.add(attribute, modifier, EquipmentSlotGroup.MAINHAND);
    }

    // ─────────────────────────────────────────────────────────────
    // DURABILITY HANDLING
    // ─────────────────────────────────────────────────────────────

    /** Reset item max damage back to stored base, if we had boosted it */
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

    /** Apply DURABILITY% from stats as a real max-damage increase */
    public static void applyDurability(ItemStack stack, RuneStats stats, CompoundTag root) {
        if (!stack.isDamageableItem()) return;

        float percent = stats.get(RuneStatType.DURABILITY);
        if (percent == 0.0F) return;

        int base;
        if (root.contains(DURABILITY_BASE_KEY)) {
            base = root.getInt(DURABILITY_BASE_KEY);
        } else {
            base = stack.getMaxDamage();
            root.putInt(DURABILITY_BASE_KEY, base);
        }

        int newMax = base + Math.round(base * (percent / 100.0F));
        if (newMax <= 0) newMax = 1;

        stack.set(DataComponents.MAX_DAMAGE, newMax);

        int dmg = stack.getDamageValue();
        if (dmg >= newMax) {
            stack.set(DataComponents.DAMAGE, Math.max(0, newMax - 1));
        }
    }
}
