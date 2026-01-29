package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.revilodev.runic.RunicMod;
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

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipHandler {
    private static final String ROOT = "runic";
    private static final String PREVIEW_DELTA = "preview_delta";

    private static final String ICON_HELMET = "\uefe1";
    private static final String ICON_CHEST = "\uefe2";
    private static final String ICON_LEGS = "\uefe3";
    private static final String ICON_BOOTS = "\uefe4";
    private static final String ICON_SWORD = "\uefe5";
    private static final String ICON_PICKAXE = "\uefe6";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();

        stripVanillaAttributeLines(tooltip);
        stripAllEnchantmentLines(stack, tooltip);

        boolean isRune = stack.getItem() instanceof RuneItem;
        boolean isEtching = stack.getItem() instanceof EtchingItem;
        boolean isEnhancement = isRune || isEtching;

        if (isEnhancement) {
            appendEnhancementHeader(stack, tooltip, isEtching);
        }

        appendItemStats(stack, tooltip);
        appendRuneStats(stack, tooltip, isEnhancement, isEtching);
        appendGearAttributes(stack, tooltip);
        appendRuneSlots(stack, tooltip);

        if (!isEnhancement) {
            appendEnhancementsSection(stack, tooltip);
        }
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

    private static void stripVanillaAttributeLines(List<Component> tooltip) {
        int idx = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getString().startsWith("When ")) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            while (tooltip.size() > idx) tooltip.remove(tooltip.size() - 1);
        }
    }

    private static void appendEnhancementHeader(ItemStack stack, List<Component> tooltip, boolean isEtching) {
        tooltip.add(Component.literal("Apply in an Artisans Workbench").withStyle(ChatFormatting.DARK_GRAY));

        RuneStats stats = RuneStats.get(stack);
        boolean hasStats = stats != null && !stats.isEmpty();

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        boolean hasEffect = !stored.isEmpty() || !direct.isEmpty();

        Component typeLine =
                !hasStats && hasEffect ? Component.literal(isEtching ? "Effect Etching" : "Effect Rune").withStyle(ChatFormatting.AQUA)
                        : hasStats && !hasEffect ? Component.literal(isEtching ? "Stat Etching" : "Stat Rune").withStyle(ChatFormatting.GOLD)
                        : Component.literal(isEtching ? "Mixed Etching" : "Mixed Rune").withStyle(ChatFormatting.LIGHT_PURPLE);

        tooltip.add(typeLine);

        tooltip.add(Component.literal("Enhancement:").withStyle(ChatFormatting.GRAY));

        Holder<Enchantment> effect = isEtching
                ? EtchingItem.getPrimaryEffectEnchantment(stack)
                : RuneItem.getPrimaryEffectEnchantment(stack);

        if (effect != null) {
            tooltip.add(
                    Component.literal("  ")
                            .append(effect.value().description().copy())
                            .withStyle(ChatFormatting.AQUA)
            );
        } else {
            tooltip.add(Component.literal("  None").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static CompoundTag getPreviewDelta(ItemStack stack) {
        CustomData cd = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag root = cd.copyTag();

        if (root.contains(PREVIEW_DELTA, Tag.TAG_COMPOUND)) {
            return root.getCompound(PREVIEW_DELTA);
        }

        if (root.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag runic = root.getCompound(ROOT);
            if (runic.contains(PREVIEW_DELTA, Tag.TAG_COMPOUND)) {
                return runic.getCompound(PREVIEW_DELTA);
            }
        }

        return null;
    }

    private static Double getItemStatDelta(CompoundTag delta, String key) {
        if (delta == null) return null;
        if (!delta.contains("item_stats", Tag.TAG_COMPOUND)) return null;
        CompoundTag is = delta.getCompound("item_stats");
        if (!is.contains(key, Tag.TAG_DOUBLE)) return null;
        double v = is.getDouble(key);
        return Math.abs(v) < 1e-9 ? null : v;
    }

    private static Float getRuneStatDelta(CompoundTag delta, String statId) {
        if (delta == null) return null;
        if (!delta.contains("rune_stats", Tag.TAG_COMPOUND)) return null;
        CompoundTag rs = delta.getCompound("rune_stats");
        if (!rs.contains(statId, Tag.TAG_FLOAT)) return null;
        float v = rs.getFloat(statId);
        return Math.abs(v) < 1e-6 ? null : v;
    }

    private static Integer getDurMaxDelta(CompoundTag delta) {
        if (delta == null) return null;
        if (!delta.contains("dur_max", Tag.TAG_INT)) return null;
        int v = delta.getInt("dur_max");
        return v == 0 ? null : v;
    }

    private static Integer getDurRemDelta(CompoundTag delta) {
        if (delta == null) return null;
        if (!delta.contains("dur_rem", Tag.TAG_INT)) return null;
        int v = delta.getInt("dur_rem");
        return v == 0 ? null : v;
    }

    private static void appendItemStats(ItemStack stack, List<Component> tooltip) {
        CompoundTag delta = getPreviewDelta(stack);
        Item item = stack.getItem();

        boolean isSword = item instanceof SwordItem;
        boolean isTrident = item instanceof TridentItem;
        boolean isBowLike = item instanceof BowItem || item instanceof CrossbowItem;
        boolean isMace = item instanceof MaceItem;

        boolean isWeapon = isSword || isMace || item instanceof AxeItem || isTrident || isBowLike;

        boolean isToolForMining =
                item instanceof PickaxeItem ||
                        item instanceof ShovelItem ||
                        item instanceof AxeItem ||
                        item instanceof HoeItem;

        boolean isArmor = item instanceof ArmorItem;

        boolean isDurabilityOnly =
                !isWeapon && !isToolForMining && !isArmor && stack.getMaxDamage() > 0;

        if (isDurabilityOnly) {
            int max = stack.getMaxDamage();
            if (max > 0) {
                int curr = max - stack.getDamageValue();
                float pct = (float) curr / max;

                ChatFormatting color =
                        pct > 0.50f ? ChatFormatting.GREEN :
                                pct > 0.25f ? ChatFormatting.YELLOW :
                                        pct > 0.10f ? ChatFormatting.GOLD :
                                                ChatFormatting.RED;

                Integer dMax = getDurMaxDelta(delta);
                Integer dRem = getDurRemDelta(delta);

                String suffix = "";
                if (dMax != null) suffix = formatSignedInt(dMax);
                else if (dRem != null) suffix = formatSignedInt(dRem);

                tooltip.add(Component.literal("Durability: " + curr + "/" + max + suffix).withStyle(color));
            }
            return;
        }

        if (!isWeapon && !isToolForMining && !isArmor) return;

        tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GOLD));

        if (isArmor) {
            ArmorItem armor = (ArmorItem) item;
            EquipmentSlot slot = armor.getEquipmentSlot();

            class ArmorStats {
                double armor = 0;
                double toughBase = 0;
                double toughMult = 0;
                double knockBase = 0;
                double knockMult = 0;
                double move = 0;
                double soul = 0;
                double healthBase = 0;
                double healthMult = 0;
            }

            ArmorStats a = new ArmorStats();

            stack.forEachModifier(slot, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ARMOR)) a.armor += mod.amount();
                else if (attr.is(Attributes.ARMOR_TOUGHNESS)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) a.toughMult += mod.amount();
                    else a.toughBase += mod.amount();
                } else if (attr.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) a.knockMult += mod.amount();
                    else a.knockBase += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) a.move += mod.amount() * 100.0;
                    else a.move += mod.amount();
                } else if (attr.is(Attributes.MOVEMENT_EFFICIENCY)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) a.soul += mod.amount() * 100.0;
                    else a.soul += mod.amount();
                } else if (attr.is(Attributes.MAX_HEALTH)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) a.healthMult += mod.amount();
                    else a.healthBase += mod.amount();
                }
            });

            double toughness = a.toughBase * (1.0 + a.toughMult);
            double knock = a.knockBase * (1.0 + a.knockMult);
            double health = a.healthBase * (1.0 + a.healthMult);

            String armorIcon = switch (slot) {
                case HEAD -> ICON_HELMET;
                case CHEST -> ICON_CHEST;
                case LEGS -> ICON_LEGS;
                case FEET -> ICON_BOOTS;
                default -> ICON_HELMET;
            };

            if (a.armor != 0) {
                tooltip.add(statLine(armorIcon,
                        Component.translatable(Attributes.ARMOR.value().getDescriptionId()),
                        a.armor,
                        getItemStatDelta(delta, "armor")));
            }
            if (toughness != 0) {
                tooltip.add(statLine(armorIcon,
                        Component.translatable(Attributes.ARMOR_TOUGHNESS.value().getDescriptionId()),
                        toughness,
                        getItemStatDelta(delta, "toughness")));
            }
            if (knock != 0) {
                tooltip.add(statLine(armorIcon,
                        Component.translatable(Attributes.KNOCKBACK_RESISTANCE.value().getDescriptionId()),
                        knock,
                        getItemStatDelta(delta, "knockback_resistance")));
            }
            if (health != 0) {
                tooltip.add(statLine(armorIcon,
                        Component.translatable(Attributes.MAX_HEALTH.value().getDescriptionId()),
                        health,
                        getItemStatDelta(delta, "max_health")));
            }
            if (a.move != 0) {
                tooltip.add(statLine(ICON_BOOTS,
                        Component.translatable(Attributes.MOVEMENT_SPEED.value().getDescriptionId()),
                        a.move,
                        getItemStatDelta(delta, "movement_speed")));
            }
            if (a.soul != 0) {
                tooltip.add(statLine(ICON_BOOTS,
                        Component.translatable(Attributes.MOVEMENT_EFFICIENCY.value().getDescriptionId()),
                        a.soul,
                        getItemStatDelta(delta, "movement_efficiency")));
            }

            appendDurabilityLine(stack, tooltip, delta, true);
            return;
        }

        if (isWeapon) {
            class WeaponStats {
                double baseDmg = 0.0;
                double dmgMult = 0.0;
                double baseSpd = 0.0;
                double spdMult = 0.0;

                double damage() { return baseDmg * (1.0 + dmgMult); }
                double speed() { return (baseSpd + 4.0) * (1.0 + spdMult); }
            }

            WeaponStats ws = new WeaponStats();

            stack.forEachModifier(EquipmentSlot.MAINHAND, (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                if (attr.is(Attributes.ATTACK_DAMAGE)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) ws.dmgMult += mod.amount();
                    else ws.baseDmg += mod.amount();
                } else if (attr.is(Attributes.ATTACK_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) ws.spdMult += mod.amount();
                    else ws.baseSpd += mod.amount();
                }
            });

            double dmgVal = ws.damage();
            double spdVal = ws.speed();

            double baseRange = 3.0;
            RuneStats rs = RuneItem.getRolledStatsForTooltip(stack);
            float rangePct = rs != null ? rs.get(RuneStatType.ATTACK_RANGE) : 0.0F;
            double finalRange = baseRange * (1.0 + (rangePct / 100.0));

            if (isBowLike) {
                float drawPct = rs != null ? rs.get(RuneStatType.DRAW_SPEED) : 0.0F;
                float powerPct = rs != null ? rs.get(RuneStatType.POWER) : 0.0F;

                double finalDraw = 1.0 * (1.0 + drawPct / 100.0);
                double finalPower = 1.0 * (1.0 + powerPct / 100.0);

                tooltip.add(statLine(ICON_SWORD, Component.literal("Draw Speed"), finalDraw, getItemStatDelta(delta, "draw_speed")));
                tooltip.add(statLine(ICON_SWORD, Component.literal("Power"), finalPower, getItemStatDelta(delta, "power")));
                tooltip.add(statLine(ICON_SWORD, Component.literal("Range"), finalRange, getItemStatDelta(delta, "range")));
            } else {
                tooltip.add(statLine(ICON_SWORD,
                        Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()),
                        dmgVal,
                        getItemStatDelta(delta, "attack_damage")));
                tooltip.add(statLine(ICON_SWORD,
                        Component.translatable(Attributes.ATTACK_SPEED.value().getDescriptionId()),
                        spdVal,
                        getItemStatDelta(delta, "attack_speed")));
                tooltip.add(statLine(ICON_SWORD, Component.literal("Range"), finalRange, getItemStatDelta(delta, "range")));
            }
        }

        if (isToolForMining && !isBowLike) {
            double baseMining;
            if (item instanceof PickaxeItem) baseMining = stack.getDestroySpeed(Blocks.STONE.defaultBlockState());
            else if (item instanceof ShovelItem) baseMining = stack.getDestroySpeed(Blocks.DIRT.defaultBlockState());
            else if (item instanceof AxeItem) baseMining = stack.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
            else if (item instanceof HoeItem) baseMining = stack.getDestroySpeed(Blocks.WHEAT.defaultBlockState());
            else baseMining = stack.getDestroySpeed(Blocks.STONE.defaultBlockState());

            RuneStats stats = RuneItem.getRolledStatsForTooltip(stack);
            float miningPct = stats != null ? stats.get(RuneStatType.MINING_SPEED) : 0.0F;
            double finalMining = baseMining * (1.0 + (miningPct / 100.0));

            tooltip.add(statLine(ICON_PICKAXE,
                    Component.translatable(Attributes.BLOCK_BREAK_SPEED.value().getDescriptionId()),
                    finalMining,
                    getItemStatDelta(delta, "block_break_speed")));
        }

        appendDurabilityLine(stack, tooltip, delta, true);
    }

    private static void appendDurabilityLine(ItemStack stack, List<Component> tooltip, CompoundTag delta, boolean indented) {
        int max = stack.getMaxDamage();
        if (max <= 0) return;

        int curr = max - stack.getDamageValue();
        float pct = (float) curr / max;

        ChatFormatting color =
                pct > 0.50f ? ChatFormatting.GREEN :
                        pct > 0.25f ? ChatFormatting.YELLOW :
                                pct > 0.10f ? ChatFormatting.GOLD :
                                        ChatFormatting.RED;

        Integer dMax = getDurMaxDelta(delta);
        Integer dRem = getDurRemDelta(delta);

        String suffix = "";
        if (dMax != null) suffix = formatSignedInt(dMax);
        else if (dRem != null) suffix = formatSignedInt(dRem);

        String pad = indented ? "  " : "";
        tooltip.add(Component.literal(pad + "Durability: " + curr + "/" + max + suffix).withStyle(color));
    }

    private static String formatSignedInt(int v) {
        if (v == 0) return "";
        return (v > 0 ? " + " : " - ") + Math.abs(v);
    }

    private static String formatSignedDouble(double v) {
        if (Math.abs(v) < 1e-9) return "";
        double av = Math.abs(v);
        String fmt = Math.abs(av - Math.round(av)) < 0.001
                ? String.format(Locale.ROOT, "%.0f", av)
                : String.format(Locale.ROOT, "%.2f", av);
        return (v > 0 ? " + " : " - ") + fmt;
    }

    private static Component statLine(String icon, Component name, double val, Double delta) {
        String fmt = Math.abs(val - Math.round(val)) < 0.001
                ? String.format(Locale.ROOT, "%.0f", val)
                : String.format(Locale.ROOT, "%.2f", val);

        String suffix = delta != null ? formatSignedDouble(delta) : "";

        return Component.literal("  " + icon + " ")
                .append(name)
                .append(Component.literal(": " + fmt + suffix))
                .withStyle(ChatFormatting.WHITE);
    }

    private static void appendRuneStats(ItemStack stack, List<Component> tooltip, boolean isEnhancement, boolean isEtching) {
        RuneStats stats = RuneStats.get(stack);
        if (stats == null || stats.isEmpty()) return;

        CompoundTag delta = getPreviewDelta(stack);

        if (isEnhancement) {
            tooltip.add(Component.literal("Stats:").withStyle(ChatFormatting.GRAY));

            for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
                RuneStatType type = e.getKey();
                float v = e.getValue();
                if (v == 0) continue;

                int min = isEtching ? type.etchingMinPercent() : type.minPercent();
                int max = isEtching ? type.etchingMaxPercent() : type.maxPercent();
                String vStr = min == max ? ("+" + min + "%") : (min + "% - " + max + "%");

                tooltip.add(
                        Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.stat." + type.id()))
                                .append(Component.literal(": "))
                                .append(Component.literal(vStr).withStyle(ChatFormatting.AQUA))
                );
            }
            return;
        }

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Enhanced Stats:").withStyle(ChatFormatting.GRAY));

        for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
            RuneStatType type = e.getKey();
            float v = e.getValue();
            if (v == 0) continue;

            float abs = Math.abs(v);
            String num =
                    Math.abs(abs - Math.round(abs)) < 0.001
                            ? String.format(Locale.ROOT, "%.0f", abs)
                            : String.format(Locale.ROOT, "%.1f", abs);

            String vStr = "+" + num + "%";

            Float dv = getRuneStatDelta(delta, type.id());
            String suffix = "";
            if (dv != null) {
                float adv = Math.abs(dv);
                String dNum =
                        Math.abs(adv - Math.round(adv)) < 0.001
                                ? String.format(Locale.ROOT, "%.0f", adv)
                                : String.format(Locale.ROOT, "%.1f", adv);
                suffix = (dv > 0 ? " + " : " - ") + dNum + "%";
            }

            tooltip.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.stat." + type.id()))
                            .append(Component.literal(": "))
                            .append(Component.literal(vStr + suffix).withStyle(ChatFormatting.AQUA))
            );
        }
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

    private static void appendGearAttributes(ItemStack stack, List<Component> tooltip) {
        Map<GearAttribute, Integer> attrs = GearAttributes.getAll(stack);
        if (attrs.isEmpty()) return;

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Attributes:").withStyle(ChatFormatting.GRAY));

        GearAttribute[] order = new GearAttribute[]{
                GearAttribute.SEALED,
                GearAttribute.CURSED,
                GearAttribute.INSTABLE,
                GearAttribute.NEGATIVE
        };

        for (GearAttribute a : order) {
            int lvl = attrs.getOrDefault(a, 0);
            if (lvl <= 0) continue;

            ChatFormatting col = (a == GearAttribute.CURSED) ? a.color() : ChatFormatting.WHITE;

            MutableComponent line = Component.literal("  ")
                    .append(a.displayName().copy().withStyle(col));

            if (lvl > 1) {
                line.append(Component.literal(" " + toRoman(lvl)).withStyle(col));
            }

            tooltip.add(line);
        }
    }

    private static void appendEnhancementsSection(ItemStack stack, List<Component> tooltip) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        boolean has = !live.isEmpty() || !stored.isEmpty();
        if (!has) return;

        List<String> vanillaNames = new ArrayList<>();
        List<Component> rebuilt = new ArrayList<>();

        collectForEnhancements(live, vanillaNames, rebuilt);
        collectForEnhancements(stored, vanillaNames, rebuilt);

        if (!vanillaNames.isEmpty()) {
            tooltip.removeIf(line -> vanillaNames.contains(line.getString()));
        }

        if (rebuilt.isEmpty()) return;

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Enhancements:").withStyle(ChatFormatting.GRAY));
        tooltip.addAll(rebuilt);
    }

    private static void collectForEnhancements(ItemEnchantments ench, List<String> vanillaNames, List<Component> rebuilt) {
        ench.entrySet().forEach(e -> {
            Holder<Enchantment> holder = e.getKey();
            int lvl = e.getIntValue();

            Component vn = holder.value().getFullname(holder, lvl);
            vanillaNames.add(vn.getString());

            EnhancementRarity rarity = EnhancementRarities.get(holder);

            Component nameOnly = holder.value().description().copy()
                    .withStyle(style -> style.withColor(rarity.color()));

            rebuilt.add(Component.literal("  ").append(nameOnly));
        });
    }

    private static void appendRuneSlots(ItemStack stack, List<Component> tooltip) {
        int baseCap = RuneSlots.capacity(stack);
        int neg = GearAttributes.getLevel(stack, GearAttribute.NEGATIVE);
        int cap = Math.max(0, baseCap - neg);
        if (cap <= 0) return;

        int used = RuneSlots.used(stack);
        int u = Math.min(used, cap);
        int rem = Math.max(0, cap - used);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < u; i++) sb.append('⬤');
        for (int i = 0; i < rem; i++) sb.append('◯');

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Rune Slots:").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("  " + sb));
    }
}
