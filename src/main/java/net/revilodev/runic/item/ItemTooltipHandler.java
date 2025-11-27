package net.revilodev.runic.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        stripVanillaAttributes(tooltip);
        appendStatsAndSections(stack, tooltip);
        recolorEnchantLines(stack, tooltip);
    }

    private static void stripVanillaAttributes(List<Component> tooltip) {
        int index = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            String s = tooltip.get(i).getString();
            if (s.startsWith("When in ") || s.startsWith("When on ")) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            while (tooltip.size() > index) {
                tooltip.remove(tooltip.size() - 1);
            }
        }
    }

    private static void appendStatsAndSections(ItemStack stack, List<Component> tooltip) {
        boolean isRune = stack.is(ModItems.ENHANCED_RUNE.get());
        RuneStats stats = RuneStats.get(stack);
        boolean hasStats = stats != null && !stats.isEmpty();

        int maxDur = stack.getMaxDamage();
        int currDur = maxDur > 0 ? (maxDur - stack.getDamageValue()) : 0;

        int slotCap = RuneSlots.capacity(stack);
        boolean hasSlots = slotCap > 0;

        boolean hasEnhancementStats = false;
        boolean hasRolledItemStats = false;

        if (hasStats) {
            for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
                float v = e.getValue();
                if (v != 0f) {
                    if (isRune) {
                        hasEnhancementStats = true;
                    } else {
                        hasRolledItemStats = true;
                    }
                }
            }
        }

        if (!isRune) {
            boolean hasItemStatsToShow = false;

            float dmg = hasStats ? stats.get(RuneStatType.ATTACK_DAMAGE) : 0f;
            float spd = hasStats ? stats.get(RuneStatType.ATTACK_SPEED) : 0f;
            float mine = hasStats ? stats.get(RuneStatType.MINING_SPEED) : 0f;
            float sweep = hasStats ? stats.get(RuneStatType.SWEEPING_RANGE) : 0f;

            if (dmg != 0f || spd != 0f || mine != 0f || sweep != 0f || maxDur > 0) {
                hasItemStatsToShow = true;
            }

            if (hasItemStatsToShow) {
                tooltip.add(Component.empty());
                tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GOLD));

                addPercentLine(tooltip, "Attack Damage", dmg);
                addPercentLine(tooltip, "Attack Speed", spd);
                addPercentLine(tooltip, "Mining Speed", mine);
                addPercentLine(tooltip, "Sweeping Range", sweep);

                if (maxDur > 0) {
                    tooltip.add(Component.literal("  Durability: " + currDur + "/" + maxDur)
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }

        if (isRune && hasEnhancementStats) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Enhancements:").withStyle(ChatFormatting.GRAY));

            for (RuneStatType t : stats.view().keySet()) {
                if (t == null) continue;
                String range = t.minPercent() + "–" + t.maxPercent() + "%";
                tooltip.add(
                        Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.stat." + t.id()))
                                .append(": ")
                                .append(Component.literal(range).withStyle(ChatFormatting.AQUA))
                );
            }
        }

        if (!isRune && hasRolledItemStats) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Enhanced Stats:").withStyle(ChatFormatting.GRAY));

            for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
                RuneStatType type = e.getKey();
                float val = e.getValue();
                if (val == 0f) continue;

                String valStr;
                if (Math.abs(val - Math.round(val)) < 0.001f) {
                    valStr = "+" + (int) val + "%";
                } else {
                    valStr = String.format(Locale.ROOT, "+%.1f%%", val);
                }

                tooltip.add(
                        Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.stat." + type.id()))
                                .append(": ")
                                .append(Component.literal(valStr).withStyle(ChatFormatting.AQUA))
                );
            }
        }

        if (hasSlots) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Rune Slots:").withStyle(ChatFormatting.GRAY));

            int used = RuneSlots.used(stack);
            int remaining = Math.max(0, slotCap - used);

            tooltip.add(
                    RuneSlots.bar(stack).copy()
                            .withStyle(remaining > 0 ? ChatFormatting.GRAY : ChatFormatting.RED)
            );
        }
    }

    private static void addPercentLine(List<Component> tooltip, String name, float value) {
        if (value == 0f) return;
        String str;
        if (Math.abs(value - Math.round(value)) < 0.001f) {
            str = String.format(Locale.ROOT, "+%d%%", (int) value);
        } else {
            str = String.format(Locale.ROOT, "+%.1f%%", value);
        }
        tooltip.add(Component.literal("  " + name + ": " + str)
                .withStyle(ChatFormatting.AQUA));
    }

    private static void recolorEnchantLines(ItemStack stack, List<Component> tooltip) {
        boolean isRune = stack.is(ModItems.ENHANCED_RUNE.get());

        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        boolean recolorGear = !live.isEmpty();
        boolean recolorRune = isRune && (!live.isEmpty() || !stored.isEmpty());
        if (!recolorGear && !recolorRune) return;

        List<Component> vanillaStrings = new ArrayList<>();
        List<Component> newLines = new ArrayList<>();

        if (recolorGear) collectRebuilt(live, vanillaStrings, newLines, isRune);
        if (recolorRune && !stored.isEmpty()) collectRebuilt(stored, vanillaStrings, newLines, isRune);

        if (vanillaStrings.isEmpty()) return;

        int firstIdx = -1;
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            String s = tooltip.get(i).getString();
            boolean removed = false;
            for (Component v : vanillaStrings) {
                if (s.equals(v.getString())) {
                    firstIdx = i;
                    tooltip.remove(i);
                    removed = true;
                    break;
                }
            }
            if (removed && i == 0) break;
        }

        int insertAt = firstIdx;
        if (insertAt != -1) {
            tooltip.addAll(insertAt, newLines);
            insertAt += newLines.size();
        }

        if (isRune && (recolorGear || !stored.isEmpty())) {
            appendRuneShiftDescriptions(tooltip, insertAt, live, stored);
        }
    }

    private static void collectRebuilt(ItemEnchantments enchants,
                                       List<Component> vanillaOut,
                                       List<Component> rebuiltOut,
                                       boolean isRune) {
        for (Object2IntMap.Entry<Holder<Enchantment>> e : enchants.entrySet()) {
            Holder<Enchantment> holder = e.getKey();
            int level = e.getIntValue();

            Component vanilla = holder.value().getFullname(holder, level);
            vanillaOut.add(vanilla);

            EnhancementRarity rarity = EnhancementRarities.get(holder);

            if (isRune) {
                Component grayLine = Component.literal(vanilla.getString())
                        .withStyle(Style.EMPTY.withColor(0xAAAAAA).withItalic(false));
                rebuiltOut.add(grayLine);

                String key = rarity.key();
                String rarityName = key.substring(0, 1).toUpperCase(Locale.ROOT) + key.substring(1);
                Component rarityLine = Component.literal(rarityName)
                        .withStyle(s -> s.withColor(rarity.color()).withItalic(false));
                rebuiltOut.add(rarityLine);
            } else {
                Component colored = Component.literal(vanilla.getString())
                        .withStyle(s -> s.withColor(rarity.color()).withItalic(false));
                rebuiltOut.add(colored);
            }
        }
    }

    private static void appendRuneShiftDescriptions(List<Component> tooltip,
                                                    int insertAt,
                                                    ItemEnchantments... sets) {
        LinkedHashSet<ResourceKey<Enchantment>> keys = new LinkedHashSet<>();
        for (ItemEnchantments set : sets) {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : set.entrySet()) {
                e.getKey().unwrapKey().ifPresent(keys::add);
            }
        }
        if (keys.isEmpty()) return;

        boolean shift = Screen.hasShiftDown();

        if (insertAt >= 0 && insertAt <= tooltip.size()) {
            tooltip.add(insertAt, Component.empty());
            insertAt++;
        } else {
            tooltip.add(Component.empty());
            insertAt = tooltip.size();
        }

        if (!shift) {
            tooltip.add(insertAt, Component.literal("Hold \u00A7eShift\u00A7r for effects")
                    .withStyle(Style.EMPTY.withColor(0x777777).withItalic(false)));
            return;
        }

        tooltip.add(insertAt++, Component.literal("Effects")
                .withStyle(Style.EMPTY.withColor(0xBBBBBB).withItalic(false)));

        for (ResourceKey<Enchantment> key : keys) {
            ResourceLocation loc = key.location();
            String langKey = "tooltip.runic." + loc.getPath();
            Component line = Component.translatable(langKey)
                    .withStyle(Style.EMPTY.withItalic(false));
            tooltip.add(insertAt++, line);
        }
    }
}
