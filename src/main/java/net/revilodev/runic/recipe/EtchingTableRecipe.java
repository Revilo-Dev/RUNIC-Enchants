package net.revilodev.runic.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.revilodev.runic.stat.RuneStatType;

import java.util.Optional;

public final class EtchingTableRecipe implements Recipe<EtchingTableInput> {
    public static final MapCodec<EtchingTableRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("base").forGetter(EtchingTableRecipe::base),
            Ingredient.CODEC.fieldOf("material").forGetter(EtchingTableRecipe::material),
            ItemStack.CODEC.fieldOf("result").forGetter(EtchingTableRecipe::result),
            RuneStatType.CODEC.optionalFieldOf("stat").forGetter(EtchingTableRecipe::stat),
            ResourceLocation.CODEC.optionalFieldOf("effect").forGetter(EtchingTableRecipe::effect)
    ).apply(inst, EtchingTableRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EtchingTableRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, EtchingTableRecipe::base,
            Ingredient.CONTENTS_STREAM_CODEC, EtchingTableRecipe::material,
            ItemStack.STREAM_CODEC, EtchingTableRecipe::result,
            net.minecraft.network.codec.ByteBufCodecs.optional(RuneStatType.STREAM_CODEC), EtchingTableRecipe::stat,
            net.minecraft.network.codec.ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), EtchingTableRecipe::effect,
            EtchingTableRecipe::new
    );

    private final Ingredient base;
    private final Ingredient material;
    private final ItemStack result;
    private final Optional<RuneStatType> stat;
    private final Optional<ResourceLocation> effect;

    public EtchingTableRecipe(Ingredient base, Ingredient material, ItemStack result, Optional<RuneStatType> stat, Optional<ResourceLocation> effect) {
        this.base = base;
        this.material = material;
        this.result = result;
        this.stat = stat;
        this.effect = effect;
    }

    public Ingredient base() {
        return base;
    }

    public Ingredient material() {
        return material;
    }

    public ItemStack result() {
        return result;
    }

    public Optional<RuneStatType> stat() {
        return stat;
    }

    public Optional<ResourceLocation> effect() {
        return effect;
    }

    @Override
    public boolean matches(EtchingTableInput input, net.minecraft.world.level.Level level) {
        return base.test(input.base()) && material.test(input.material());
    }

    @Override
    public ItemStack assemble(EtchingTableInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.ETCHING_TABLE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ETCHING_TABLE.get();
    }
}
