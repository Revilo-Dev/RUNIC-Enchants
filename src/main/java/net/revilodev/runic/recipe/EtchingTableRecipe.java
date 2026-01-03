package net.revilodev.runic.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.Optional;

public record EtchingTableRecipe(
        Ingredient base,
        Ingredient material,
        ItemStack result,
        Optional<RuneStatType> stat,
        Optional<ResourceLocation> effect
) implements Recipe<EtchingTableInput> {

    public static final Codec<RuneStatType> STAT_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                RuneStatType t = RuneStatType.byId(s);
                return t == null ? DataResult.error(() -> "Unknown stat: " + s) : DataResult.success(t);
            },
            RuneStatType::id
    );

    public static final MapCodec<EtchingTableRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC.fieldOf("base").forGetter(EtchingTableRecipe::base),
            Ingredient.CODEC.fieldOf("material").forGetter(EtchingTableRecipe::material),
            ItemStack.CODEC.fieldOf("result").forGetter(EtchingTableRecipe::result),
            STAT_CODEC.optionalFieldOf("stat").forGetter(EtchingTableRecipe::stat),
            ResourceLocation.CODEC.optionalFieldOf("effect").forGetter(EtchingTableRecipe::effect)
    ).apply(i, EtchingTableRecipe::new));

    @Override
    public boolean matches(EtchingTableInput input, net.minecraft.world.level.Level level) {
        if (input == null) return false;
        if (!base.test(input.base())) return false;
        return material.test(input.material());
    }

    @Override
    public ItemStack assemble(EtchingTableInput input, HolderLookup.Provider registries) {
        ItemStack out = result.copy();

        stat.ifPresent(t -> RuneStats.set(out, RuneStats.singleUnrolled(t)));

        effect.ifPresent(id -> {
            HolderLookup.RegistryLookup<net.minecraft.world.item.enchantment.Enchantment> ench =
                    registries.lookupOrThrow(Registries.ENCHANTMENT);

            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key =
                    ResourceKey.create(Registries.ENCHANTMENT, id);

            ench.get(key).ifPresent(holder -> {
                int lvl;
                if (out.getItem() instanceof EtchingItem) lvl = RuneItem.forcedEtchingEffectLevel(holder);
                else if (out.getItem() instanceof RuneItem) lvl = RuneItem.forcedEffectLevel(holder);
                else lvl = Math.min(holder.value().getMaxLevel(), 1);

                if (lvl > 0) out.enchant(holder, lvl);
            });
        });

        return out;
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
