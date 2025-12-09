package net.revilodev.runic.runes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import net.revilodev.runic.RunicMod;
import net.revilodev.runic.effect.ModMobEffects;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

import java.util.Random;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class CombatHandler {

    private static final Random RNG = new Random();

    private CombatHandler() {}

    /* -----------------------------------------------------------
       DAMAGE BONUS HANDLING (UNDEAD, NETHER)
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {

        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        LivingEntity attacker =
                source.getEntity() instanceof LivingEntity le ? le : null;

        if (attacker == null) return;

        RuneStats stats = RuneStats.get(attacker.getMainHandItem());
        if (stats.isEmpty()) return;

        float bonus = 0f;

        ResourceLocation id = target.getType().builtInRegistryHolder().key().location();
        String path = id.getPath();

        boolean undead =
                path.contains("zombie") ||
                        path.contains("skeleton") ||
                        path.contains("wither") ||
                        path.contains("phantom") ||
                        path.contains("drowned");

        if (undead) bonus += stats.get(RuneStatType.UNDEAD_DAMAGE);

        boolean nether =
                path.contains("blaze") ||
                        path.contains("ghast") ||
                        path.contains("magma") ||
                        path.contains("piglin") ||
                        path.contains("hoglin");

        if (nether) bonus += stats.get(RuneStatType.NETHER_DAMAGE);

        if (bonus > 0f) {
            event.setAmount(event.getAmount() * (1f + bonus / 100f));
        }
    }

    /* -----------------------------------------------------------
       ON-HIT EFFECTS (Bleed, Stun, Poison, Weakness, Flame)
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent.Pre event) {

        DamageSource source = event.getSource();
        LivingEntity target = event.getEntity();

        LivingEntity attacker =
                source.getEntity() instanceof LivingEntity le ? le : null;

        if (attacker == null) return;

        RuneStats stats = RuneStats.get(attacker.getMainHandItem());
        if (stats.isEmpty()) return;

        // BLEEDING (custom effect — DeferredHolder = Holder<MobEffect>)
        if (roll(stats.get(RuneStatType.BLEEDING_CHANCE))) {
            target.addEffect(new MobEffectInstance(ModMobEffects.BLEEDING, 60, 0));
        }

        // STUNNING (custom effect)
        if (roll(stats.get(RuneStatType.STUN_CHANCE))) {
            target.addEffect(new MobEffectInstance(ModMobEffects.STUNNING, 40, 0));
        }

        // POISON (vanilla Holder)
        if (roll(stats.get(RuneStatType.POISON_CHANCE))) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        }

        // WEAKNESS (vanilla Holder)
        if (roll(stats.get(RuneStatType.WEAKENING_CHANCE))) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
        }

        // SHOCKING — disabled for now as requested

        // FLAME
        if (roll(stats.get(RuneStatType.FLAME_CHANCE))) {
            target.setRemainingFireTicks(60);
        }
    }

    /* -----------------------------------------------------------
       UTILITY
       ----------------------------------------------------------- */
    private static boolean roll(float percent) {
        return percent > 0 && RNG.nextFloat() < percent / 100f;
    }
}
