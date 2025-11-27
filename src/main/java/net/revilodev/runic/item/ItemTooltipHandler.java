package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
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

import java.util.*;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipHandler {

    private static final ResourceLocation ENHANCED_RUNE_ID =
            ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "enhanced_rune");

    private static final String ICON_SWORD = "\uEFE5";
    private static final String ICON_PICKAXE = "\uEFE6";
    private static final String ICON_STAR = "\uEEF2";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        stripVanillaAttributeLines(tooltip);
        stripTagLines(tooltip);

        boolean isRune = stack.is(ModItems.ENHANCED_RUNE.get());

        appendItemStats(stack, tooltip, isRune);
        appendRuneStats(stack, tooltip, isRune);
        appendRuneSlots(stack, tooltip);
        recolorEnchants(stack, tooltip, isRune);
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // REMOVE VANILLA WEAPON ATTRIBUTES
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void stripVanillaAttributeLines(List<Component> tooltip) {
        int idx = -1;

        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().startsWith("When in ")) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            while (tooltip.size() > idx)
                tooltip.remove(tooltip.size() - 1);
        }
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // REMOVE ITEM & BLOCK TAGS
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void stripTagLines(List<Component> tooltip) {
        tooltip.removeIf(c -> {
            String s = c.getString().toLowerCase(Locale.ROOT);
            return s.startsWith("tags:")
                    || s.startsWith("item tags:")
                    || s.startsWith("block tags:")
                    || s.startsWith("#")
                    || s.contains("tag:") || s.contains("tags:");
        });
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ITEM STATS (NOT ON RUNES, ONLY ON WEAPONS & TOOLS)
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void appendItemStats(ItemStack stack, List<Component> tooltip, boolean isRune) {
        if (isRune) return;

        Item item = stack.getItem();

        boolean isWeapon =
                item instanceof SwordItem ||
                        item instanceof AxeItem ||
                        item instanceof TridentItem;

        boolean isTool =
                item instanceof DiggerItem;   // ✔ correct for 1.21.1


        if (!isWeapon && !isTool && !(item instanceof TieredItem)) {
            return; // DO NOT show stats on misc items
        }

        tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GOLD));

        // --- attack damage and speed ---
        double baseDmg = 1.0;
        double baseSpd = 4.0;

        double dmg = baseDmg;
        double spd = baseSpd;

        ItemAttributeModifiers mods =
                stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        for (ItemAttributeModifiers.Entry e : mods.modifiers()) {
            if (!e.slot().test(EquipmentSlot.MAINHAND)) continue;

            if (e.attribute().is(Attributes.ATTACK_DAMAGE)) {
                dmg += e.modifier().amount();
            }
            if (e.attribute().is(Attributes.ATTACK_SPEED)) {
                spd += e.modifier().amount();
            }
        }

        tooltip.add(statLine(ICON_SWORD + " Attack Damage", dmg));
        tooltip.add(statLine(ICON_SWORD + " Attack Speed", spd));

        // --- attack range (future expansion) ---
        tooltip.add(statLine("Attack Range", 3.0));

        // --- only tools get mining speed ---
        if (isTool) {
            double mining = stack.getDestroySpeed(Blocks.STONE.defaultBlockState());
            tooltip.add(statLine(ICON_PICKAXE + " Mining Speed", mining));
        }

        // --- durability (color-coded) ---
        int max = stack.getMaxDamage();
        if (max > 0) {
            int curr = max - stack.getDamageValue();
            float pct = (float) curr / max;

            ChatFormatting color =
                    pct > 0.50f ? ChatFormatting.GREEN :
                            pct > 0.25f ? ChatFormatting.YELLOW :
                                    pct > 0.10f ? ChatFormatting.GOLD :
                                            ChatFormatting.RED;

            tooltip.add(Component.literal("  Durability: " + curr + "/" + max)
                    .withStyle(color));
        }
    }

    private static Component statLine(String name, double val) {
        String fmt = Math.abs(val - Math.round(val)) < 0.001
                ? String.format(Locale.ROOT, "%.0f", val)
                : String.format(Locale.ROOT, "%.2f", val);
        return Component.literal("  " + name + ": " + fmt)
                .withStyle(ChatFormatting.GRAY);
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // RUNE STATS (ONLY WHEN PRESENT)
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void appendRuneStats(ItemStack stack, List<Component> tooltip, boolean isRune) {
        RuneStats stats = RuneStats.get(stack);
        if (stats == null || stats.isEmpty()) return;

        if (isRune) {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Enhancements:").withStyle(ChatFormatting.GRAY));

            for (RuneStatType type : stats.view().keySet()) {
                String range = type.minPercent() + "–" + type.maxPercent() + "%";
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.runic.stat." + type.id()))
                        .append(": ")
                        .append(Component.literal(range).withStyle(ChatFormatting.AQUA)));
            }
        } else {
            tooltip.add(Component.empty());
            tooltip.add(Component.literal("Enhanced Stats:").withStyle(ChatFormatting.GRAY));

            for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
                float v = e.getValue();
                if (v == 0) continue;

                String vStr = Math.abs(v - Math.round(v)) < 0.001
                        ? "+" + (int) v + "%"
                        : "+" + String.format(Locale.ROOT, "%.1f%%", v);

                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.runic.stat." + e.getKey().id()))
                        .append(": ")
                        .append(Component.literal(vStr).withStyle(ChatFormatting.AQUA)));
            }
        }
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // RUNE SLOTS BAR
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void appendRuneSlots(ItemStack stack, List<Component> tooltip) {
        int cap = RuneSlots.capacity(stack);
        if (cap <= 0) return;

        int used = RuneSlots.used(stack);
        int remaining = Math.max(0, cap - used);

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Rune Slots:").withStyle(ChatFormatting.GRAY));
        tooltip.add(
                RuneSlots.bar(stack).copy()
                        .withStyle(remaining > 0 ? ChatFormatting.GRAY : ChatFormatting.RED)
        );
    }

    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // ENCHANT RECOLOR + SHIFT-DESCRIPTIONS
    //━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    private static void recolorEnchants(ItemStack stack, List<Component> tooltip, boolean isRune) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        boolean recolor = !live.isEmpty() || (isRune && !stored.isEmpty());
        if (!recolor) return;

        List<Component> vanilla = new ArrayList<>();
        List<Component> rebuilt = new ArrayList<>();

        collectEnchantDisplay(live, vanilla, rebuilt, isRune);
        collectEnchantDisplay(stored, vanilla, rebuilt, isRune);

        if (vanilla.isEmpty()) return;

        int first = -1;
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            String s = tooltip.get(i).getString();
            for (Component v : vanilla) {
                if (s.equals(v.getString())) {
                    first = i;
                    tooltip.remove(i);
                    break;
                }
            }
        }

        if (first != -1) {
            tooltip.addAll(first, rebuilt);
        }
    }

    private static void collectEnchantDisplay(
            ItemEnchantments ench, List<Component> vanilla, List<Component> rebuilt, boolean isRune
    ) {
        ench.entrySet().forEach(e -> {
            Holder<Enchantment> holder = e.getKey();
            int lvl = e.getIntValue();

            Component vn = holder.value().getFullname(holder, lvl);
            vanilla.add(vn);

            EnhancementRarity rarity = EnhancementRarities.get(holder);

            if (isRune) {
                rebuilt.add(vn.copy().withStyle(ChatFormatting.GRAY));
                rebuilt.add(Component.literal(
                                rarity.key().substring(0,1).toUpperCase(Locale.ROOT) +
                                        rarity.key().substring(1))
                        .withStyle(style -> style.withColor(rarity.color())));
            } else {
                rebuilt.add(vn.copy().withStyle(style -> style.withColor(rarity.color())));
            }
        });
    }
}
