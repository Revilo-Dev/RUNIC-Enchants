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

public class RuneItem extends Item {

    private static final Set<ResourceLocation> EFFECT_ENCHANT_IDS = Set.of(
            ResourceLocation.fromNamespaceAndPath("aether", "renewal"),

            ResourceLocation.fromNamespaceAndPath("combat_roll", "acrobat"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "longfooted"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "multi_roll"),

            ResourceLocation.fromNamespaceAndPath("create", "capacity"),
            ResourceLocation.fromNamespaceAndPath("create", "potato_recovery"),

            ResourceLocation.fromNamespaceAndPath("deeperdarker", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "discharge"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "sculk_smite"),

            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "discharge"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "ensnaring"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "lolths_curse"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "purification"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "voltaic_shot"),

            ResourceLocation.fromNamespaceAndPath("expanded_combat", "agility"),
            ResourceLocation.fromNamespaceAndPath("expanded_combat", "blocking"),
            ResourceLocation.fromNamespaceAndPath("expanded_combat", "ground_slam"),

            ResourceLocation.fromNamespaceAndPath("farmersdelight", "backstabbing"),

            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "mystical_enlightenment"),
            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("simplyswords", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "fire_react"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("supplementaries", "stasis"),

            ResourceLocation.fromNamespaceAndPath("twilightforest", "chill_aura"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "destruction"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "fire_react"),

            ResourceLocation.withDefaultNamespace("binding_curse"),
            ResourceLocation.withDefaultNamespace("breach"),
            ResourceLocation.withDefaultNamespace("fortune"),
            ResourceLocation.withDefaultNamespace("frost_walker"),
            ResourceLocation.withDefaultNamespace("loyalty"),
            ResourceLocation.withDefaultNamespace("lure"),
            ResourceLocation.withDefaultNamespace("mending"),
            ResourceLocation.withDefaultNamespace("piercing"),
            ResourceLocation.withDefaultNamespace("punch"),
            ResourceLocation.withDefaultNamespace("silk_touch"),
            ResourceLocation.withDefaultNamespace("soul_speed"),
            ResourceLocation.withDefaultNamespace("swift_sneak"),
            ResourceLocation.withDefaultNamespace("thorns"),
            ResourceLocation.withDefaultNamespace("vanishing_curse"),
            ResourceLocation.withDefaultNamespace("wind_burst")
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

    public static int forcedEffectLevel(Holder<Enchantment> holder) {
        return Math.max(1, holder.value().getMaxLevel());
    }

    public static ItemStack createEffectRune(Holder<Enchantment> enchantment) {
        if (!isEffectEnchantment(enchantment)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        stack.enchant(enchantment, forcedEffectLevel(enchantment));
        return stack;
    }

    public static ItemStack createStatRune(RandomSource random, RuneStatType type) {
        RuneStats stats = RuneStats.singleUnrolled(type);
        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.get());
        RuneStats.set(stack, stats);
        return stack;
    }

    public static ItemStack createRandomStatRune(RandomSource random) {
        RuneStatType[] all = RuneStatType.values();
        if (all.length == 0) {
            return ItemStack.EMPTY;
        }
        return createStatRune(random, all[random.nextInt(all.length)]);
    }

    public static RuneStats getRolledStatsForTooltip(ItemStack rune) {
        RuneStats template = RuneStats.get(rune);
        if (template == null || template.isEmpty()) {
            return RuneStats.empty();
        }
        return RuneStats.rollForApplication(template, RandomSource.create());
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
