package net.revilodev.runic.enchants.soulbound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.revilodev.runic.RunicMod;

import java.util.Optional;

public final class SoulboundData {
    public static final ResourceKey<Enchantment> SOULBOUND = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "soulbound"));

    public static boolean hasSoulbound(Level level, ItemStack stack) {
        Optional<Holder.Reference<Enchantment>> holder = level.registryAccess().registry(Registries.ENCHANTMENT).flatMap(r -> r.getHolder(SOULBOUND));
        return holder.filter(h -> stack.getEnchantmentLevel(h) > 0).isPresent();
    }

    public static boolean shouldDamage(Player player, ItemStack stack) {
        return SoulboundConfig.maxDamagePercent != 0 && !player.isCreative() && stack.isDamageableItem();
    }

    public static boolean isBroken(ItemStack stack) {
        return stack.getDamageValue() >= stack.getMaxDamage();
    }

    public static void damageRandomly(Player player, ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        int damageRange = maxDamage * SoulboundConfig.maxDamagePercent / 100;
        if (damageRange <= 0) damageRange = maxDamage;
        RandomSource random = player.getRandom();
        Level l = player.level();
        if (l instanceof ServerLevel level) {
            stack.hurtAndBreak(random.nextInt(damageRange) + 1, level, player, s -> {});
        }
    }

    private SoulboundData() {}
}
