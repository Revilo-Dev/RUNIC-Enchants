package net.revilodev.runic.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

public final class RunicClientModels {

    private RunicClientModels() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, path);
    }

    public static void init() {
        ItemProperties.register(
                ModItems.ENHANCED_RUNE.get(),
                id("rune_model"),
                RunicClientModels::runePredicate
        );
    }

    private static float runePredicate(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        if (stack.isEmpty()) return 0.0F;

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            RuneStatType type = stats.view().keySet().iterator().next();
            return mapStatToIndex(type);
        }

        ItemEnchantments enchants =
                stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        if (enchants.isEmpty()) {
            enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }

        if (enchants.isEmpty()) return 0.0F;

        Holder<Enchantment> ench = enchants.keySet().iterator().next();
        String path = ench.unwrapKey().map(k -> k.location().getPath()).orElse("");

        return mapEnchantToIndex(path);
    }

    private static float mapStatToIndex(RuneStatType type) {
        return switch (type.id()) {
            case "attack_damage" -> 1;
            case "attack_range" -> 2;
            case "attack_speed" -> 3;
            case "blast_resistance" -> 4;
            case "bleeding_chance" -> 5;
            case "bonus_chance" -> 6;
            case "draw_speed" -> 7;
            case "durability" -> 8;
            case "fall_reduction" -> 9;
            case "fire_resistance" -> 10;
            case "flame_chance" -> 11;
            case "freezing_chance" -> 12;
            case "health" -> 13;
            case "healing_efficiency" -> 14;
            case "jump_height" -> 15;
            case "knockback_resistance" -> 16;
            case "leeching_chance" -> 17;
            case "mining_speed" -> 18;
            case "movement_speed" -> 19;
            case "nether_damage" -> 20;
            case "poison_chance" -> 21;
            case "power" -> 22;
            case "projectile_resistance" -> 23;
            case "resistance" -> 24;
            case "shocking_chance" -> 25;
            case "stun_chance" -> 26;
            case "sweeping_range" -> 27;
            case "swimming_speed" -> 28;
            case "toughness" -> 29;
            case "undead_damage" -> 30;
            case "water_breathing" -> 31;
            case "weakening_chance" -> 32;
            case "withering_chance" -> 33;
            default -> 0;
        };
    }

    private static float mapEnchantToIndex(String path) {
        return switch (path) {
            case "acrobat" -> 34;
            case "backstabbing" -> 35;
            case "binding_curse" -> 36;
            case "blocking" -> 37;
            case "breach" -> 38;
            case "capacity" -> 39;
            case "catalysis" -> 40;
            case "channeling" -> 41;
            case "chill_aura" -> 42;
            case "density" -> 43;
            case "destruction" -> 44;
            case "discharge" -> 45;
            case "ensnaring" -> 46;
            case "fire_react" -> 47;
            case "flame" -> 48;
            case "fortune" -> 49;
            case "frost_walker" -> 50;
            case "ground_slam" -> 51;
            case "impaling" -> 52;
            case "infinity" -> 53;
            case "lolths_curse" -> 54;
            case "longfooted" -> 55;
            case "looting" -> 56;
            case "loyalty" -> 57;
            case "luck_of_the_sea" -> 58;
            case "lure" -> 59;
            case "mending" -> 60;
            case "multi_roll" -> 61;
            case "mystical_enlightenment" -> 62;
            case "piercing" -> 63;
            case "potato_recovery" -> 64;
            case "purification" -> 65;
            case "renewal" -> 66;
            case "respiration" -> 67;
            case "riptide" -> 68;
            case "sculk_smite" -> 69;
            case "silk_touch" -> 70;
            case "soul_siphoner" -> 71;
            case "soul_speed" -> 72;
            case "stasis" -> 73;
            case "swift_sneak" -> 74;
            case "thorns" -> 75;
            case "vanishing_curse" -> 76;
            case "voltaic_shot" -> 77;
            case "wind_burst" -> 78;
            case "multishot" -> 79;
            case "punch" -> 80;
            default -> 0;
        };
    }
}
