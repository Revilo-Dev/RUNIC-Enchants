package net.revilodev.runic.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RunicMod.MOD_ID);

    public static final DeferredHolder<Item, RuneItem> ENHANCED_RUNE =
            ITEMS.register("enhanced_rune", () -> new RuneItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, EtchingItem> ETCHING =
            ITEMS.register("etching", () -> new EtchingItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> REPAIR_RUNE =
            ITEMS.register("repair_rune", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> EXPANSION_RUNE =
            ITEMS.register("expansion_rune", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> NULLIFICATION_RUNE =
            ITEMS.register("nullification_rune", () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> UPGRADE_RUNE =
            ITEMS.register("upgrade_rune", () -> new Item(new Item.Properties().stacksTo(16)));

    private ModItems() {}

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
