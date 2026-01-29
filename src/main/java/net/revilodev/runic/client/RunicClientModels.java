package net.revilodev.runic.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;
import net.revilodev.runic.item.custom.RuneItem;
import net.revilodev.runic.stat.RuneStatType;
import net.revilodev.runic.stat.RuneStats;

public final class RunicClientModels {

    private RunicClientModels() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, path);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerAllRuneLikeItems();
        });
    }

    private static void registerAllRuneLikeItems() {
        ResourceLocation pred = id("rune_model");

        register(ModItems.ENHANCED_RUNE.get(), pred);
        register(ModItems.ETCHING.get(), pred);
    }

    private static void register(Item item, ResourceLocation predicateId) {
        ItemProperties.register(item, predicateId, RunicClientModels::runePredicate);
    }

    private static float runePredicate(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        if (stack.isEmpty()) return 0.0F;

        RuneStats stats = RuneStats.get(stack);
        if (stats != null && !stats.isEmpty()) {
            RuneStatType type = stats.view().keySet().iterator().next();
            return mapStatToIndex(type);
        }

        Holder<Enchantment> effect = RuneItem.getPrimaryEffectEnchantment(stack);
        if (effect == null) return 0.0F;

        ResourceLocation rl = effect.unwrapKey().map(k -> k.location()).orElse(null);
        if (rl == null) return 0.0F;

        return mapEnchantToIndex(rl);
    }

    private static float mapStatToIndex(RuneStatType type) {
        return switch (type.id()) {
            case "attack_damage" -> 1f;
            case "attack_range" -> 2f;
            case "attack_speed" -> 3f;
            case "blast_resistance" -> 4f;
            case "bleeding_chance" -> 5f;
            case "bonus_chance" -> 6f;
            case "draw_speed" -> 7f;
            case "durability" -> 8f;
            case "fall_reduction" -> 9f;
            case "fire_resistance" -> 10f;
            case "flame_chance" -> 11f;
            case "freezing_chance" -> 12f;
            case "health" -> 13f;
            case "healing_efficiency" -> 14f;
            case "jump_height" -> 15f;
            case "knockback_resistance" -> 16f;
            case "leeching_chance" -> 17f;
            case "mining_speed" -> 18f;
            case "movement_speed" -> 19f;
            case "nether_damage" -> 20f;
            case "poison_chance" -> 21f;
            case "power" -> 22f;
            case "projectile_resistance" -> 23f;
            case "resistance" -> 24f;
            case "shocking_chance" -> 25f;
            case "stun_chance" -> 26f;
            case "sweeping_range" -> 27f;
            case "swimming_speed" -> 28f;
            case "toughness" -> 29f;
            case "undead_damage" -> 30f;
            case "water_breathing" -> 31f;
            case "weakening_chance" -> 32f;
            case "withering_chance" -> 33f;
            default -> 0f;
        };
    }

    private static float mapEnchantToIndex(ResourceLocation id) {
        return switch (id.toString()) {
            case "combat_roll:acrobat" -> 34f;
            case "farmersdelight:backstabbing" -> 35f;
            case "minecraft:binding_curse" -> 36f;
            case "expanded_combat:blocking" -> 37f;
            case "minecraft:breach" -> 38f;
            case "create:capacity" -> 39f;
            case "deeperdarker:catalysis" -> 40f;
            case "minecraft:channeling" -> 41f;
            case "twilightforest:chill_aura" -> 42f;
            case "minecraft:density" -> 43f;
            case "twilightforest:destruction" -> 44f;
            case "deeperdarker:discharge" -> 45f;
            case "dungeons_arise:ensnaring" -> 46f;
            case "simplyswords:fire_react" -> 47f;
            case "minecraft:flame" -> 48f;
            case "minecraft:fortune" -> 49f;
            case "minecraft:frost_walker" -> 50f;
            case "expanded_combat:ground_slam" -> 51f;
            case "minecraft:impaling" -> 52f;
            case "minecraft:infinity" -> 53f;
            case "dungeons_arise:lolths_curse" -> 54f;
            case "combat_roll:longfooted" -> 55f;
            case "minecraft:looting" -> 56f;
            case "minecraft:loyalty" -> 57f;
            case "minecraft:luck_of_the_sea" -> 58f;
            case "minecraft:lure" -> 59f;
            case "minecraft:mending" -> 60f;
            case "combat_roll:multi_roll" -> 61f;
            case "mysticalagriculture:mystical_enlightenment" -> 62f;
            case "minecraft:piercing" -> 63f;
            case "create:potato_recovery" -> 64f;
            case "dungeons_arise:purification" -> 65f;
            case "aether:renewal" -> 66f;
            case "minecraft:respiration" -> 67f;
            case "minecraft:riptide" -> 68f;
            case "deeperdarker:sculk_smite" -> 69f;
            case "minecraft:silk_touch" -> 70f;
            case "mysticalagriculture:soul_siphoner" -> 71f;
            case "minecraft:soul_speed" -> 72f;
            case "supplementaries:stasis" -> 73f;
            case "minecraft:swift_sneak" -> 74f;
            case "minecraft:thorns" -> 75f;
            case "minecraft:vanishing_curse" -> 76f;
            case "dungeons_arise:voltaic_shot" -> 77f;
            case "minecraft:wind_burst" -> 78f;
            case "minecraft:multishot" -> 79f;
            case "minecraft:punch" -> 80f;
            default -> 0f;
        };
    }
}
