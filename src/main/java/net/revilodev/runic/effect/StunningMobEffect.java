package net.revilodev.runic.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class StunningMobEffect extends MobEffect {
    public StunningMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE6E600);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {

        entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
        entity.hasImpulse = false;
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }
}
