package net.revilodev.runic.runes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.registry.ModDataComponents;
import net.revilodev.runic.stat.RuneStats;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class RuneSlotResetHandler {

    private RuneSlotResetHandler() {}

    @SubscribeEvent
    public static void onPlace(GrindstoneEvent.OnPlaceItem e) {
        ItemStack top = e.getTopItem();
        ItemStack bottom = e.getBottomItem();

        if (!top.isEmpty() && bottom.isEmpty()) {
            if (hasRunicEnhancements(top)) {
                ItemStack out = top.copy();
                clearEnhancements(out);
                e.setOutput(out);
                e.setXp(0);
            }
            return;
        }

        if (!bottom.isEmpty() && top.isEmpty()) {
            if (hasRunicEnhancements(bottom)) {
                ItemStack out = bottom.copy();
                clearEnhancements(out);
                e.setOutput(out);
                e.setXp(0);
            }
        }
    }

    private static boolean hasRunicEnhancements(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (RuneSlots.used(stack) > 0) return true;
        if (!RuneStats.get(stack).isEmpty()) return true;

        return stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
                || !stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
                || !stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    private static void clearEnhancements(ItemStack stack) {
        stack.remove(DataComponents.ENCHANTMENTS);
        stack.remove(DataComponents.STORED_ENCHANTMENTS);
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);

        stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), 0);

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(RuneStats.NBT_KEY));
    }
}
