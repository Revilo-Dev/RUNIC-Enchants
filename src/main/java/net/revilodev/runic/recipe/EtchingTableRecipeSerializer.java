package net.revilodev.runic.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class EtchingTableRecipeSerializer implements RecipeSerializer<EtchingTableRecipe> {
    public static final EtchingTableRecipeSerializer INSTANCE = new EtchingTableRecipeSerializer();

    public static final MapCodec<EtchingTableRecipe> CODEC = EtchingTableRecipe.CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, EtchingTableRecipe> STREAM_CODEC = EtchingTableRecipe.STREAM_CODEC;

    @Override
    public MapCodec<EtchingTableRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EtchingTableRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
