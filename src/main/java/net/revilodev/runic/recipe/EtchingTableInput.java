package net.revilodev.runic.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record EtchingTableInput(ItemStack base, ItemStack material) implements RecipeInput {
    @Override
    public int size() {
        return 2;
    }

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> base;
            case 1 -> material;
            default -> ItemStack.EMPTY;
        };
    }
}
