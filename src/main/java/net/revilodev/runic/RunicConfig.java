package net.revilodev.runic;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class RunicConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST_RAW;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        BLACKLIST_RAW = builder
                .comment("Enchantments disabled entirely")
                .defineList(
                        "enchant_blacklist.blacklisted",
                        List.of(),
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null
                );

        SPEC = builder.build();
    }

    public static Set<ResourceLocation> blacklistedEnchantments() {
        return BLACKLIST_RAW.get().stream()
                .map(ResourceLocation::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
