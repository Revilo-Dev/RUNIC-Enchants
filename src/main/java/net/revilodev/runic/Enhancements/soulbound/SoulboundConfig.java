package net.revilodev.runic.Enhancements.soulbound;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class SoulboundConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.BooleanValue ALLOW_BREAK_ITEM = BUILDER.define("allowBreakItem", false);
    private static final ModConfigSpec.IntValue MAX_DAMAGE_PERCENT = BUILDER.defineInRange("maxDamagePercent", 20, 0, 100);
    public static final ModConfigSpec SPEC = BUILDER.build();
    public static boolean allowBreakItem;
    public static int maxDamagePercent;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        allowBreakItem = ALLOW_BREAK_ITEM.get();
        maxDamagePercent = MAX_DAMAGE_PERCENT.get();
    }

    private SoulboundConfig() {}
}
