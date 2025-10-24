package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.loot.RemoveEnchantedBooksModifier;
import net.revilodev.runic.loot.RuneInjector;
import net.revilodev.runic.loot.MobRuneInjector;
import net.revilodev.runic.loot.ArmourRuneInjector;

import java.util.concurrent.CompletableFuture;

public class ModGLMProvider extends GlobalLootModifierProvider {
    public ModGLMProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, RunicMod.MOD_ID);
    }

    @Override
    protected void start() {
        add("remove_enchanted_books", new RemoveEnchantedBooksModifier(new LootItemCondition[]{}));
        add("rune_injector", new RuneInjector(new LootItemCondition[]{}, 0.15f, 1, 3));
        add("mob_rune_injector", new MobRuneInjector(new LootItemCondition[]{}, 0.02f, 1, 2));
        add("armour_rune_injector", new ArmourRuneInjector(new LootItemCondition[]{}, 0.25f, 1, 3));

    }
}
