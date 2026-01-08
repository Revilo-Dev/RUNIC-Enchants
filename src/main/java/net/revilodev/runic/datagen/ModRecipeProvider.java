package net.revilodev.runic.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.crafting.Ingredient;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.recipe.EtchingTableRecipe;
import net.revilodev.runic.stat.RuneStatType;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ModRecipeProvider extends RecipeProvider {
    private static final EnumMap<RuneStatType, ItemLike> STAT_MATERIALS = new EnumMap<>(RuneStatType.class);

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

    private static final List<ResourceLocation> EFFECTS = List.of(
            ResourceLocation.fromNamespaceAndPath("aether", "renewal"),

            ResourceLocation.fromNamespaceAndPath("combat_roll", "acrobat"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "longfooted"),
            ResourceLocation.fromNamespaceAndPath("combat_roll", "multi_roll"),

            ResourceLocation.fromNamespaceAndPath("create", "capacity"),
            ResourceLocation.fromNamespaceAndPath("create", "potato_recovery"),

            ResourceLocation.fromNamespaceAndPath("deeperdarker", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "discharge"),
            ResourceLocation.fromNamespaceAndPath("deeperdarker", "sculk_smite"),

            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "discharge"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "ensnaring"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "lolths_curse"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "purification"),
            ResourceLocation.fromNamespaceAndPath("dungeons_arise", "voltaic_shot"),

            ResourceLocation.fromNamespaceAndPath("expanded_combat", "blocking"),
            ResourceLocation.fromNamespaceAndPath("expanded_combat", "ground_slam"),

            ResourceLocation.fromNamespaceAndPath("farmersdelight", "backstabbing"),

            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "mystical_enlightenment"),
            ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("simplyswords", "catalysis"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "fire_react"),
            ResourceLocation.fromNamespaceAndPath("simplyswords", "soul_siphoner"),

            ResourceLocation.fromNamespaceAndPath("supplementaries", "stasis"),

            ResourceLocation.fromNamespaceAndPath("twilightforest", "chill_aura"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "destruction"),
            ResourceLocation.fromNamespaceAndPath("twilightforest", "fire_react"),

            ResourceLocation.withDefaultNamespace("aqua_affinity"),
            ResourceLocation.withDefaultNamespace("depth_strider"),
            ResourceLocation.withDefaultNamespace("feather_falling"),

            ResourceLocation.withDefaultNamespace("binding_curse"),
            ResourceLocation.withDefaultNamespace("breach"),
            ResourceLocation.withDefaultNamespace("channeling"),
            ResourceLocation.withDefaultNamespace("density"),
            ResourceLocation.withDefaultNamespace("flame"),
            ResourceLocation.withDefaultNamespace("impaling"),
            ResourceLocation.withDefaultNamespace("infinity"),
            ResourceLocation.withDefaultNamespace("looting"),
            ResourceLocation.withDefaultNamespace("luck_of_the_sea"),
            ResourceLocation.withDefaultNamespace("multishot"),
            ResourceLocation.withDefaultNamespace("respiration"),
            ResourceLocation.withDefaultNamespace("riptide"),
            ResourceLocation.withDefaultNamespace("fortune"),
            ResourceLocation.withDefaultNamespace("frost_walker"),
            ResourceLocation.withDefaultNamespace("loyalty"),
            ResourceLocation.withDefaultNamespace("lure"),
            ResourceLocation.withDefaultNamespace("mending"),
            ResourceLocation.withDefaultNamespace("piercing"),
            ResourceLocation.withDefaultNamespace("punch"),
            ResourceLocation.withDefaultNamespace("silk_touch"),
            ResourceLocation.withDefaultNamespace("soul_speed"),
            ResourceLocation.withDefaultNamespace("swift_sneak"),
            ResourceLocation.withDefaultNamespace("thorns"),
            ResourceLocation.withDefaultNamespace("vanishing_curse"),
            ResourceLocation.withDefaultNamespace("wind_burst")
    );

    private static final Map<ResourceLocation, ItemLike> EFFECT_MATERIALS = new LinkedHashMap<>();

    static {
        put("aether", "renewal", Items.GHAST_TEAR);

        put("combat_roll", "acrobat", Items.RABBIT_FOOT);
        put("combat_roll", "longfooted", Items.LEATHER_BOOTS);
        put("combat_roll", "multi_roll", Items.FEATHER);

        put("create", "capacity", Items.BARREL);
        put("create", "potato_recovery", Items.POTATO);

        put("deeperdarker", "catalysis", Items.SCULK_CATALYST);
        put("deeperdarker", "discharge", Items.ECHO_SHARD);
        put("deeperdarker", "sculk_smite", Items.SCULK_SHRIEKER);

        put("dungeons_arise", "discharge", Items.LIGHTNING_ROD);
        put("dungeons_arise", "ensnaring", Items.COBWEB);
        put("dungeons_arise", "lolths_curse", Items.SPIDER_EYE);
        put("dungeons_arise", "purification", Items.MILK_BUCKET);
        put("dungeons_arise", "voltaic_shot", Items.COPPER_INGOT);

        put("expanded_combat", "blocking", Items.SHIELD);
        put("expanded_combat", "ground_slam", Items.ANVIL);

        put("farmersdelight", "backstabbing", Items.IRON_SWORD);

        put("mysticalagriculture", "mystical_enlightenment", Items.EXPERIENCE_BOTTLE);
        put("mysticalagriculture", "soul_siphoner", Items.SOUL_SAND);

        put("simplyswords", "catalysis", Items.BLAZE_POWDER);
        put("simplyswords", "fire_react", Items.MAGMA_CREAM);
        put("simplyswords", "soul_siphoner", Items.SOUL_LANTERN);

        put("supplementaries", "stasis", Items.CLOCK);

        put("twilightforest", "chill_aura", Items.PACKED_ICE);
        put("twilightforest", "destruction", Items.TNT);
        put("twilightforest", "fire_react", Items.FIRE_CHARGE);

        putVanilla("aqua_affinity", Items.PRISMARINE_CRYSTALS);
        putVanilla("depth_strider", Items.PRISMARINE_SHARD);
        putVanilla("feather_falling", Items.FEATHER);

        putVanilla("binding_curse", Items.CHAIN);
        putVanilla("breach", Items.IRON_PICKAXE);
        putVanilla("channeling", Items.TRIDENT);
        putVanilla("density", Items.IRON_BLOCK);
        putVanilla("flame", Items.FLINT_AND_STEEL);
        putVanilla("impaling", Items.PRISMARINE_SHARD);
        putVanilla("infinity", Items.ARROW);
        putVanilla("looting", Items.GOLD_INGOT);
        putVanilla("luck_of_the_sea", Items.NAUTILUS_SHELL);
        putVanilla("multishot", Items.CROSSBOW);
        putVanilla("respiration", Items.PUFFERFISH);
        putVanilla("riptide", Items.HEART_OF_THE_SEA);
        putVanilla("fortune", Items.EMERALD);
        putVanilla("frost_walker", Items.PACKED_ICE);
        putVanilla("loyalty", Items.LEAD);
        putVanilla("lure", Items.FISHING_ROD);
        putVanilla("mending", Items.EXPERIENCE_BOTTLE);
        putVanilla("piercing", Items.SPECTRAL_ARROW);
        putVanilla("punch", Items.PISTON);
        putVanilla("silk_touch", Items.SHEARS);
        putVanilla("soul_speed", Items.SOUL_SOIL);
        putVanilla("swift_sneak", Items.ECHO_SHARD);
        putVanilla("thorns", Items.CACTUS);
        putVanilla("vanishing_curse", Items.ENDER_PEARL);
        putVanilla("wind_burst", Items.BREEZE_ROD);
    }

    private static void put(String ns, String path, ItemLike item) {
        EFFECT_MATERIALS.put(ResourceLocation.fromNamespaceAndPath(ns, path), item);
    }

    private static void putVanilla(String path, ItemLike item) {
        EFFECT_MATERIALS.put(ResourceLocation.withDefaultNamespace(path), item);
    }

    private static ItemLike effectMaterialOrThrow(ResourceLocation effectId) {
        ItemLike item = EFFECT_MATERIALS.get(effectId);
        if (item == null) throw new IllegalStateException("Missing EFFECT_MATERIALS entry for: " + effectId);
        return item;
    }

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ItemStack etchingResult = new ItemStack(ModItems.ETCHING.get(), 1);

        Ingredient statBase = Ingredient.of(ModItems.BLANK_ETCHING.get());
        for (RuneStatType type : RuneStatType.values()) {
            ItemLike mat = STAT_MATERIALS.get(type);
            if (mat == null) throw new IllegalStateException("Missing STAT_MATERIALS for stat: " + type.id());

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

        for (ResourceLocation effectId : EFFECTS) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    RunicMod.MOD_ID,
                    "etching_table/effect/" + effectId.getNamespace() + "/" + effectId.getPath()
            );

            EtchingTableRecipe recipe = new EtchingTableRecipe(
                    effectBase,
                    Ingredient.of(effectMaterialOrThrow(effectId)),
                    etchingResult.copy(),
                    Optional.empty(),
                    Optional.of(effectId)
            );

            output.accept(id, recipe, null);
        }

        buildUtilityInscriptions(output);
    }

    private void buildUtilityInscriptions(RecipeOutput output) {
        Ingredient base = Ingredient.of(ModItems.BLANK_INSCRIPTION.get());

        addUtility(output, "repair_rune", base, Items.IRON_BLOCK, ModItems.REPAIR_INSCRIPTION.get());
        addUtility(output, "expansion_rune", base, Items.DIAMOND_BLOCK, ModItems.EXPANSION_INSCRIPTION.get());
        addUtility(output, "nullification_rune", base, Items.AMETHYST_BLOCK, ModItems.NULLIFICATION_INSCRIPTION.get());
        addUtility(output, "upgrade_rune", base, Items.LAPIS_BLOCK, ModItems.UPGRADE_INSCRIPTION.get());
        addUtility(output, "reroll_inscription", base, Items.GOLD_BLOCK, ModItems.REROLL_INSCRIPTION.get());
        addUtility(output, "extraction_inscription", base, Items.EMERALD_BLOCK, ModItems.EXTRACTION_INSCRIPTION.get());
    }

    private static void addUtility(
            RecipeOutput output,
            String idPath,
            Ingredient base,
            ItemLike material,
            ItemLike resultItem
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                RunicMod.MOD_ID,
                "etching_table/utility/" + idPath
        );

        EtchingTableRecipe recipe = new EtchingTableRecipe(
                base,
                Ingredient.of(material),
                new ItemStack(resultItem, 1),
                Optional.empty(),
                Optional.empty()
        );

        output.accept(id, recipe, null);
    }
}
