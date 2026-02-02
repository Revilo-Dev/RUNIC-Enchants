package net.revilodev.runic.event;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import net.revilodev.runic.recipe.EtchingTableRecipe;

import java.util.HashSet;
import java.util.Set;

public final class EnchantBlacklist {
    private EnchantBlacklist() {}

    /* ===============================
       HARD (PERMANENT) DISABLED
       =============================== */
    private static final Set<ResourceLocation> HARD_DISABLED = Set.of(
            ResourceLocation.parse("minecraft:bane_of_arthropods"),
            ResourceLocation.parse("minecraft:efficiency"),
            ResourceLocation.parse("minecraft:unbreaking"),
            ResourceLocation.parse("minecraft:sharpness"),
            ResourceLocation.parse("minecraft:smite"),
            ResourceLocation.parse("minecraft:protection"),
            ResourceLocation.parse("minecraft:fire_protection"),
            ResourceLocation.parse("minecraft:blast_protection"),
            ResourceLocation.parse("minecraft:projectile_protection"),
            ResourceLocation.parse("minecraft:sweeping_edge"),
            ResourceLocation.parse("minecraft:respiration"),
            ResourceLocation.parse("minecraft:power")
    );

    /* ===============================
       CONFIG DISABLED (RUNTIME)
       =============================== */
    private static final Set<ResourceLocation> CONFIG_DISABLED = new HashSet<>();

    public static void setConfigDisabled(Set<ResourceLocation> ids) {
        CONFIG_DISABLED.clear();
        CONFIG_DISABLED.addAll(ids);
    }

    /* ===============================
       CORE CHECKS
       =============================== */
    public static boolean isBlacklisted(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .map(EnchantBlacklist::isBlacklisted)
                .orElse(false);
    }

    public static boolean isBlacklisted(ResourceLocation id) {
        return HARD_DISABLED.contains(id) || CONFIG_DISABLED.contains(id);
    }

    /* ===============================
       ETCHING TABLE SUPPORT
       =============================== */
    public static boolean isRecipeBlacklisted(EtchingTableRecipe recipe) {
        return recipe.effect()
                .map(EnchantBlacklist::isBlacklisted)
                .orElse(false);
    }

    /* ===============================
       STRIPPING
       =============================== */
    public static boolean strip(ItemStack stack) {
        boolean changed = false;

        ItemEnchantments cur = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!cur.isEmpty()) {
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(cur);
            cur.entrySet().forEach(e -> {
                if (isBlacklisted(e.getKey())) mut.set(e.getKey(), 0);
            });
            ItemEnchantments cleaned = mut.toImmutable();
            if (!cleaned.equals(cur)) {
                stack.set(DataComponents.ENCHANTMENTS, cleaned);
                changed = true;
            }
        }

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(stored);
            stored.entrySet().forEach(e -> {
                if (isBlacklisted(e.getKey())) mut.set(e.getKey(), 0);
            });
            ItemEnchantments cleaned = mut.toImmutable();
            if (!cleaned.equals(stored)) {
                stack.set(DataComponents.STORED_ENCHANTMENTS, cleaned);
                changed = true;
            }
        }

        return changed;
    }
}
