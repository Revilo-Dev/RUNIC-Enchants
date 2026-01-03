package net.revilodev.runic.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.revilodev.runic.stat.RuneStatType;

public final class EtchingTableRecipeSerializer implements RecipeSerializer<EtchingTableRecipe> {
    public static final EtchingTableRecipeSerializer INSTANCE = new EtchingTableRecipeSerializer();

    public static final MapCodec<EtchingTableRecipe> CODEC = EtchingTableRecipe.CODEC;

    public static final StreamCodec<RegistryFriendlyByteBuf, EtchingTableRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, EtchingTableRecipe::base,
            Ingredient.CONTENTS_STREAM_CODEC, EtchingTableRecipe::material,
            ItemStack.STREAM_CODEC, EtchingTableRecipe::result,
            ByteBufCodecs.optional(RuneStatType.STREAM_CODEC), EtchingTableRecipe::stat,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), EtchingTableRecipe::effect,
            EtchingTableRecipe::new
    );

    @Override
    public MapCodec<EtchingTableRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EtchingTableRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
