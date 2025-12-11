package net.revilodev.runic.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.particle.ModParticles;

public class StunningMobEffect extends MobEffect {

    public StunningMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE6E600);

        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stun_speed"),
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stun_damage"),
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setJumping(false);

        if (entity.level() instanceof ServerLevel serverLevel) {
            double radius = 0.55D;
            double y = entity.getY() + entity.getBbHeight() + 0.10D;
            int tick = entity.tickCount;

            for (int i = 0; i < 4; i++) {
                double angleDeg = (tick * 14.0D) + (i * 90.0D);
                double angle = angleDeg * (Math.PI / 180.0D);

                double x = entity.getX() + radius * Math.cos(angle);
                double z = entity.getZ() + radius * Math.sin(angle);

                serverLevel.sendParticles(
                        ModParticles.STUN_STAR.value(),
                        x, y, z,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );
            }
        }

        return true;
    }
}
