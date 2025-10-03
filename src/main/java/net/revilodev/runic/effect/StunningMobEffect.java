package net.revilodev.runic.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.revilodev.runic.RunicMod;

public class StunningMobEffect extends MobEffect {
    public StunningMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE6E600);
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "stun_speed"),
                -1.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setJumping(false);
        if (entity.level() instanceof ServerLevel sl) {
            double radius = 0.6;
            int tick = entity.tickCount;
            for (int i = 0; i < 3; i++) {
                double angleDeg = tick * 0.3 + (i * 120);
                double angle = angleDeg * (Math.PI / 180.0);
                double x = entity.getX() + radius * Math.cos(angle);
                double z = entity.getZ() + radius * Math.sin(angle);
                double y = entity.getY() + entity.getBbHeight() + 0.4;
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return true;
    }
}
