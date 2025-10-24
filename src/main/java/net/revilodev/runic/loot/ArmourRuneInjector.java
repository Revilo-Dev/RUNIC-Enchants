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
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.revilodev.runic.RunicMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArmourRuneInjector extends LootModifier {
    public static final MapCodec<ArmourRuneInjector> CODEC = RecordCodecBuilder.mapCodec(inst ->
            LootModifier.codecStart(inst).and(inst.group(
                    Codec.FLOAT.fieldOf("chance").orElse(0.25f).forGetter(m -> m.chance),
                    Codec.INT.fieldOf("min_level").orElse(1).forGetter(m -> m.minLevel),
                    Codec.INT.fieldOf("max_level").orElse(3).forGetter(m -> m.maxLevel)
            )).apply(inst, ArmourRuneInjector::new));

    private final float chance;
    private final int minLevel;
    private final int maxLevel;

    public ArmourRuneInjector(LootItemCondition[] conditions, float chance, int minLevel, int maxLevel) {
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
        ResourceLocation tableId = ctx.getQueriedLootTableId();
        if (tableId == null) return generated;

        String id = tableId.toString();
        if (!(id.contains("chests/") || id.contains("/chest/") || id.contains("structures/"))) {
            return generated;
        }

        RandomSource rand = ctx.getRandom();
        Level level = ctx.getLevel();
        Registry<Enchantment> reg = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);

        for (int i = 0; i < generated.size(); i++) {
            ItemStack stack = generated.get(i);
            if (!stack.is(ItemTags.ARMOR_ENCHANTABLE)) continue;
            if (rand.nextFloat() >= chance) continue;

            List<Holder<Enchantment>> pool = reg.holders()
                    .filter(h -> h.key().location().getNamespace().equals(RunicMod.MOD_ID))
                    .filter(h -> h.value().canEnchant(stack))
                    .collect(Collectors.toList());

            if (pool.isEmpty()) {
                pool = reg.holders()
                        .filter(h -> h.value().canEnchant(stack))
                        .collect(Collectors.toList());
            }
            if (pool.isEmpty()) continue;

            int rolls = 1 + rand.nextInt(2);
            ItemEnchantments.Mutable mut = new ItemEnchantments.Mutable(stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
            boolean changed = false;

            for (int r = 0; r < rolls; r++) {
                Holder<Enchantment> ench = pool.get(rand.nextInt(pool.size()));
                int lvl = Mth.clamp(rand.nextIntBetweenInclusive(minLevel, maxLevel), 1, ench.value().getMaxLevel());
                if (mut.getLevel(ench) < lvl) {
                    mut.set(ench, lvl);
                    changed = true;
                }
            }

            if (changed) {
                stack.set(DataComponents.ENCHANTMENTS, mut.toImmutable());
                generated.set(i, stack);
            }
        }

        return generated;
    }
}
