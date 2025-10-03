package net.revilodev.runic.Enhancements.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.revilodev.runic.effect.ModMobEffects;

public record StunningAspect() implements EnchantmentEntityEffect {
    public static final MapCodec<StunningAspect> CODEC = MapCodec.unit(StunningAspect::new);

    @Override
    public void apply(ServerLevel level, int enchantLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!(entity instanceof LivingEntity living)) return;

        RandomSource random = level.getRandom();
        float chance = switch (enchantLevel) {
            case 1 -> 0.05f;
            case 2 -> 0.07f;
            default -> 0.10f;
        };

        if (random.nextFloat() < chance) {
            int duration = switch (enchantLevel) {
                case 1 -> 20;
                case 2 -> 40;
                default -> 50;
            };
            int amplifier = enchantLevel - 1;

            var holder = level.registryAccess()
                    .registryOrThrow(Registries.MOB_EFFECT)
                    .getHolderOrThrow(ModMobEffects.STUNNING.getKey());

            living.addEffect(new MobEffectInstance(holder, duration, amplifier, false, false, true));


        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }
}
