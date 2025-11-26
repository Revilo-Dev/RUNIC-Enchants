package net.revilodev.runic.stat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.item.ModItems;

import java.util.Locale;
import java.util.Map;

@EventBusSubscriber(modid = RunicMod.MOD_ID, value = Dist.CLIENT)
public final class RuneStatTooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        RuneStats stats = RuneStats.get(stack);
        if (stats == null || stats.isEmpty()) {
            return;
        }

        boolean isRune = stack.is(ModItems.ENHANCED_RUNE.get());

        var tooltip = event.getToolTip();
        tooltip.add(Component.empty());
        tooltip.add((isRune
                ? Component.literal("Rune Stats:")
                : Component.literal("Item Stats:"))
                .withStyle(ChatFormatting.GRAY));

        if (isRune) {
            for (RuneStatType type : stats.view().keySet()) {
                String range = String.format(Locale.ROOT, "%d–%d%%", type.minPercent(), type.maxPercent());
                Component name = Component.translatable("tooltip.runic.stat." + type.id());
                Component line = Component.literal("  ")
                        .append(name)
                        .append(": ")
                        .append(Component.literal(range).withStyle(ChatFormatting.AQUA));
                tooltip.add(line);
            }
        } else {
            for (Map.Entry<RuneStatType, Float> entry : stats.view().entrySet()) {
                RuneStatType type = entry.getKey();
                float value = entry.getValue();
                if (value == 0.0F) continue;

                Component name = Component.translatable("tooltip.runic.stat." + type.id());

                String valueStr;
                if (Math.abs(value - Math.round(value)) < 0.001F) {
                    valueStr = String.format(Locale.ROOT, "+%d%%", (int) value);
                } else {
                    valueStr = String.format(Locale.ROOT, "+%.1f%%", value);
                }

                Component line = Component.literal("  ")
                        .append(name)
                        .append(": ")
                        .append(Component.literal(valueStr).withStyle(ChatFormatting.AQUA));

                tooltip.add(line);
            }
        }
    }
}
