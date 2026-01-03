package net.revilodev.runic.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.stat.RuneStatType;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RunicMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNE_SLOTS_USED =
            DATA_COMPONENT_TYPES.register("rune_slots_used", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNE_EXPANSIONS_USED =
            DATA_COMPONENT_TYPES.register("rune_expansions_used", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNE_SLOTS_CAPACITY =
            DATA_COMPONENT_TYPES.register("rune_slots_capacity", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RuneStatType>> RUNE_STAT_TYPE =
            DATA_COMPONENT_TYPES.register("rune_stat_type", () ->
                    DataComponentType.<RuneStatType>builder()
                            .persistent(RuneStatType.CODEC)
                            .networkSynchronized(RuneStatType.STREAM_CODEC)
                            .build());

    private ModDataComponents() {}
}
