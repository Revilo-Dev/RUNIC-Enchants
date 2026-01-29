package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DataGenerators {
    private DataGenerators() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        if (event.includeServer()) {
            generator.addProvider(true, new LootTableProvider(
                    output,
                    Collections.emptySet(),
                    List.of(new LootTableProvider.SubProviderEntry(
                            ModBlockLootTableProvider::new,
                            net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.BLOCK
                    )),
                    lookupProvider
            ));

            generator.addProvider(true, new ModDatapackEntries(output, lookupProvider));
            generator.addProvider(true, new ModGLMProvider(output, lookupProvider));
            generator.addProvider(true, new ModRecipeProvider(output, lookupProvider));

            BlockTagsProvider blockTags = new ModBlockTagProvider(output, lookupProvider, fileHelper);
            generator.addProvider(true, blockTags);
        }

        if (event.includeClient()) {
            generator.addProvider(true, new ModItemModelProvider(output, fileHelper));

            generator.addProvider(true, new RunicLangProviders.EN_US(output));
            generator.addProvider(true, new RunicLangProviders.EN_GB(output));
            generator.addProvider(true, new RunicLangProviders.EN_AU(output));
            generator.addProvider(true, new RunicLangProviders.EN_CA(output));
            generator.addProvider(true, new RunicLangProviders.EN_NZ(output));
        }
    }
}
