package net.revilodev.runic.item;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.block.ModBlocks;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStatType;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RunicMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RUNIC_ITEMS_TAB =
            CREATIVE_MODE_TABS.register("runic_items_tab", () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ETCHING_TABLE.get()))
                    .title(Component.translatable("creativetab.runicmod.runic_items"))
                    .displayItems((params, output) -> {
                        output.accept(ModBlocks.ETCHING_TABLE.get());
                        output.accept(ModItems.EXPANSION_RUNE.get());
                        output.accept(ModItems.REPAIR_RUNE.get());
                        output.accept(ModItems.NULLIFICATION_RUNE.get());
                        output.accept(ModItems.UPGRADE_RUNE.get());

                        RandomSource random = RandomSource.create();

                        for (RuneStatType type : RuneStatType.values()) {
                            ItemStack statRune = RuneItem.createStatRune(random, type);
                            if (!statRune.isEmpty()) output.accept(statRune);

                            ItemStack statEtching = EtchingItem.createStatEtching(random, type);
                            if (!statEtching.isEmpty()) output.accept(statEtching);
                        }

                        params.holders()
                                .lookup(Registries.ENCHANTMENT)
                                .ifPresent((HolderLookup.RegistryLookup<Enchantment> enchants) -> {
                                    for (ResourceLocation id : RuneItem.allowedEffectIds()) {
                                        ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
                                        enchants.get(key).ifPresent((Holder<Enchantment> holder) -> {
                                            ItemStack effectRune = RuneItem.createEffectRune(holder);
                                            if (!effectRune.isEmpty()) output.accept(effectRune);

                                            ItemStack effectEtching = EtchingItem.createEffectEtching(holder);
                                            if (!effectEtching.isEmpty()) output.accept(effectEtching);
                                        });
                                    }
                                });
                    })
                    .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
