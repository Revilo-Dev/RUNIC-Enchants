package net.revilodev.runic;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class RunicCommonConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MAX_RUNE_LEVEL;
    public static final ModConfigSpec.BooleanValue ENABLE_SOULBOUND;
    public static final ModConfigSpec.DoubleValue SOULBOUND_DROP_CHANCE;

    static {
        BUILDER.push("runic_settings");

        MAX_RUNE_LEVEL = BUILDER
                .comment("Maximum rune enhancement level allowed.")
                .defineInRange("maxRuneLevel", 5, 1, 100);

        ENABLE_SOULBOUND = BUILDER
                .comment("Enable or disable the Soulbound enhancement.")
                .define("enableSoulbound", true);

        SOULBOUND_DROP_CHANCE = BUILDER
                .comment("Chance (0.0–1.0) that Soulbound prevents item dropping on death.")
                .defineInRange("soulboundDropChance", 0.60, 0.0, 1.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
