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

    @SubscribeEvent
    public static void onPlace(GrindstoneEvent.OnPlaceItem e) {
        ItemStack top = e.getTopItem();
        ItemStack bottom = e.getBottomItem();

        boolean topIsGear = RuneSlots.capacity(top) > 0;
        boolean bottomIsGear = RuneSlots.capacity(bottom) > 0;

        boolean topHasEnhancements =
                topIsGear &&
                        (!top.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() ||
                                !top.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() ||
                                RuneSlots.used(top) > 0 ||
                                !RuneStats.get(top).isEmpty());

        boolean bottomHasEnhancements =
                bottomIsGear &&
                        (!bottom.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() ||
                                !bottom.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty() ||
                                RuneSlots.used(bottom) > 0 ||
                                !RuneStats.get(bottom).isEmpty());

        if (topHasEnhancements && bottom.isEmpty()) {
            ItemStack out = top.copy();
            clearEnhancements(out);
            e.setOutput(out);
        } else if (bottomHasEnhancements && top.isEmpty()) {
            ItemStack out = bottom.copy();
            clearEnhancements(out);
            e.setOutput(out);
        }
    }

    private static void clearEnhancements(ItemStack stack) {
        stack.remove(DataComponents.ENCHANTMENTS);
        stack.remove(DataComponents.STORED_ENCHANTMENTS);
        stack.set(ModDataComponents.RUNE_SLOTS_USED.get(), 0);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove("runic_stats"));
    }
}
