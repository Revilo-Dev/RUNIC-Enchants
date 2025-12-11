package net.revilodev.runic.effect;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.revilodev.runic.RunicMod;

public class BleedingMobEffect extends MobEffect {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "textures/mob_effects/bleeding.png");

    public BleedingMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            float percentPerSecond = 0.02f;
            float damage = percentPerSecond * entity.getMaxHealth();
            entity.hurt(entity.damageSources().magic(), damage);

            if (entity.level() instanceof ServerLevel sl) {
                sl.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                        entity.getX(),
                        entity.getY() + entity.getBbHeight() * 0.6,
                        entity.getZ(),
                        6,
                        0.3,
                        0.25,
                        0.3,
                        0.1
                );
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 20 == 0; // once per second
    }

    public void renderInventoryIcon(MobEffectInstance effect, EffectRenderingInventoryScreen<?> gui, GuiGraphics graphics, int x, int y, float z) {
        graphics.blit(ICON, x, y, 0, 0, 18, 18, 18, 18);
    }

    public void renderGuiIcon(MobEffectInstance effect, GuiGraphics graphics, int x, int y, float z, float alpha) {
        graphics.blit(ICON, x, y, 0, 0, 18, 18, 18, 18);
    }
}
