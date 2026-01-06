package net.revilodev.runic.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public final class RunicEtchingRecipeProvider extends RecipeProvider {
    public RunicEtchingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        Ingredient statBase = Ingredient.of(itemOrThrow(RunicMod.MOD_ID, "blank_etching"));
        Ingredient statMaterial = Ingredient.of(Items.DIAMOND);

        Ingredient effectBase = Ingredient.of(itemOrThrow(RunicMod.MOD_ID, "blank_inscription"));
        Ingredient effectMaterial = Ingredient.of(Items.PRISMARINE_SHARD);

        ItemStack result = new ItemStack(itemOrThrow(RunicMod.MOD_ID, "etching"), 1);

        for (RuneStatType stat : RuneStatType.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "etching_table/etchings/stats/" + stat.id());
            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    statBase,
                    statMaterial,
                    result.copy(),
                    Optional.of(stat),
                    Optional.empty()
            );
            output.accept(id, recipe, null);
        }

        Set<ResourceLocation> allowed = EtchingItem.allowedEffectIds();
        ArrayList<ResourceLocation> effects = new ArrayList<>(allowed);
        effects.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation effectId : effects) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    RunicMod.MOD_ID,
                    "etching_table/etchings/effects/" + effectId.getNamespace() + "/" + effectId.getPath()
            );

            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    effectBase,
                    effectMaterial,
                    result.copy(),
                    Optional.empty(),
                    Optional.of(effectId)
            );
            output.accept(id, recipe, null);
        }
    }

    private static net.minecraft.world.item.Item itemOrThrow(String modid, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, path);
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id);
        if (item == net.minecraft.world.item.Items.AIR) {
            throw new IllegalStateException("Missing item: " + id);
        }
        return item;
    }
}
