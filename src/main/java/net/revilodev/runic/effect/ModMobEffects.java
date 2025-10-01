package net.revilodev.runic.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, RunicMod.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = MOB_EFFECTS.register("bleeding", BleedingMobEffect::new);

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
