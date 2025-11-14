package net.revilodev.runic.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;

import java.util.*;

public class RunicStructureLootInjector extends LootModifier {
    public static final MapCodec<RunicStructureLootInjector> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.FLOAT.fieldOf("rune_chance").orElse(0.35f).forGetter(m -> m.runeChance),
                    Codec.FLOAT.fieldOf("armor_chance").orElse(0.30f).forGetter(m -> m.armorChance),
                    Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel),
                    Codec.INT.fieldOf("max_level").orElse(3).forGetter(m -> m.maxLevel)
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
        ResourceLocation tableId = ctx.getQueriedLootTableId();
        if (tableId == null) return generated;

        String id = tableId.toString().toLowerCase(Locale.ROOT);
        if (!isStructureLootTable(id)) return generated;

        Level level = ctx.getLevel();
        RandomSource rand = ctx.getRandom();
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        maybeAddRunes(generated, rand, reg, id);
        maybeEnchantArmor(generated, rand, reg);

        return generated;
    }

    private boolean isStructureLootTable(String id) {
        if (id.contains("villager") || id.contains("fishing") || id.contains("entity/") ||
                id.contains("block/") || id.contains("gameplay/") || id.contains("trades/"))
            return false;

        return id.contains("chest") || id.contains("chests/") || id.contains("structures/") ||
                id.contains("dungeon") || id.contains("temple") || id.contains("ruin") ||
                id.contains("bastion") || id.contains("ancient_city") || id.contains("shipwreck") ||
                id.contains("fortress") || id.contains("stronghold") || id.contains("mineshaft");
    }

    private void maybeAddRunes(ObjectArrayList<ItemStack> generated, RandomSource rand, Registry<Enchantment> reg, String tableId) {
        if (rand.nextFloat() >= runeChance) return;

        List<Holder.Reference<Enchantment>> all = new ArrayList<>();
        EnhancementRarities.rawMap().forEach((id, rarity) ->
                reg.getHolder(ResourceKey.create(Registries.ENCHANTMENT, id)).ifPresent(all::add));
        if (all.isEmpty()) reg.holders().forEach(all::add);
        if (all.isEmpty()) return;

        int rolls = tableId.contains("bastion") || tableId.contains("ancient_city") ? 2 : 1;

        for (int i = 0; i < rolls; i++) {
            boolean utilityRune = rand.nextBoolean();

            if (!utilityRune) {
                Holder.Reference<Enchantment> ench = all.get(rand.nextInt(all.size()));
                int lvl = Mth.clamp(Mth.randomBetweenInclusive(rand, minLevel, maxLevel), 1, ench.value().getMaxLevel());
                ItemStack rune = RuneItem.createForEnchantment(new EnchantmentInstance(ench, lvl));
                if (!rune.isEmpty()) generated.add(rune);
            } else {
                int roll = rand.nextInt(12);
                ItemStack util;
                if (roll < 6) util = new ItemStack(ModItems.REPAIR_RUNE.get());
                else if (roll < 9) util = new ItemStack(ModItems.EXPANSION_RUNE.get());
                else if (roll < 11) util = new ItemStack(ModItems.NULLIFICATION_RUNE.get());
                else util = new ItemStack(ModItems.UPGRADE_RUNE.get());
                generated.add(util);
            }
        }
    }

    private void maybeEnchantArmor(ObjectArrayList<ItemStack> generated, RandomSource rand, Registry<Enchantment> reg) {
        for (int i = 0; i < generated.size(); i++) {
            ItemStack stack = generated.get(i);
            if (!stack.is(ItemTags.ARMOR_ENCHANTABLE)) continue;
            if (rand.nextFloat() >= armorChance) continue;

            List<Holder.Reference<Enchantment>> pool = reg.holders()
                    .filter(h -> h.key().location().getNamespace().equals(RunicMod.MOD_ID) && h.value().canEnchant(stack))
                    .toList();
            if (pool.isEmpty())
                pool = reg.holders().filter(h -> h.value().canEnchant(stack)).toList();
            if (pool.isEmpty()) continue;

            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(
                    stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
            );

            Holder.Reference<Enchantment> ench = pool.get(rand.nextInt(pool.size()));
            int lvl = Mth.clamp(Mth.randomBetweenInclusive(rand, minLevel, maxLevel), 1, ench.value().getMaxLevel());
            mut.set(ench, lvl);

            stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
            generated.set(i, stack);
        }
    }
}
