package net.revilodev.runic.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class RunicEtchingRecipeProvider extends RecipeProvider {
    private static final ResourceLocation BLANK_ETCHING = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "blank_etching");
    private static final ResourceLocation ETCHING = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "etching");

    private static final EnumMap<RuneStatType, Item> STAT_MATERIALS = new EnumMap<>(RuneStatType.class);

    static {
        STAT_MATERIALS.put(RuneStatType.ATTACK_SPEED, Items.SUGAR);
        STAT_MATERIALS.put(RuneStatType.ATTACK_DAMAGE, Items.DIAMOND);
        STAT_MATERIALS.put(RuneStatType.ATTACK_RANGE, Items.SPYGLASS);
        STAT_MATERIALS.put(RuneStatType.MOVEMENT_SPEED, Items.RABBIT_FOOT);
        STAT_MATERIALS.put(RuneStatType.SWEEPING_RANGE, Items.FEATHER);
        STAT_MATERIALS.put(RuneStatType.DURABILITY, Items.OBSIDIAN);
        STAT_MATERIALS.put(RuneStatType.RESISTANCE, Items.IRON_INGOT);
        STAT_MATERIALS.put(RuneStatType.FIRE_RESISTANCE, Items.MAGMA_CREAM);
        STAT_MATERIALS.put(RuneStatType.BLAST_RESISTANCE, Items.GUNPOWDER);
        STAT_MATERIALS.put(RuneStatType.PROJECTILE_RESISTANCE, Items.SHIELD);
        STAT_MATERIALS.put(RuneStatType.KNOCKBACK_RESISTANCE, Items.SLIME_BALL);
        STAT_MATERIALS.put(RuneStatType.MINING_SPEED, Items.REDSTONE);
        STAT_MATERIALS.put(RuneStatType.UNDEAD_DAMAGE, Items.ROTTEN_FLESH);
        STAT_MATERIALS.put(RuneStatType.NETHER_DAMAGE, Items.BLAZE_ROD);
        STAT_MATERIALS.put(RuneStatType.HEALTH, Items.GOLDEN_APPLE);
        STAT_MATERIALS.put(RuneStatType.STUN_CHANCE, Items.AMETHYST_SHARD);
        STAT_MATERIALS.put(RuneStatType.FLAME_CHANCE, Items.BLAZE_POWDER);
        STAT_MATERIALS.put(RuneStatType.BLEEDING_CHANCE, Items.SWEET_BERRIES);
        STAT_MATERIALS.put(RuneStatType.SHOCKING_CHANCE, Items.COPPER_INGOT);
        STAT_MATERIALS.put(RuneStatType.POISON_CHANCE, Items.SPIDER_EYE);
        STAT_MATERIALS.put(RuneStatType.WITHERING_CHANCE, Items.WITHER_ROSE);
        STAT_MATERIALS.put(RuneStatType.WEAKENING_CHANCE, Items.FERMENTED_SPIDER_EYE);
        STAT_MATERIALS.put(RuneStatType.HEALING_EFFICIENCY, Items.GHAST_TEAR);
        STAT_MATERIALS.put(RuneStatType.DRAW_SPEED, Items.STRING);
        STAT_MATERIALS.put(RuneStatType.TOUGHNESS, Items.NETHERITE_SCRAP);
        STAT_MATERIALS.put(RuneStatType.FREEZING_CHANCE, Items.PACKED_ICE);
        STAT_MATERIALS.put(RuneStatType.LEECHING_CHANCE, Items.HONEY_BOTTLE);
        STAT_MATERIALS.put(RuneStatType.BONUS_CHANCE, Items.LAPIS_LAZULI);
        STAT_MATERIALS.put(RuneStatType.JUMP_HEIGHT, Items.SLIME_BLOCK);
        STAT_MATERIALS.put(RuneStatType.POWER, Items.NETHER_STAR);
    }

    public RunicEtchingRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        Ingredient blankEtching = Ingredient.of(itemOrThrow(BLANK_ETCHING));
        Item etchingItem = itemOrThrow(ETCHING);

        for (RuneStatType stat : RuneStatType.values()) {
            Item materialItem = STAT_MATERIALS.get(stat);
            if (materialItem == null) {
                throw new IllegalStateException("Missing STAT_MATERIALS for stat: " + stat.id());
            }

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "etching_table/etchings/stats/" + stat.id());
            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    blankEtching,
                    Ingredient.of(materialItem),
                    new ItemStack(etchingItem),
                    Optional.of(stat),
                    Optional.empty()
            );
            output.accept(id, recipe, null);
        }

        Set<ResourceLocation> allowed = EtchingItem.allowedEffectIds();
        ArrayList<ResourceLocation> effects = new ArrayList<>(allowed);
        effects.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation effectId : effects) {
            Holder<Enchantment> enchantment = enchantmentHolderOrThrow(effectId);
            int max = Math.max(1, enchantment.value().getMaxLevel());

            for (int level = 1; level <= max; level++) {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                        RunicMod.MOD_ID,
                        "etching_table/etchings/effects/" + effectId.getNamespace() + "/" + effectId.getPath() + "/lvl_" + level
                );

                EtchingTableRecipe recipe = new EtchingTableRecipe(
                        blankEtching,
                        enchantedBookWith(enchantment, level),
                        new ItemStack(etchingItem),
                        Optional.empty(),
                        Optional.of(effectId)
                );
                output.accept(id, recipe, null);
            }
        }
    }

    private static Item itemOrThrow(ResourceLocation id) {
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            throw new IllegalStateException("Missing item: " + id);
        }
        return BuiltInRegistries.ITEM.get(id);
    }

    private static Holder<Enchantment> enchantmentHolderOrThrow(ResourceLocation id) {
        ResourceKey<Enchantment> key = ResourceKey.create(BuiltInRegistries.ENCHANTMENT.key(), id);
        return BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(key);
    }

    private static Ingredient enchantedBookWith(Holder<Enchantment> enchantment, int level) {
        ItemEnchantments.Mutable m = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        m.set(enchantment, level);
        ItemEnchantments stored = m.toImmutable();
        return DataComponentIngredient.of(false, DataComponents.STORED_ENCHANTMENTS, stored, Items.ENCHANTED_BOOK);
    }
}
