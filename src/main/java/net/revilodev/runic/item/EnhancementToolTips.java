package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.*;

public final class EnhancementToolTips {
    private static final String STAR = "\u2605";

    private EnhancementToolTips() {}

    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        boolean isRune = stack.getItem() instanceof RuneItem;
        boolean isEtching = stack.getItem() instanceof EtchingItem;
        if (!isRune && !isEtching) return false;

        stripModifierContextLines(tooltip);
        stripAttributeLines(tooltip);
        stripAllEnchantmentLines(stack, tooltip);

        RuneStats stats = RuneStats.get(stack);
        boolean hasStats = stats != null && !stats.isEmpty();

        List<EnchLine> enchLines = collectEnchantments(stack, isEtching);
        boolean hasEnchants = !enchLines.isEmpty();

        if (!hasStats && !hasEnchants) return true;

        if (hasStats) {
            tooltip.addAll(buildStatBlocks(stats, isEtching));
        }

        if (hasEnchants) {
            tooltip.addAll(buildEnchantBlocks(enchLines));
        }

        return true;
    }

    private static List<Component> buildStatBlocks(RuneStats stats, boolean isEtching) {
        List<Component> out = new ArrayList<>();

        for (RuneStatType type : RuneStatType.values()) {
            float present = stats.get(type);
            if (Math.abs(present) < 1.0e-6f) continue;

            int min = isEtching ? type.etchingMinPercent() : type.minPercent();
            int max = isEtching ? type.etchingMaxPercent() : type.maxPercent();

            String range = (min == max) ? ("+" + min + "%") : (min + "% - " + max + "%");
            EnhancementRarity rarity = EnhancementRarities.getStat(type.id());

            MutableComponent name = rarity.applyTo(Component.translatable("tooltip.runic.stat." + type.id()));
            out.add(name);

            String descKey = "tooltip.runic.stat_desc." + type.id();
            if (I18n.exists(descKey)) {
                out.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                String fallback = statDescription(type);
                if (fallback != null && !fallback.isBlank()) {
                    out.add(Component.literal(fallback).withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            out.add(rarityLine(rarity));
            out.add(rangeLine(range));
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
            ResourceLocation id = h.unwrapKey().map(ResourceKey::location).orElse(null);

            int desired = isEtching ? 1 : 2;
            int lvl = Math.min(h.value().getMaxLevel(), desired);

            EnhancementRarity rarity = EnhancementRarities.get(h);
            out.putIfAbsent(key, new EnchLine(h.value().description().copy(), lvl, rarity, id));
        });
    }

    private static List<Component> buildEnchantBlocks(List<EnchLine> enchLines) {
        List<Component> out = new ArrayList<>();
        for (EnchLine e : enchLines) {
            MutableComponent name = e.rarity.applyTo(e.name.copy());
            out.add(name);

            String descKey = descriptionKey(e.id);
            if (descKey != null && I18n.exists(descKey)) {
                out.add(Component.translatable(descKey).withStyle(ChatFormatting.DARK_GRAY));
            }

            out.add(rarityLine(e.rarity));

            String roman = toRoman(e.level);
            if (!roman.isEmpty()) {
                out.add(levelLine(roman, e.rarity));
            }
        }
        return out;
    }

    private record EnchLine(Component name, int level, EnhancementRarity rarity, ResourceLocation id) {}

    private static Component rarityLine(EnhancementRarity rarity) {
        String label = titleize(rarity.key());
        String stars = starString(rarity.stars());
        return rarity.applyTo(Component.literal(label + stars));
    }

    private static Component levelLine(String roman, EnhancementRarity rarity) {
        return Component.literal("Level: ")
                .withStyle(ChatFormatting.GRAY)
                .append(rarity.applyTo(Component.literal(roman)));
    }

    private static Component rangeLine(String range) {
        return Component.literal("Range: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal("[" + range + "]").withStyle(ChatFormatting.AQUA));
    }

    private static String descriptionKey(ResourceLocation id) {
        if (id == null) return null;
        return "tooltip.runic." + id.getPath();
    }

    private static String starString(int stars) {
        if (stars <= 0) return "";
        return " " + STAR.repeat(stars);
    }

    private static String titleize(String key) {
        if (key == null || key.isBlank()) return "";
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
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

    private static void stripModifierContextLines(List<Component> tooltip) {
        tooltip.removeIf(c -> {
            String key = keyOf(c);
            if (key != null && key.startsWith("item.modifiers.")) return true;

            String s = c.getString();
            return s.startsWith("When ") && s.endsWith(":");
        });
    }

    private static void stripAttributeLines(List<Component> tooltip) {
        tooltip.removeIf(c -> {
            String key = keyOf(c);
            if (key != null) {
                if (key.startsWith("attribute.modifier.") || key.startsWith("attribute.name.")) return true;
            }

            String s = c.getString();
            if (s == null || s.isEmpty()) return false;
            char ch = s.charAt(0);
            return (ch == '+' || ch == '-' || (ch >= '0' && ch <= '9'));
        });
    }

    private static String keyOf(Component c) {
        if (c == null) return null;
        if (c.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
            return tc.getKey();
        }
        return null;
    }
}
