package net.revilodev.runic.Enhancements;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.Enhancements.custom.*;
import net.revilodev.runic.RunicMod;

public final class ModEnhancementEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_ENCHANTMENT_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, RunicMod.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> LIGHTNING_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("lightning_aspect", () -> LightningAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> POISON_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("poison_aspect", () -> PoisonAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> SLOWNESS_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("slowness_aspect", () -> SlownessAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> WEAKNESS_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("weakness_aspect", () -> WeaknessAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> BLEEDING_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("bleeding_aspect", () -> BleedingAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> STUNNING_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("stunning_aspect", () -> StunningAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> HOARD_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("hoard_aspect", () -> HoardAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> WITHER_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("wither_aspect", () -> WitherAspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> MANDELA_ASPECT =
            ENTITY_ENCHANTMENT_EFFECTS.register("mandela_aspect", () -> Mandela_Aspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> CURSEOFOOZING =
            ENTITY_ENCHANTMENT_EFFECTS.register("curse_of_oozing", () -> Mandela_Aspect.CODEC);
    public static final DeferredHolder<MapCodec<? extends EnchantmentEntityEffect>, MapCodec<? extends EnchantmentEntityEffect>> CURSEOFUNLUCK =
            ENTITY_ENCHANTMENT_EFFECTS.register("curse_of_unluck", () -> Mandela_Aspect.CODEC);


    public static void register(IEventBus modBus) {
        ENTITY_ENCHANTMENT_EFFECTS.register(modBus);

    }
}
