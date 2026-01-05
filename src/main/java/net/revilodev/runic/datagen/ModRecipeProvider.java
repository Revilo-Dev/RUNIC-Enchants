package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
        this.lookupProvider = lookupProvider;
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        Ingredient statBase = Ingredient.of(ModItems.BLANK_ETCHING.get());
        Ingredient statMaterial = Ingredient.of(Items.DIAMOND);
        ItemStack etchingResult = new ItemStack(ModItems.ETCHING.get(), 1);

        for (RuneStatType type : RuneStatType.values()) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    RunicMod.MOD_ID,
                    "etching_table/stat/" + type.id()
            );

            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    statBase,
                    statMaterial,
                    etchingResult.copy(),
                    Optional.of(type),
                    Optional.empty()
            );

            output.accept(recipeKey(id), recipe, null);
        }

        HolderLookup.Provider registries = lookupProvider.join();
        Ingredient effectBase = Ingredient.of(ModItems.BLANK_INSCRIPTION.get());
        Ingredient effectMaterial = Ingredient.of(Items.PRISMARINE_SHARD);

        Set<ResourceLocation> allowed = EtchingItem.allowedEffectIds();
        var enchLookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

        for (ResourceLocation enchId : allowed) {
            ResourceKey<Enchantment> enchKey = ResourceKey.create(Registries.ENCHANTMENT, enchId);
            if (enchLookup.get(enchKey).isEmpty()) {
                continue;
            }

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    RunicMod.MOD_ID,
                    "etching_table/effect/" + enchId.getNamespace() + "/" + enchId.getPath()
            );

            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    effectBase,
                    effectMaterial,
                    etchingResult.copy(),
                    Optional.empty(),
                    Optional.of(enchId)
            );

            output.accept(recipeKey(id), recipe, null);
        }
    }

    private static ResourceKey<Recipe<?>> recipeKey(ResourceLocation id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }
}
