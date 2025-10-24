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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.loot.rarity.EnhancementRarities;
import net.revilodev.runic.loot.rarity.EnhancementRarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MobRuneInjector extends LootModifier {
    public static final MapCodec<MobRuneInjector> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.FLOAT.fieldOf("chance").orElse(0.02f).forGetter(m -> m.chance),
                    Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel),
                    Codec.INT.fieldOf("max_level").orElse(2).forGetter(m -> m.maxLevel)
            )).apply(inst, MobRuneInjector::new));

    private final float chance;
    private final int minLevel;
    private final int maxLevel;

    public MobRuneInjector(LootItemCondition[] conditions, float chance, int minLevel, int maxLevel) {
        super(conditions);
        this.chance = chance;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated, LootContext ctx) {
        if (ctx.getParamOrNull(LootContextParams.THIS_ENTITY) == null) return generated;

        RandomSource rand = ctx.getRandom();
        if (rand.nextFloat() >= chance) return generated;

        Level level = ctx.getLevel();
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        List<Holder<Enchantment>> pool = new ArrayList<>();
        for (Map.Entry<ResourceLocation, EnhancementRarity> e : EnhancementRarities.rawMap().entrySet()) {
            ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, e.getKey());
            reg.getHolder(key).ifPresent(pool::add);
        }
        if (pool.isEmpty()) pool = reg.holders().collect(Collectors.toList());
        if (pool.isEmpty()) return generated;

        Holder<Enchantment> ench = pool.get(rand.nextInt(pool.size()));
        int lvl = Mth.clamp(rand.nextIntBetweenInclusive(minLevel, maxLevel), 1, ench.value().getMaxLevel());
        ItemStack rune = RuneItem.createForEnchantment(new EnchantmentInstance(ench, lvl));
        if (!rune.isEmpty()) generated.add(rune);

        return generated;
    }
}
