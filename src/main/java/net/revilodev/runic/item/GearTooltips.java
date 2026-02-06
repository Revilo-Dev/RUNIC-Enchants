package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.gear.GearAttribute;
import net.revilodev.runic.gear.GearAttributes;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
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

        moveVanillaStatsToTop(tooltip);
        stripAllEnchantmentLines(stack, tooltip);

        boolean showDetails = Screen.hasControlDown();

        RuneStats stats = RuneStats.get(stack);
        boolean hasRunicStats = stats != null && !stats.isEmpty();
        List<Component> runicStats = hasRunicStats
                ? buildStatLines(stats, showDetails)
                : List.of();

        List<Component> enchLines = buildEnchantmentLines(stack, showDetails);

        int insertAt = afterVanillaStatLines(tooltip);

        if (stack.getMaxDamage() > 0) {
            tooltip.add(insertAt, buildDurabilityLine(stack));
            insertAt++;
        }

        if (hasRunicStats) {
            tooltip.add(insertAt, Component.literal("Modded Stats:").withStyle(ChatFormatting.GRAY));
            insertAt++;
            tooltip.addAll(insertAt, runicStats);
            insertAt += runicStats.size();
        }

        if (!enchLines.isEmpty()) {
            tooltip.add(insertAt, Component.literal("Enchants:").withStyle(ChatFormatting.GRAY));
            insertAt++;
            tooltip.addAll(insertAt, enchLines);
            insertAt += enchLines.size();
        }

        List<Component> slots = buildRuneSlots(stack);
        if (!slots.isEmpty()) {
            tooltip.addAll(insertAt, slots);
            insertAt += slots.size();
        }

        if (!showDetails && (hasRunicStats || !enchLines.isEmpty())) {
            tooltip.add(insertAt, Component.literal("(Ctrl for details)").withStyle(ChatFormatting.DARK_GRAY));
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

    private static int findFirstVanillaStatLine(List<Component> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (isLikelyVanillaStatLine(tooltip.get(i))) return i;
        }
        return -1;
    }

    private static boolean isLikelyVanillaStatLine(Component c) {
        String key = keyOf(c);
        if (key != null) {
            if (key.startsWith("attribute.modifier.") || key.startsWith("attribute.name.")) return true;
        }

        String s = c.getString();
        if (s == null || s.isEmpty()) return false;

        String trimmed = s.trim();
        if (trimmed.isEmpty()) return false;

        char ch = trimmed.charAt(0);
        boolean startsNumeric = (ch == '+' || ch == '-' || (ch >= '0' && ch <= '9'));
        if (!startsNumeric) return false;

        if (trimmed.contains("%")) return false;
        if (trimmed.endsWith(":")) return false;
        return true;
    }

    private static void moveVanillaStatsToTop(List<Component> tooltip) {
        if (tooltip.size() <= 1) return;

        List<Component> statLines = new ArrayList<>();
        List<Integer> removeIdx = new ArrayList<>();

        for (int i = 0; i < tooltip.size(); i++) {
            Component c = tooltip.get(i);
            if (isAttributeHeader(c)) {
                removeIdx.add(i);
                continue;
            }
            if (isLikelyVanillaStatLine(c)) {
                statLines.add(c);
                removeIdx.add(i);
            }
        }

        if (statLines.isEmpty()) {
            for (int i = removeIdx.size() - 1; i >= 0; i--) {
                tooltip.remove((int) removeIdx.get(i));
            }
            return;
        }

        for (int i = removeIdx.size() - 1; i >= 0; i--) {
            tooltip.remove((int) removeIdx.get(i));
        }

        int insertAt = Math.min(1, tooltip.size());
        tooltip.addAll(insertAt, statLines);
    }

    private static int afterVanillaStatLines(List<Component> tooltip) {
        int statsStart = findFirstVanillaStatLine(tooltip);
        if (statsStart == -1) return Math.min(1, tooltip.size());

        int end = statsStart;
        while (end < tooltip.size() && isLikelyVanillaStatLine(tooltip.get(end))) {
            end++;
        }
        return end;
    }

    private static List<Component> buildStatLines(RuneStats stats, boolean showDetails) {
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

            if (showDetails) {
                String descKey = "tooltip.runic.stat_desc." + type.id();
                if (I18n.exists(descKey)) {
                    out.add(Component.literal("  ")
                            .append(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY)));
                } else {
                    String fallback = statDescription(type);
                    if (fallback != null && !fallback.isBlank()) {
                        out.add(Component.literal("  ")
                                .append(Component.literal(fallback).withStyle(ChatFormatting.DARK_GRAY)));
                    }
                }
            }
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

    private static List<Component> buildEnchantmentLines(ItemStack stack, boolean showDetails) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (live.isEmpty() && stored.isEmpty()) return List.of();

        LinkedHashMap<String, EnchLine> ordered = new LinkedHashMap<>();
        addEnchantments(ordered, live);
        addEnchantments(ordered, stored);

        List<Component> out = new ArrayList<>();
        for (EnchLine e : ordered.values()) {
            Component name = e.rarity.applyTo(e.name.copy());
            String roman = toRoman(e.level);
            Component lvl = roman.isEmpty()
                    ? Component.empty()
                    : Component.literal(" " + roman).withStyle(e.rarity.style());
            out.add(Component.literal("  ").append(name).append(lvl));

            if (showDetails) {
                String descKey = descriptionKey(e.id);
                if (descKey != null && I18n.exists(descKey)) {
                    out.add(Component.literal("  ")
                            .append(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY)));
                }
            }
        }
        return out;
    }

    private static void addEnchantments(Map<String, EnchLine> out, ItemEnchantments ench) {
        ench.entrySet().forEach(e -> {
            var holder = e.getKey();
            int lvl = e.getIntValue();
            String key = holder.unwrapKey().map(k -> k.location().toString()).orElse(holder.toString());
            EnhancementRarity rarity = EnhancementRarities.get(holder);
            ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
            out.putIfAbsent(key, new EnchLine(holder.value().description().copy(), lvl, rarity, id));
        });
    }

    private record EnchLine(Component name, int level, EnhancementRarity rarity, ResourceLocation id) {}

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
                Component.literal("Rune Slots: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(sb.toString()).withStyle(ChatFormatting.WHITE))
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

    private static String descriptionKey(ResourceLocation id) {
        if (id == null) return null;
        return "tooltip.runic." + id.getPath();
    }

    private static String statDescription(RuneStatType type) {
        return switch (type) {
            case ATTACK_SPEED -> "Increases attack speed.";
            case ATTACK_DAMAGE -> "Increases attack damage.";
            case ATTACK_RANGE -> "Increases melee reach.";
            case MOVEMENT_SPEED -> "Increases movement speed.";
            case SWEEPING_RANGE -> "Increases sweeping attack range.";
            case DURABILITY -> "Increases maximum durability.";
            case RESISTANCE -> "Reduces incoming damage.";
            case FIRE_RESISTANCE -> "Reduces fire damage.";
            case BLAST_RESISTANCE -> "Reduces explosion damage.";
            case PROJECTILE_RESISTANCE -> "Reduces projectile damage.";
            case KNOCKBACK_RESISTANCE -> "Reduces knockback taken.";
            case MINING_SPEED -> "Increases mining speed.";
            case UNDEAD_DAMAGE -> "Increases damage to undead.";
            case NETHER_DAMAGE -> "Increases damage to nether mobs.";
            case HEALTH -> "Increases maximum health.";
            case STUN_CHANCE -> "Chance to stun on hit.";
            case FLAME_CHANCE -> "Chance to ignite targets.";
            case BLEEDING_CHANCE -> "Chance to apply bleeding.";
            case SHOCKING_CHANCE -> "Chance to shock targets.";
            case POISON_CHANCE -> "Chance to poison targets.";
            case WITHERING_CHANCE -> "Chance to wither targets.";
            case WEAKENING_CHANCE -> "Chance to weaken targets.";
            case HEALING_EFFICIENCY -> "Improves healing received.";
            case DRAW_SPEED -> "Increases bow draw speed.";
            case TOUGHNESS -> "Increases armor toughness.";
            case FREEZING_CHANCE -> "Chance to freeze targets.";
            case LEECHING_CHANCE -> "Chance to heal on hit.";
            case BONUS_CHANCE -> "Chance to fire an extra projectile.";
            case JUMP_HEIGHT -> "Increases jump height.";
            case POWER -> "Increases ranged damage.";
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

    private static boolean isAttributeHeader(Component c) {
        String key = keyOf(c);
        if (key != null && key.startsWith("item.modifiers.")) return true;
        String s = c.getString();
        return s != null && s.startsWith("When ") && s.endsWith(":");
    }
}

