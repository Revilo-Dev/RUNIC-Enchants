package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public final class GearTooltips {

    private GearTooltips() {}

    private static final char SLOT_FILLED = '⬤';
    private static final char SLOT_EMPTY = '◯';

    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        if (!isGear(stack)) return false;
        if (stack.getItem() instanceof RuneItem || stack.getItem() instanceof EtchingItem) return false;
        if (!shouldOverride(stack)) return false;

        stripModifierContextLines(tooltip);
        stripAllEnchantmentLines(stack, tooltip);

        int durabilityInsertIdx = -1;
        if (stack.getMaxDamage() > 0) {
            durabilityInsertIdx = findAfterVanillaStatLines(tooltip);
        }

        List<Component> enhancements = new ArrayList<>();

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            enhancements.addAll(buildStatLines(stats));
        }

        List<Component> enchLines = buildEnchantmentLines(stack);
        if (!enchLines.isEmpty()) {
            enhancements.addAll(enchLines);
        }

        if (!enhancements.isEmpty()) {
            tooltip.add(Component.literal("Enhancements:").withStyle(ChatFormatting.GRAY));
            tooltip.addAll(enhancements);
        }

        if (durabilityInsertIdx != -1) {
            int idx = durabilityInsertIdx;
            if (idx < 0) idx = Math.min(1, tooltip.size());

            int end = idx;
            while (end < tooltip.size() && isLikelyVanillaStatLine(tooltip.get(end))) end++;
            idx = end;

            tooltip.add(idx, buildDurabilityLine(stack));
        }

        List<Component> slots = buildRuneSlots(stack);
        if (!slots.isEmpty()) {
            tooltip.addAll(slots);
        }

        return true;
    }

    private static boolean isGear(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem
                || item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof ShovelItem
                || item instanceof HoeItem
                || item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof TridentItem
                || item instanceof MaceItem;
    }

    private static boolean shouldOverride(ItemStack stack) {
        if (stack.isEnchanted()) return true;
        if (RuneSlots.capacity(stack) > 0) return true;

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) return true;

        return !GearAttributes.getAll(stack).isEmpty();
    }

    private static void stripModifierContextLines(List<Component> tooltip) {
        tooltip.removeIf(c -> {
            String key = keyOf(c);
            if (key != null && key.startsWith("item.modifiers.")) return true;

            String s = c.getString();
            return s.startsWith("When ") && s.endsWith(":");
        });
    }

    private static int findAfterVanillaStatLines(List<Component> tooltip) {
        int i = 1;

        while (i < tooltip.size() && tooltip.get(i).getString().isBlank()) i++;

        while (i < tooltip.size() && isLikelyVanillaStatLine(tooltip.get(i))) {
            i++;
        }

        return i;
    }

    private static boolean isLikelyVanillaStatLine(Component c) {
        String key = keyOf(c);
        if (key != null) {
            if (key.startsWith("attribute.modifier.") || key.startsWith("attribute.name.")) return true;
        }

        String s = c.getString();
        if (s == null || s.isEmpty()) return false;

        char ch = s.charAt(0);
        boolean startsNumeric = (ch == '+' || ch == '-' || (ch >= '0' && ch <= '9'));
        if (!startsNumeric) return false;

        if (s.contains("%")) return false;
        if (s.endsWith(":")) return false;
        return true;
    }

    private static List<Component> buildStatLines(RuneStats stats) {
        List<Component> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            float v = stats.get(type);
            if (Math.abs(v) < 1.0e-6f) continue;

            out.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.stat." + type.id()))
                            .append(Component.literal(" "))
                            .append(Component.literal(formatSignedPercent(v)).withStyle(ChatFormatting.AQUA))
                            .withStyle(ChatFormatting.WHITE)
            );
        }

        return out;
    }

    private static Component buildDurabilityLine(ItemStack stack) {
        int max = stack.getMaxDamage();
        int curr = max - stack.getDamageValue();
        float pct = max <= 0 ? 1.0f : (float) curr / (float) max;

        ChatFormatting color =
                pct > 0.50f ? ChatFormatting.GREEN :
                        pct > 0.25f ? ChatFormatting.YELLOW :
                                pct > 0.10f ? ChatFormatting.GOLD :
                                        ChatFormatting.RED;

        return Component.literal("Durability: " + curr + "/" + max).withStyle(color);
    }

    private static List<Component> buildEnchantmentLines(ItemStack stack) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (live.isEmpty() && stored.isEmpty()) return List.of();

        LinkedHashMap<String, EnchLine> ordered = new LinkedHashMap<>();
        addEnchantments(ordered, live);
        addEnchantments(ordered, stored);

        List<Component> out = new ArrayList<>();
        for (EnchLine e : ordered.values()) {
            Component name = e.name.copy().withStyle(ChatFormatting.GRAY);
            Component lvl = Component.literal(" " + toRoman(e.level)).withStyle(ChatFormatting.LIGHT_PURPLE);
            out.add(Component.literal("  ").append(name).append(lvl));
        }
        return out;
    }

    private static void addEnchantments(Map<String, EnchLine> out, ItemEnchantments ench) {
        ench.entrySet().forEach(e -> {
            var holder = e.getKey();
            int lvl = e.getIntValue();
            String key = holder.unwrapKey().map(k -> k.location().toString()).orElse(holder.toString());
            out.putIfAbsent(key, new EnchLine(holder.value().description().copy(), lvl));
        });
    }

    private record EnchLine(Component name, int level) {}

    private static List<Component> buildRuneSlots(ItemStack stack) {
        int baseCap = RuneSlots.capacity(stack);
        int neg = GearAttributes.getLevel(stack, GearAttribute.NEGATIVE);
        int cap = Math.max(0, baseCap - neg);
        if (cap <= 0) return List.of();

        int used = RuneSlots.used(stack);
        int u = Math.min(used, cap);
        int rem = Math.max(0, cap - used);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < u; i++) sb.append(SLOT_FILLED);
        for (int i = 0; i < rem; i++) sb.append(SLOT_EMPTY);

        return List.of(
                Component.literal("Rune Slots:").withStyle(ChatFormatting.GRAY),
                Component.literal(sb.toString()).withStyle(ChatFormatting.WHITE)
        );
    }

    private static String formatSignedPercent(float v) {
        float av = Math.abs(v);
        String num = Math.abs(av - Math.round(av)) < 0.001f
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.1f", av);

        return (v >= 0 ? "+" : "-") + num + "%";
    }

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

    private static String keyOf(Component c) {
        if (c == null) return null;
        if (c.getContents() instanceof TranslatableContents tc) {
            return tc.getKey();
        }
        return null;
    }
}
