package net.revilodev.runic.loot;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.revilodev.runic.RunicMod;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RunicMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> REMOVE_ENCHANTED_BOOKS =
            LOOT_MODIFIERS.register("remove_enchanted_books", () -> RemoveEnchantedBooksModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> RUNIC_STRUCTURE_LOOT_INJECTOR =
            LOOT_MODIFIERS.register("runic_structure_loot_injector", () -> RunicStructureLootInjector.CODEC);

    // NEW: register mob rune injector
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> MOB_RUNE_INJECTOR =
            LOOT_MODIFIERS.register("mob_rune_injector", () -> MobRuneInjector.CODEC);

    public static void register(IEventBus bus) {
        LOOT_MODIFIERS.register(bus);
        RunicMod.LOGGER.debug("[Runic] Registered all global loot modifier codecs.");
    }
}
