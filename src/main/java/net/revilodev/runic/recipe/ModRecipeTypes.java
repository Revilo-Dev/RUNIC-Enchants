package net.revilodev.runic.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;

public final class ModRecipeTypes {
    private ModRecipeTypes() {}

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RunicMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EtchingTableRecipe>> ETCHING_TABLE =
            RECIPE_TYPES.register("etching_table", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return RunicMod.MOD_ID + ":etching_table";
                }
            });

    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
    }
}
