package net.revilodev.runic.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

public class RuneItem extends Item {
    private static final Set<ResourceLocation> EFFECT_ENCHANT_IDS = Set.of(
            ResourceLocation.withDefaultNamespace("silk_touch"),
            ResourceLocation.withDefaultNamespace("frost_walker"),
            ResourceLocation.withDefaultNamespace("loyalty"),
            ResourceLocation.withDefaultNamespace("lure"),
            ResourceLocation.withDefaultNamespace("wind_burst"),
            ResourceLocation.withDefaultNamespace("breach"),
            ResourceLocation.withDefaultNamespace("flame"),
            ResourceLocation.withDefaultNamespace("binding_curse"),
            ResourceLocation.withDefaultNamespace("vanishing_curse")
    );

    public RuneItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public static Set<ResourceLocation> allowedEffectIds() {
        return EFFECT_ENCHANT_IDS;
    }

    public static boolean isEffectEnchantment(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(ResourceKey::location)
                .map(EFFECT_ENCHANT_IDS::contains)
                .orElse(false);
    }

    public static ItemStack createEffectRune(Holder<Enchantment> enchantment, int level) {
        if (!isEffectEnchantment(enchantment)) {
            return ItemStack.EMPTY;
        }
        int clamped = Math.max(1, Math.min(level, enchantment.value().getMaxLevel()));
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.value());
        stack.enchant(enchantment, clamped);
        return stack;
    }

    public static ItemStack createStatRune(RandomSource random, RuneStatType type) {
        RuneStats stats = RuneStats.singleUnrolled(type);
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.value());
        RuneStats.set(stack, stats);
        return stack;
    }

    public static ItemStack createRandomStatRune(RandomSource random) {
        RuneStatType[] all = RuneStatType.values();
        if (all.length == 0) {
            return ItemStack.EMPTY;
        }
        RuneStatType chosen = all[random.nextInt(all.length)];
        return createStatRune(random, chosen);
    }

    public static RuneStats getRolledStatsForTooltip(ItemStack rune) {
        RuneStats template = getRuneStats(rune);
        if (template == null || template.isEmpty()) return RuneStats.empty();

        return RuneStats.rollForApplication(template, RandomSource.create());
    }



    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEnchanted()) {
            return true;
        }
        RuneStats stats = RuneStats.get(stack);
        stats = RuneStats.rollForApplication(stats, RandomSource.create());

        return stats != null && !stats.isEmpty();
    }

    public static RuneStats getRuneStats(ItemStack stack) {
        return RuneStats.get(stack);
    }

    public static Holder<Enchantment> getPrimaryEffectEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments enchants = !stored.isEmpty() ? stored : direct;
        if (enchants.isEmpty()) {
            return null;
        }
        List<Holder<Enchantment>> candidates = new ArrayList<>();
        for (Holder<Enchantment> holder : enchants.keySet()) {
            if (isEffectEnchantment(holder)) {
                candidates.add(holder);
            }
        }
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }
        return enchants.keySet().iterator().next();
    }

    public static ResourceLocation getRuneTexture(ItemStack stack) {
        ResourceLocation base = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "item/rune/enhanced_rune");

        Holder<Enchantment> effect = getPrimaryEffectEnchantment(stack);
        if (effect != null) {
            return effect.unwrapKey()
                    .map(k -> {
                        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                                RunicMod.MOD_ID,
                                "item/rune/" + k.location().getNamespace() + "/" + k.location().getPath()
                        );
                        RunicMod.LOGGER.debug("[Runic] Resolved effect rune texture for {} -> {}", k.location(), tex);
                        return tex;
                    })
                    .orElseGet(() -> {
                        RunicMod.LOGGER.warn("[Runic] Could not resolve key for effect enchant on rune: {}", stack);
                        return base;
                    });
        }

        RuneStats stats = RuneStats.get(stack);
        if (!stats.isEmpty()) {
            RuneStatType chosen = null;
            float best = 0.0F;
            for (RuneStatType type : RuneStatType.values()) {
                float v = stats.get(type);
                if (Math.abs(v) > Math.abs(best) + 0.001F) {
                    best = v;
                    chosen = type;
                }
            }
            if (chosen != null) {
                ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                        RunicMod.MOD_ID,
                        "item/rune/stat/" + chosen.id()
                );
                RunicMod.LOGGER.debug("[Runic] Resolved stat rune texture for {} -> {}", chosen.id(), tex);
                return tex;
            }
        }



        RunicMod.LOGGER.debug("[Runic] Falling back to base rune texture for {}", stack);
        return base;
    }
}
