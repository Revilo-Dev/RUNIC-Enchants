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
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EtchingItem extends Item {

    public EtchingItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    public static Set<ResourceLocation> allowedEffectIds() {
        return RuneItem.allowedEffectIds();
    }

    public static boolean isEffectEnchantment(Holder<Enchantment> holder) {
        return RuneItem.isEffectEnchantment(holder);
    }

    public static ItemStack createEffectEtching(Holder<Enchantment> enchantment) {
        if (!isEffectEnchantment(enchantment)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.ETCHING.get());
        stack.enchant(enchantment, RuneItem.forcedEtchingEffectLevel(enchantment));
        return stack;
    }

    public static ItemStack createStatEtching(RandomSource random, RuneStatType type) {
        RuneStats stats = RuneStats.singleUnrolled(type);
        ItemStack stack = new ItemStack(ModItems.ETCHING.get());
        RuneStats.set(stack, stats);
        return stack;
    }

    public static ItemStack createRandomStatEtching(RandomSource random) {
        RuneStatType[] all = RuneStatType.values();
        if (all.length == 0) {
            return ItemStack.EMPTY;
        }
        return createStatEtching(random, all[random.nextInt(all.length)]);
    }

    public static RuneStats getRolledStatsForTooltip(ItemStack etching) {
        RuneStats template = RuneStats.get(etching);
        if (template == null || template.isEmpty()) {
            return RuneStats.empty();
        }
        return RuneStats.rollForApplication(template, RandomSource.create(), true);
    }

    public static RuneStats getEtchingStats(ItemStack stack) {
        return RuneStats.get(stack);
    }

    public static Holder<Enchantment> getPrimaryEffectEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments enchants = !stored.isEmpty() ? stored : direct;

        if (enchants.isEmpty()) {
            return null;
        }

        List<Holder<Enchantment>> effects = new ArrayList<>();
        for (Holder<Enchantment> h : enchants.keySet()) {
            if (isEffectEnchantment(h)) {
                effects.add(h);
            }
        }

        if (!effects.isEmpty()) {
            return effects.get(0);
        }

        return enchants.keySet().iterator().next();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (stack.isEnchanted()) {
            return true;
        }
        RuneStats stats = RuneStats.get(stack);
        return stats != null && !stats.isEmpty();
    }
}
