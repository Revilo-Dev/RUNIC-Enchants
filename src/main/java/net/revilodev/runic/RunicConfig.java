package net.revilodev.runic;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.revilodev.runic.event.EnchantBlacklist;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class RunicConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue DISABLE_ALL;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_RAW;

    private static final AtomicBoolean DISABLE_ALL_CACHE = new AtomicBoolean(false);
    private static final AtomicReference<Set<ResourceLocation>> BLACKLIST_CACHE =
            new AtomicReference<>(Set.of());

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DISABLE_ALL = builder
                .comment("When true, all enchantments are treated as blacklisted")
                .define("enchant_blacklist.disable_all", false);

        BLACKLIST_RAW = builder
                .comment("Enchantments disabled entirely")
                .defineList(
                        "enchant_blacklist.blacklisted",
                        List.of(),
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                );

        SPEC = builder.build();
    }

    private RunicConfig() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RunicConfig::onConfigLoading);
        modBus.addListener(RunicConfig::onConfigReloading);
    }

    public static Set<ResourceLocation> blacklistedEnchantments() {
        return BLACKLIST_CACHE.get();
    }

    public static boolean disableAllEnchantments() {
        return DISABLE_ALL_CACHE.get();
    }

    private static void onConfigLoading(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void onConfigReloading(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) rebuildCache();
    }

    private static void rebuildCache() {
        Set<ResourceLocation> parsed = BLACKLIST_RAW.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());

        boolean disableAll = DISABLE_ALL.get();
        BLACKLIST_CACHE.set(parsed);
        DISABLE_ALL_CACHE.set(disableAll);

        EnchantBlacklist.setConfigDisabled(parsed);
        EnchantBlacklist.setDisableAll(disableAll);
    }
}
