package net.revilodev.runic.runes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

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
       DAMAGE BONUS HANDLING
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

        float amount = event.getAmount();
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
            amount *= (1f + bonus / 100f);
        }

        // Arrow "Power" modifier
        if (source.getDirectEntity() instanceof AbstractArrow) {
            float power = stats.get(RuneStatType.POWER);
            if (power > 0f) {
                amount *= (1f + power / 100f);
            }
        }

        event.setAmount(amount);
    }


    /* -----------------------------------------------------------
       LEECHING
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        Entity src = source.getEntity();
        if (!(src instanceof LivingEntity attacker)) return;
        if (attacker.level().isClientSide) return;

        RuneStats stats = RuneStats.get(attacker.getMainHandItem());
        if (stats.isEmpty()) return;

        float chance = stats.get(RuneStatType.LEECHING_CHANCE);
        if (chance <= 0f) return;

        if (RNG.nextFloat() <= chance / 100f) {
            float healAmount = event.getEntity().getMaxHealth() * 0.10f;
            attacker.heal(healAmount);
        }
    }
    


    /* -----------------------------------------------------------
       BONUS ARROW
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {

        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof LivingEntity shooter)) return;

        if (arrow.getPersistentData().getBoolean("runic_bonus_arrow")) return;

        RuneStats stats = RuneStats.get(shooter.getMainHandItem());
        if (stats.isEmpty()) return;

        float chance = stats.get(RuneStatType.BONUS_CHANCE);
        if (chance <= 0f) return;

        if (RNG.nextFloat() > chance / 100f) return;

        AbstractArrow extra = (AbstractArrow) arrow.getType().create(level);
        if (extra == null) return;

        extra.setOwner(shooter);
        extra.copyPosition(arrow);

        var vel = arrow.getDeltaMovement();
        double spread = 0.05;
        extra.setDeltaMovement(
                vel.x + (level.random.nextDouble() - 0.5) * spread,
                vel.y,
                vel.z + (level.random.nextDouble() - 0.5) * spread
        );

        extra.getPersistentData().putBoolean("runic_bonus_arrow", true);

        level.addFreshEntity(extra);
    }


    /* -----------------------------------------------------------
       STUN DAMAGE REDUCTION
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onDamageFromStunned(LivingDamageEvent.Pre event) {

        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;

        if (attacker.hasEffect(ModMobEffects.STUNNING)) {
            float modified = event.getNewDamage() * 0.5f;
            event.setNewDamage(modified);
        }
    }


    /* -----------------------------------------------------------
       APPLY STUN EFFECT (NO SWIRL PARTICLES)
       ----------------------------------------------------------- */
    @SubscribeEvent
    public static void onApplyStunChance(LivingDamageEvent.Pre event) {

        DamageSource src = event.getSource();
        LivingEntity target = event.getEntity();

        LivingEntity attacker =
                src.getEntity() instanceof LivingEntity le ? le : null;

        if (attacker == null) return;

        RuneStats stats = RuneStats.get(attacker.getMainHandItem());
        if (stats.isEmpty()) return;

        float chance = stats.get(RuneStatType.STUN_CHANCE);
        if (chance <= 0f) return;

        if (RNG.nextFloat() > chance / 100f) return;

        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ModMobEffects.STUNNING, 40, 0,
                false, // ambient
                false, // NO vanilla swirl particles
                true   // still show icon
        ));
    }
}
