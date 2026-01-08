package net.revilodev.runic.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.RuneItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunicStructureLootInjector extends LootModifier {
    public static final MapCodec<RunicStructureLootInjector> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.FLOAT.fieldOf("rune_chance").orElse(0.35f).forGetter(m -> m.runeChance),
                    Codec.FLOAT.fieldOf("armor_chance").orElse(0.0f).forGetter(m -> m.armorChance),
                    Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel),
                    Codec.INT.fieldOf("max_level").orElse(1).forGetter(m -> m.maxLevel)
            )).apply(inst, RunicStructureLootInjector::new));

    private final float runeChance;
    private final float armorChance;
    private final int minLevel;
    private final int maxLevel;

    public RunicStructureLootInjector(LootItemCondition[] conditions, float runeChance, float armorChance, int minLevel, int maxLevel) {
        super(conditions);
        this.runeChance = runeChance;
        this.armorChance = armorChance;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext ctx) {
        if (ctx.getParamOrNull(LootContextParams.BLOCK_STATE) != null) {
            return generated;
        }

        ResourceLocation tableId = ctx.getQueriedLootTableId();
        if (tableId == null) {
            return generated;
        }

        String id = tableId.toString().toLowerCase(Locale.ROOT);
        if (!isStructureLootTable(id)) {
            return generated;
        }

        Level level = ctx.getLevel();
        RandomSource rand = ctx.getRandom();

        maybeAddRunes(generated, rand, level, id);

        return generated;
    }

    private static boolean isStructureLootTable(String id) {
        if (id.contains("villager") || id.contains("fishing") || id.contains("entity/") ||
                id.contains("block/") || id.contains("blocks/") || id.contains("gameplay/") || id.contains("trades/")) {
            return false;
        }
        return id.contains("chest") || id.contains("chests/") || id.contains("structures/") ||
                id.contains("dungeon") || id.contains("temple") || id.contains("ruin") ||
                id.contains("bastion") || id.contains("ancient_city") || id.contains("shipwreck") ||
                id.contains("fortress") || id.contains("stronghold") || id.contains("mineshaft");
    }

    private void maybeAddRunes(ObjectArrayList<ItemStack> generated, RandomSource rand, Level level, String tableId) {
        if (rand.nextFloat() >= this.runeChance) {
            return;
        }

        int rolls = tableId.contains("bastion") || tableId.contains("ancient_city") ? 2 : 1;

        for (int i = 0; i < rolls; i++) {
            if (rand.nextFloat() < 0.25f) {
                ItemStack util = randomUtilityRune(rand);
                if (!util.isEmpty()) {
                    generated.add(util);
                }
            } else {
                if (rand.nextFloat() < 0.8f) {
                    ItemStack statRune = RuneItem.createRandomStatRune(rand);
                    if (!statRune.isEmpty()) {
                        generated.add(statRune);
                    }
                } else {
                    ItemStack effectRune = randomEffectRune(level, rand);
                    if (!effectRune.isEmpty()) {
                        generated.add(effectRune);
                    }
                }
            }
        }
    }

    private static ItemStack randomUtilityRune(RandomSource rand) {
        int roll = rand.nextInt(12);
        if (roll < 6) {
            return new ItemStack(ModItems.REPAIR_INSCRIPTION.get());
        } else if (roll < 9) {
            return new ItemStack(ModItems.EXPANSION_INSCRIPTION.get());
        } else if (roll < 11) {
            return new ItemStack(ModItems.NULLIFICATION_INSCRIPTION.get());
        } else {
            return new ItemStack(ModItems.UPGRADE_INSCRIPTION.get());
        }
    }

    private static ItemStack randomEffectRune(Level level, RandomSource rand) {
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        List<Holder<Enchantment>> pool = new ArrayList<>();
        for (ResourceLocation id : RuneItem.allowedEffectIds()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, id);
            reg.getHolder(key).ifPresent(pool::add);
        }
        if (pool.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Holder<Enchantment> chosen = pool.get(rand.nextInt(pool.size()));
        return RuneItem.createEffectRune(chosen);

    }
}
