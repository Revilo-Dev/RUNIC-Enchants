package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;
import net.revilodev.runic.runes.RuneSlots;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipHandler {

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

        boolean isRune = stack.getItem() instanceof RuneItem;

        if (isRune) {
            stripRuneEnchantmentLines(stack, tooltip);
            appendRuneHeader(stack, tooltip);
        }

        appendItemStats(stack, tooltip);
        appendRuneStats(stack, tooltip, isRune);
        appendRuneSlots(stack, tooltip);
        recolorEnchants(stack, tooltip, isRune);
    }

    private static boolean isRunicModifier(AttributeModifier mod) {
        ResourceLocation id = mod.id();
        return id != null && RunicMod.MOD_ID.equals(id.getNamespace());
    }

    private static void stripRuneEnchantmentLines(ItemStack stack, List<Component> tooltip) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (live.isEmpty() && stored.isEmpty()) return;

        List<String> names = new ArrayList<>();

        live.entrySet().forEach(e ->
                names.add(e.getKey().value().getFullname(e.getKey(), e.getIntValue()).getString())
        );
        stored.entrySet().forEach(e ->
                names.add(e.getKey().value().getFullname(e.getKey(), e.getIntValue()).getString())
        );

        tooltip.removeIf(line -> names.contains(line.getString()));
    }

    private static void appendRuneHeader(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.literal("Apply in an Etching Table").withStyle(ChatFormatting.DARK_GRAY));

        RuneStats stats = RuneStats.get(stack);
        boolean hasStats = !stats.isEmpty();

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        boolean hasEffect = !stored.isEmpty() || !direct.isEmpty();

        if (hasEffect && !hasStats) {
            tooltip.add(Component.literal("Effect Rune").withStyle(ChatFormatting.AQUA));
        } else if (hasStats && !hasEffect) {
            tooltip.add(Component.literal("Stat Rune").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(Component.literal("Mixed Rune").withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        tooltip.add(Component.literal("Enhancement:").withStyle(ChatFormatting.GRAY));

        Holder<Enchantment> effect = RuneItem.getPrimaryEffectEnchantment(stack);
        if (effect != null) {
            tooltip.add(
                    Component.literal("  ")
                            .append(effect.value().getFullname(effect, 1))
                            .withStyle(ChatFormatting.AQUA)
            );
        }
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
            while (tooltip.size() > idx) {
                tooltip.remove(tooltip.size() - 1);
            }
        }
    }

    private static void appendItemStats(ItemStack stack, List<Component> tooltip) {
        Item item = stack.getItem();

        boolean isSword = item instanceof SwordItem;
        boolean isTrident = item instanceof TridentItem;
        boolean isBowLike = item instanceof BowItem || item instanceof CrossbowItem;
        boolean isFishing = item instanceof FishingRodItem;
        boolean isMace = item instanceof MaceItem;
        boolean isShield = item instanceof ShieldItem;

        boolean isWeapon =
                isSword ||
                        isMace ||
                        item instanceof AxeItem ||
                        isTrident ||
                        isBowLike;

        boolean isToolForMining =
                item instanceof PickaxeItem ||
                        item instanceof ShovelItem ||
                        item instanceof AxeItem ||
                        item instanceof HoeItem;

        boolean isArmor = item instanceof ArmorItem;

        boolean isDurabilityOnly =
                !isWeapon &&
                        !isToolForMining &&
                        !isArmor &&
                        stack.getMaxDamage() > 0;

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

                tooltip.add(Component.literal("Durability: " + curr + "/" + max).withStyle(color));
            }
            return;
        }

        RuneStats stats = RuneItem.getRolledStatsForTooltip(stack);

        if (!isWeapon && !isToolForMining && !isArmor)
            return;

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
                if (attr.is(Attributes.ARMOR)) {
                    a.armor += mod.amount();
                } else if (attr.is(Attributes.ARMOR_TOUGHNESS)) {
                    if (isRunicModifier(mod) && mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        a.toughMult += mod.amount();
                    } else {
                        a.toughBase += mod.amount();
                    }
                } else if (attr.is(Attributes.KNOCKBACK_RESISTANCE)) {
                    if (isRunicModifier(mod) && mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        a.knockMult += mod.amount();
                    } else {
                        a.knockBase += mod.amount();
                    }
                } else if (attr.is(Attributes.MOVEMENT_SPEED)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        a.move += mod.amount() * 100.0;
                    } else {
                        a.move += mod.amount();
                    }
                } else if (attr.is(Attributes.MOVEMENT_EFFICIENCY)) {
                    if (mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        a.soul += mod.amount() * 100.0;
                    } else {
                        a.soul += mod.amount();
                    }
                } else if (attr.is(Attributes.MAX_HEALTH)) {
                    if (isRunicModifier(mod) && mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                        a.healthMult += mod.amount();
                    } else {
                        a.healthBase += mod.amount();
                    }
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
                tooltip.add(statLine(
                        armorIcon,
                        Component.translatable(Attributes.ARMOR.value().getDescriptionId()),
                        a.armor
                ));
            }
            if (toughness != 0) {
                tooltip.add(statLine(
                        armorIcon,
                        Component.translatable(Attributes.ARMOR_TOUGHNESS.value().getDescriptionId()),
                        toughness
                ));
            }
            if (knock != 0) {
                tooltip.add(statLine(
                        armorIcon,
                        Component.translatable(Attributes.KNOCKBACK_RESISTANCE.value().getDescriptionId()),
                        knock
                ));
            }
            if (health != 0) {
                tooltip.add(statLine(
                        armorIcon,
                        Component.translatable(Attributes.MAX_HEALTH.value().getDescriptionId()),
                        health
                ));
            }
            if (a.move != 0) {
                tooltip.add(statLine(
                        ICON_BOOTS,
                        Component.translatable(Attributes.MOVEMENT_SPEED.value().getDescriptionId()),
                        a.move
                ));
            }
            if (a.soul != 0) {
                tooltip.add(statLine(
                        ICON_BOOTS,
                        Component.translatable(Attributes.MOVEMENT_EFFICIENCY.value().getDescriptionId()),
                        a.soul
                ));
            }

            int max = stack.getMaxDamage();
            if (max > 0) {
                int curr = max - stack.getDamageValue();
                float pct = (float) curr / max;

                ChatFormatting color =
                        pct > 0.50f ? ChatFormatting.GREEN :
                                pct > 0.25f ? ChatFormatting.YELLOW :
                                        pct > 0.10f ? ChatFormatting.GOLD :
                                                ChatFormatting.RED;

                tooltip.add(Component.literal("  Durability: " + curr + "/" + max).withStyle(color));
            }

            return;
        }

        class WeaponStats {
            double baseDmg = 0.0;
            double dmgMult = 0.0;
            double baseSpd = 0.0;
            double spdMult = 0.0;

            double damage() {
                return baseDmg * (1.0 + dmgMult);
            }

            double speed() {
                double real = (baseSpd + 4.0) * (1.0 + spdMult);
                return real;
            }
        }


        if (isWeapon) {
            WeaponStats ws = new WeaponStats();

            stack.forEachModifier(EquipmentSlot.MAINHAND,
                    (Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, AttributeModifier mod) -> {
                        if (attr.is(Attributes.ATTACK_DAMAGE)) {
                            if (isRunicModifier(mod) && mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                                ws.dmgMult += mod.amount();
                            } else {
                                ws.baseDmg += mod.amount();
                            }
                        } else if (attr.is(Attributes.ATTACK_SPEED)) {
                            if (isRunicModifier(mod) && mod.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                                ws.spdMult += mod.amount();
                            } else {
                                ws.baseSpd += mod.amount();
                            }
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
                double baseDraw = 1.0;
                double finalDraw = baseDraw * (1.0 + drawPct / 100.0);

                float powerPct = rs != null ? rs.get(RuneStatType.POWER) : 0.0F;
                double basePower = 1.0;
                double finalPower = basePower * (1.0 + powerPct / 100.0);

                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.literal("Draw Speed"),
                        finalDraw
                ));
                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.literal("Power"),
                        finalPower
                ));
                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.literal("Range"),
                        finalRange
                ));
            } else {
                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.translatable(Attributes.ATTACK_DAMAGE.value().getDescriptionId()),
                        dmgVal
                ));
                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.translatable(Attributes.ATTACK_SPEED.value().getDescriptionId()),
                        spdVal
                ));
                tooltip.add(statLine(
                        ICON_SWORD,
                        Component.literal("Range"),
                        finalRange
                ));
            }
        }

        if (isToolForMining && !isBowLike) {
            double baseMining;
            if (item instanceof PickaxeItem) baseMining = stack.getDestroySpeed(Blocks.STONE.defaultBlockState());
            else if (item instanceof ShovelItem) baseMining = stack.getDestroySpeed(Blocks.DIRT.defaultBlockState());
            else if (item instanceof AxeItem) baseMining = stack.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
            else if (item instanceof HoeItem) baseMining = stack.getDestroySpeed(Blocks.WHEAT.defaultBlockState());
            else baseMining = stack.getDestroySpeed(Blocks.STONE.defaultBlockState());

            float miningPct = stats != null ? stats.get(RuneStatType.MINING_SPEED) : 0.0F;
            double finalMining = baseMining * (1.0 + (miningPct / 100.0));

            tooltip.add(statLine(
                    ICON_PICKAXE,
                    Component.translatable(Attributes.BLOCK_BREAK_SPEED.value().getDescriptionId()),
                    finalMining
            ));
        }

        int max = stack.getMaxDamage();
        if (max > 0) {
            int curr = max - stack.getDamageValue();
            float pct = (float) curr / max;

            ChatFormatting color =
                    pct > 0.50f ? ChatFormatting.GREEN :
                            pct > 0.25f ? ChatFormatting.YELLOW :
                                    pct > 0.10f ? ChatFormatting.GOLD :
                                            ChatFormatting.RED;

            tooltip.add(Component.literal("  Durability: " + curr + "/" + max).withStyle(color));
        }
    }

    private static Component statLine(String icon, Component name, double val) {
        String fmt = Math.abs(val - Math.round(val)) < 0.001
                ? String.format(Locale.ROOT, "%.0f", val)
                : String.format(Locale.ROOT, "%.2f", val);

        return Component.literal("  " + icon + " ")
                .append(name)
                .append(Component.literal(": " + fmt))
                .withStyle(ChatFormatting.WHITE);
    }

    private static void appendRuneStats(ItemStack stack, List<Component> tooltip, boolean isRune) {
        RuneStats stats = RuneStats.get(stack);
        if (stats == null || stats.isEmpty()) return;

        if (isRune) {
            for (Map.Entry<RuneStatType, Float> e : stats.view().entrySet()) {
                RuneStatType type = e.getKey();
                float v = e.getValue();
                if (v == 0) continue;

                int min = type.minPercent();
                int max = type.maxPercent();
                String vStr = min == max ? "+" + min + "%" : min + "% - " + max + "%";

                tooltip.add(
                        Component.literal("  ")
                                .append(Component.translatable("tooltip.runic.stat." + type.id()))
                                .append(": ")
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

            float shown = Math.max(v, 0.0F);
            String vStr =
                    Math.abs(shown - Math.round(shown)) < 0.001
                            ? "+" + (int) shown + "%"
                            : "+" + String.format(Locale.ROOT, "%.1f%%", shown);

            tooltip.add(
                    Component.literal("  ")
                            .append(Component.translatable("tooltip.runic.stat." + type.id()))
                            .append(": ")
                            .append(Component.literal(vStr).withStyle(ChatFormatting.AQUA))
            );
        }
    }

    private static void appendRuneSlots(ItemStack stack, List<Component> tooltip) {
        int cap = RuneSlots.capacity(stack);
        if (cap <= 0) return;

        int used = RuneSlots.used(stack);
        int remain = Math.max(0, cap - used);

        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Rune Slots:").withStyle(ChatFormatting.GRAY));
        tooltip.add(
                RuneSlots.bar(stack).copy()
                        .withStyle(remain > 0 ? ChatFormatting.GRAY : ChatFormatting.RED)
        );
    }

    private static void recolorEnchants(ItemStack stack, List<Component> tooltip, boolean isRune) {
        ItemEnchantments live = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        boolean recolor = !live.isEmpty() || !stored.isEmpty();
        if (!recolor) return;

        List<Component> vanilla = new ArrayList<>();
        List<Component> rebuilt = new ArrayList<>();

        collect(live, vanilla, rebuilt);
        collect(stored, vanilla, rebuilt);

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

        if (first != -1)
            tooltip.addAll(first, rebuilt);
    }

    private static void collect(ItemEnchantments ench, List<Component> vanilla, List<Component> rebuilt) {
        ench.entrySet().forEach(e -> {
            Holder<Enchantment> holder = e.getKey();
            int lvl = e.getIntValue();

            Component vn = holder.value().getFullname(holder, lvl);
            vanilla.add(vn);

            EnhancementRarity rarity = EnhancementRarities.get(holder);
            rebuilt.add(vn.copy().withStyle(style -> style.withColor(rarity.color())));
        });
    }
}
