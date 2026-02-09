package net.revilodev.runic.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.Optional;

public final class EtchingTableRecipe implements Recipe<EtchingTableInput> {
    private static final ResourceKey<Registry<Enchantment>> ENCHANTMENT_REGISTRY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("minecraft", "enchantment"));

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
        if (!base.test(input.base())) return false;

        if (!material.test(input.material())) return false;

        if (effect.isPresent() && stackHasAnyEnchantment(input.material())) {
            return stackHasEnchantmentId(input.material(), effect.get());
        }

        return true;
    }

    @Override
    public ItemStack assemble(EtchingTableInput input, HolderLookup.Provider registries) {
        ItemStack out = result.copy();

        if (stat.isPresent()) {
            RuneStats.set(out, RuneStats.singleUnrolled(stat.get()));
        }

        if (effect.isPresent()) {
            Holder<Enchantment> ench = enchantmentHolderOrNull(registries, effect.get());
            if (ench != null && EtchingItem.isEffectEnchantment(ench)) {
                out.enchant(ench, RuneItem.forcedEtchingEffectLevel(ench));
            }
        }

        return out;
    }

    private static boolean stackHasAnyEnchantment(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) return true;

        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        return !direct.isEmpty();
    }

    private static boolean stackHasEnchantmentId(ItemStack stack, ResourceLocation id) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            for (Holder<Enchantment> h : stored.keySet()) {
                if (holderIdEquals(h, id)) return true;
            }
        }

        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!direct.isEmpty()) {
            for (Holder<Enchantment> h : direct.keySet()) {
                if (holderIdEquals(h, id)) return true;
            }
        }

        return false;
    }

    private static boolean holderIdEquals(Holder<Enchantment> h, ResourceLocation id) {
        return h.unwrapKey().map(k -> k.location().equals(id)).orElse(false);
    }

    private static Holder<Enchantment> enchantmentHolderOrNull(HolderLookup.Provider registries, ResourceLocation id) {
        ResourceKey<Enchantment> key = ResourceKey.create(ENCHANTMENT_REGISTRY, id);
        return registries.lookup(ENCHANTMENT_REGISTRY).flatMap(l -> l.get(key)).orElse(null);
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
