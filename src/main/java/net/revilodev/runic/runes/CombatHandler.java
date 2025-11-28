package net.revilodev.runic.runes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.Random;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class CombatHandler {

    private static final Random RNG = new Random();

    // ---------------------------------------------------------
    // DAMAGE BONUSES (UNDEAD, NETHER)
    // ---------------------------------------------------------
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        LivingEntity target = event.getEntity();
        ItemStack weapon = attacker.getMainHandItem();
        RuneStats stats = RuneStats.get(weapon);

        float bonusPercent = 0f;

        // --- Detect undead by registry name ---
        ResourceLocation id = target.getType().builtInRegistryHolder().key().location();
        String path = id.getPath();

        boolean isUndead =
                path.contains("zombie") ||
                        path.contains("skeleton") ||
                        path.contains("drowned") ||
                        path.contains("phantom") ||
                        path.contains("wither");

        if (isUndead) {
            bonusPercent += stats.get(RuneStatType.UNDEAD_DAMAGE);
        }

        // --- Detect nether mobs ---
        boolean isNether =
                path.contains("blaze") ||
                        path.contains("magma") ||
                        path.contains("ghast") ||
                        path.contains("piglin") ||
                        path.contains("hoglin");

        if (isNether) {
            bonusPercent += stats.get(RuneStatType.NETHER_DAMAGE);
        }

        // Apply bonus
        if (bonusPercent > 0) {
            float extra = event.getAmount() * (bonusPercent / 100f);
            event.setAmount(event.getAmount() + extra);
        }
    }

    // ---------------------------------------------------------
    // ON-HIT EFFECT CHANCES
    // ---------------------------------------------------------
    @SubscribeEvent
    public static void onPreDamage(LivingDamageEvent.Pre event) {

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        LivingEntity target = event.getEntity();
        ItemStack weapon = attacker.getMainHandItem();
        RuneStats stats = RuneStats.get(weapon);

        // ----------------------------------------------------------------
        // ALL EFFECTS REQUIRE YOU TO ADD THEM (I ADDED TODO MARKERS)
        // ----------------------------------------------------------------

        // Bleed
        tryRoll(stats.get(RuneStatType.BLEEDING_CHANCE), () -> {
            // TODO: target.addEffect(new MobEffectInstance(ModEffects.BLEEDING, duration, amp));
        });

        // Stun
        tryRoll(stats.get(RuneStatType.STUN_CHANCE), () -> {
            // TODO: target.addEffect(new MobEffectInstance(ModEffects.STUNNED, duration, 0));
        });

        // Poison
        tryRoll(stats.get(RuneStatType.POISON_CHANCE), () -> {
            // TODO: target.addEffect(new MobEffectInstance(ModEffects.POISONED, duration, 1));
        });

        // Weaken
        tryRoll(stats.get(RuneStatType.WEAKENING_CHANCE), () -> {
            // TODO: target.addEffect(new MobEffectInstance(ModEffects.WEAKENED, duration, 1));
        });

        // Shock
        tryRoll(stats.get(RuneStatType.SHOCKING_CHANCE), () -> {
            // TODO: target.addEffect(new MobEffectInstance(ModEffects.SHOCKED, duration, 0));
        });

        // Flame
        tryRoll(stats.get(RuneStatType.FLAME_CHANCE), () -> {
            target.setRemainingFireTicks(60); // 3 seconds
        });
    }

    // ---------------------------------------------------------
    // HEALING EFFICIENCY
    // ---------------------------------------------------------
    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {

        LivingEntity entity = event.getEntity();
        if (!(entity instanceof LivingEntity)) return;

        ItemStack held = entity.getMainHandItem();
        RuneStats stats = RuneStats.get(held);

        float bonus = stats.get(RuneStatType.HEALING_EFFICIENCY);
        if (bonus > 0) {
            float extra = event.getAmount() * (bonus / 100f);
            event.setAmount(event.getAmount() + extra);
        }
    }

    // ---------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------
    private static void tryRoll(float percent, Runnable effect) {
        if (percent > 0 && RNG.nextFloat() < percent / 100f) {
            effect.run();
        }
    }
}
