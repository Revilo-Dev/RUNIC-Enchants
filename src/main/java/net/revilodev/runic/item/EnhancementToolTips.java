package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public final class EnhancementToolTips {

    private EnhancementToolTips() {}

    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        boolean isRune = stack.getItem() instanceof RuneItem;
        boolean isEtching = stack.getItem() instanceof EtchingItem;
        if (!isRune && !isEtching) return false;

        stripAllEnchantmentLines(stack, tooltip);

        RuneStats stats = RuneStats.get(stack);
        boolean hasStats = stats != null && !stats.isEmpty();

        List<EnchLine> enchLines = collectEnchantments(stack, isEtching);
        boolean hasEnchants = !enchLines.isEmpty();

        if (!hasStats && !hasEnchants) return true;

        if (hasStats) {
            tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GRAY));
            tooltip.addAll(buildStatLines(stats, isEtching));
        }

        if (hasEnchants) {
            tooltip.add(Component.literal("Enhancements:").withStyle(ChatFormatting.GRAY));
            for (EnchLine e : enchLines) {
                MutableComponent line = Component.literal("  ")
                        .append(e.name.copy().withStyle(ChatFormatting.GRAY));

                String roman = toRoman(e.level);
                if (!roman.isEmpty()) {
                    line.append(Component.literal(" " + roman).withStyle(ChatFormatting.LIGHT_PURPLE));
                }

                tooltip.add(line);
            }
        }

        return true;
    }

    private static List<Component> buildStatLines(RuneStats stats, boolean isEtching) {
        List<Component> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            float present = stats.get(type);
            if (Math.abs(present) < 1.0e-6f) continue;

            int min = isEtching ? type.etchingMinPercent() : type.minPercent();
            int max = isEtching ? type.etchingMaxPercent() : type.maxPercent();

            String range = (min == max) ? ("+" + min + "%") : (min + "% - " + max + "%");

            MutableComponent line = Component.literal("  ")
                    .append(Component.translatable("tooltip.runic.stat." + type.id()))
                    .append(Component.literal(": "))
                    .append(Component.literal(range).withStyle(ChatFormatting.AQUA));

            out.add(line.withStyle(ChatFormatting.WHITE));
        }

        return out;
    }

    private static List<EnchLine> collectEnchantments(ItemStack stack, boolean isEtching) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

        LinkedHashMap<String, EnchLine> ordered = new LinkedHashMap<>();
        addEnchantments(ordered, stored, isEtching);
        addEnchantments(ordered, direct, isEtching);

        return new ArrayList<>(ordered.values());
    }

    private static void addEnchantments(Map<String, EnchLine> out, ItemEnchantments ench, boolean isEtching) {
        ench.entrySet().forEach(e -> {
            Holder<Enchantment> h = e.getKey();
            String key = h.unwrapKey().map(k -> k.location().toString()).orElse(h.toString());

            int desired = isEtching ? 1 : 2;
            int lvl = Math.min(h.value().getMaxLevel(), desired);

            out.putIfAbsent(key, new EnchLine(h.value().description().copy(), lvl));
        });
    }

    private record EnchLine(Component name, int level) {}

    private static String toRoman(int v) {
        if (v <= 0) return "";
        if (v >= 10) return "X";
        return switch (v) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            default -> "";
        };
    }

    private static void stripAllEnchantmentLines(ItemStack stack, List<Component> tooltip) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (live.isEmpty() && stored.isEmpty()) return;

        Set<String> vanillaLines = new HashSet<>();
        live.entrySet().forEach(e -> vanillaLines.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).getString()));
        stored.entrySet().forEach(e -> vanillaLines.add(Enchantment.getFullname(e.getKey(), e.getIntValue()).getString()));

        tooltip.removeIf(line -> vanillaLines.contains(line.getString()));
    }
}
