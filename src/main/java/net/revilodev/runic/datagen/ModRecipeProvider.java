package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.EtchingItem;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {
    private static final EnumMap<RuneStatType, net.minecraft.world.level.ItemLike> STAT_MATERIALS = new EnumMap<>(RuneStatType.class);

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

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ItemStack etchingResult = new ItemStack(ModItems.ETCHING.get(), 1);

        Ingredient statBase = Ingredient.of(ModItems.BLANK_ETCHING.get());
        for (RuneStatType type : RuneStatType.values()) {
            net.minecraft.world.level.ItemLike mat = STAT_MATERIALS.get(type);
            if (mat == null) {
                throw new IllegalStateException("Missing STAT_MATERIALS for stat: " + type.id());
            }

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    RunicMod.MOD_ID,
                    "etching_table/stat/" + type.id()
            );

            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    statBase,
                    Ingredient.of(mat),
                    etchingResult.copy(),
                    Optional.of(type),
                    Optional.empty()
            );

            output.accept(id, recipe, null);
        }

        Ingredient effectBase = Ingredient.of(ModItems.BLANK_INSCRIPTION.get());
        Ingredient effectMaterial = Ingredient.of(Items.ENCHANTED_BOOK);

        Set<ResourceLocation> allowed = EtchingItem.allowedEffectIds();
        ArrayList<ResourceLocation> effects = new ArrayList<>(allowed);
        effects.sort(Comparator.comparing(ResourceLocation::toString));

        for (ResourceLocation enchId : effects) {
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

            output.accept(id, recipe, null);
        }
    }
}
