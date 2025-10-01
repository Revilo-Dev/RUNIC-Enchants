package net.revilodev.runic.Enhancements.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;
import net.revilodev.runic.effect.ModMobEffects;

import java.util.List;

public record BleedingAspect() implements EnchantmentEntityEffect {
    public static final MapCodec<BleedingAspect> CODEC = MapCodec.unit(BleedingAspect::new);

    @Override
    public void apply(ServerLevel level, int enchantLevel, EnchantedItemInUse item, Entity entity, Vec3 origin) {
        if (!(entity instanceof LivingEntity living)) return;

        RandomSource random = level.getRandom();

        // ✅ fixed rarity scaling
        float chance = switch (enchantLevel) {
            case 1 -> 0.05f; // 5%
            case 2 -> 0.10f; // 10%
            default -> 0.15f; // 15%
        };

        if (random.nextFloat() < chance) {
            int duration = switch (enchantLevel) {
                case 1 -> 40;   // 2s
                case 2 -> 80;   // 4s
                default -> 120; // 6s
            };
            int amplifier = enchantLevel - 1;

            var holder = level.registryAccess()
                    .registryOrThrow(Registries.MOB_EFFECT)
                    .getHolderOrThrow(ModMobEffects.BLEEDING.getKey());

            living.addEffect(new MobEffectInstance(
                    holder,
                    duration,
                    amplifier,
                    false,
                    true,
                    true
            ));

            level.sendParticles(
                    ParticleTypes.DAMAGE_INDICATOR,
                    living.getX(),
                    living.getY() + living.getBbHeight() * 0.6,
                    living.getZ(),
                    6,
                    0.3,
                    0.2,
                    0.3,
                    0.1
            );
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> codec() {
        return CODEC;
    }

    // ✅ Tooltip for clarity
    public static void appendTooltip(int level, List<Component> tooltip, TooltipFlag flag) {
        String percent = switch (level) {
            case 1 -> "2%";
            case 2 -> "4%";
            default -> "6%";
        };
        String seconds = switch (level) {
            case 1 -> "2";
            case 2 -> "4";
            default -> "6";
        };
        String chance = switch (level) {
            case 1 -> "5%";
            case 2 -> "10%";
            default -> "15%";
        };
        tooltip.add(Component.translatable("enchantment.runic.bleeding.tooltip", percent, seconds, chance));
    }
}
