package net.revilodev.runic.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

import java.util.Set;

public class RuneItem extends Item {
    private static final Set<ResourceLocation> BLACKLIST = Set.of(
            ResourceLocation.withDefaultNamespace("mending"),
            ResourceLocation.withDefaultNamespace("protection")
    );

    public RuneItem(Properties props) {
        super(props);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    private static boolean isBlacklisted(Holder<Enchantment> enchantHolder) {
        return enchantHolder.unwrapKey()
                .map(k -> BLACKLIST.contains(k.location()))
                .orElse(false);
    }


    public static ItemStack createForEnchantment(EnchantmentInstance inst) {
        if (isBlacklisted(inst.enchantment)) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(ModItems.ENHANCED_RUNE.value());
        stack.enchant(inst.enchantment, inst.level);
        return stack;
    }

    public static ResourceLocation getRuneTexture(ItemStack stack) {
        ResourceLocation base = ResourceLocation.fromNamespaceAndPath(RunicMod.MOD_ID, "item/rune/enhanced_rune");

        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments direct = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        ItemEnchantments enchants = !stored.isEmpty() ? stored : direct;

        if (enchants.isEmpty()) {
            RunicMod.LOGGER.debug("[Runic] No enchantments found on rune: {}", stack);
            return base;
        }

        Holder<Enchantment> ench = enchants.keySet().iterator().next();
        return ench.unwrapKey()
                .map(k -> {
                    ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                            RunicMod.MOD_ID,
                            "item/rune/" + k.location().getNamespace() + "/" + k.location().getPath()
                    );
                    RunicMod.LOGGER.debug("[Runic] Resolved rune texture for {} → {}", k.location(), tex);
                    return tex;
                })
                .orElseGet(() -> {
                    RunicMod.LOGGER.warn("[Runic] Could not resolve key for enchant on rune: {}", stack);
                    return base;
                });
    }

}
