package net.revilodev.runic.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record EtchingTableInput(ItemStack base, ItemStack material) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? base : index == 1 ? material : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return base.isEmpty() && material.isEmpty();
    }
}
