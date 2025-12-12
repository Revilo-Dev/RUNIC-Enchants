package net.revilodev.runic.stat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RuneStatEvents {

    private RuneStatEvents() {}

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        DamageSource src = event.getSource();

        float amount = event.getAmount();
        if (amount <= 0.0F) return;

        float generic = getTotal(target, RuneStatType.RESISTANCE);
        float fire = getTotal(target, RuneStatType.FIRE_RESISTANCE);
        float blast = getTotal(target, RuneStatType.BLAST_RESISTANCE);
        float proj = getTotal(target, RuneStatType.PROJECTILE_RESISTANCE);

        amount *= reduce(generic);

        if (src.is(DamageTypeTags.IS_FIRE)) {
            amount *= reduce(fire);
        }
        if (src.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= reduce(proj);
        }
        if (src.is(DamageTypeTags.IS_EXPLOSION)) {
            amount *= reduce(blast);
        }

        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        float eff = getTotal(entity, RuneStatType.HEALING_EFFICIENCY);
        if (eff > 0.0F) {
            event.setAmount(event.getAmount() * (1.0F + eff / 100.0F));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (entity.level().isClientSide) return;

        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) return;

        float jump = RuneStats.get(boots).get(RuneStatType.JUMP_HEIGHT);
        if (jump <= 0) return;

        int amplifier = (int)(jump / 10f);
        if (amplifier < 0) amplifier = 0;

        entity.addEffect(new MobEffectInstance(
                MobEffects.JUMP,
                5,
                amplifier,
                false,
                false,
                false
        ));
    }

    private static float getTotal(LivingEntity e, RuneStatType type) {
        float total = 0.0F;
        for (ItemStack s : e.getAllSlots()) {
            total += RuneStats.get(s).get(type);
        }
        return total;
    }

    private static float reduce(float percent) {
        if (percent <= 0.0F) return 1.0F;
        return 1.0F - Math.min(percent, 90.0F) / 100.0F;
    }
}
