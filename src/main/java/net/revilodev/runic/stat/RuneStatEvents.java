package net.revilodev.runic.stat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
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
