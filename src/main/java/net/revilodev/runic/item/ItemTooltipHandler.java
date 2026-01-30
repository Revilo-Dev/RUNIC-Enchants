package net.revilodev.runic.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.revilodev.runic.RunicMod;

import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTooltipHandler {
    private ItemTooltipHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<net.minecraft.network.chat.Component> tooltip = event.getToolTip();
        if (tooltip.isEmpty()) return;

        if (EnhancementToolTips.apply(stack, tooltip)) return;
        GearTooltips.apply(stack, tooltip);
    }
}
