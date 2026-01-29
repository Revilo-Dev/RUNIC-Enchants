package net.revilodev.runic.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;

import java.util.List;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RunicMod.MOD_ID);

    public static final DeferredHolder<Item, Item> BLANK_INSCRIPTION =
            ITEMS.register("blank_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.runic.use_etching_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, EtchingItem> BLANK_ETCHING =
            ITEMS.register("blank_etching", () -> new EtchingItem(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.runic.use_etching_table").withStyle(ChatFormatting.DARK_GRAY));
                }
            });

    public static final DeferredHolder<Item, RuneItem> ENHANCED_RUNE =
            ITEMS.register("enhanced_rune", () -> new RuneItem(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, EtchingItem> ETCHING =
            ITEMS.register("etching", () -> new EtchingItem(new Item.Properties().stacksTo(64)));

    public static final DeferredHolder<Item, Item> REPAIR_INSCRIPTION =
            ITEMS.register("repair_rune", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.repair_rune").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> EXPANSION_INSCRIPTION =
            ITEMS.register("expansion_rune", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.expansion_rune").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> NULLIFICATION_INSCRIPTION =
            ITEMS.register("nullification_rune", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.nullification_rune").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> UPGRADE_INSCRIPTION =
            ITEMS.register("upgrade_rune", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.upgrade_rune").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> REROLL_INSCRIPTION =
            ITEMS.register("reroll_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.reroll_inscription").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> CURSED_INSCRIPTION =
            ITEMS.register("cursed_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.cursed_inscription").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> WILD_INSCRIPTION =
            ITEMS.register("wild_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.wild_inscription").withStyle(ChatFormatting.GRAY));
                }
            });

    public static final DeferredHolder<Item, Item> EXTRACTION_INSCRIPTION =
            ITEMS.register("extraction_inscription", () -> new Item(new Item.Properties().stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.literal("Apply in an Artisan's Workbench").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("tooltip.runic.extraction_inscription").withStyle(ChatFormatting.GRAY));
                }
            });

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
