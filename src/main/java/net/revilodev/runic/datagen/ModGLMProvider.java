package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.loot.RemoveEnchantedBooksModifier;
import net.revilodev.runic.loot.RunicStructureLootInjector;

import java.util.concurrent.CompletableFuture;

public class ModGLMProvider extends GlobalLootModifierProvider {
    public ModGLMProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, RunicMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("remove_enchanted_books",
                new RemoveEnchantedBooksModifier(new LootItemCondition[]{}));

        add("runic_structure_loot_injector",
                new RunicStructureLootInjector(
                        new LootItemCondition[]{},
                        0.35f, 0.30f, 1, 3
                )
        );
    }
}
