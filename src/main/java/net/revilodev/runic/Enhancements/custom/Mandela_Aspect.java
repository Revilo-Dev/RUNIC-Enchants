package net.revilodev.runic.Enhancements.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record Mandela_Aspect() implements EnchantmentEntityEffect {
    public static final MapCodec<Mandela_Aspect> CODEC = MapCodec.unit(Mandela_Aspect::new);

    @Override
    public void apply(ServerLevel level, int enchantLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        RandomSource random = level.getRandom();

        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        if (enchantLevel == 1) {
            if (random.nextFloat() < 0.25f) { // 25%
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0)); // 5s, Confusion I
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0)); // 5s, Darkness I
            }
        } else if (enchantLevel == 2) {
            if (random.nextFloat() < 0.50f) { // 50%
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1)); // 10s, Confusion II
                living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1)); // 5s, Blindness I
                living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 1)); // 10s, Darkness II
            }
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}