package net.revilodev.runic.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.revilodev.runic.RunicMod;

import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class ItemBlacklistEvents {
    private ItemBlacklistEvents() {}

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            List<VillagerTrades.ItemListing> master = trades.get(5);
            if (master != null) {
                master.clear();
            }
        }
    }

    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (Minecraft.getInstance().level == null) return;

        var enchReg = Minecraft.getInstance()
                .level
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT);

        enchReg.holders().forEach(holder -> {
            Enchantment ench = holder.value();
            for (int lvl = ench.getMinLevel(); lvl <= ench.getMaxLevel(); lvl++) {
                ItemStack book =
                        EnchantedBookItem.createForEnchantment(
                                new EnchantmentInstance(holder, lvl)
                        );
                event.remove(book, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
        });

        event.remove(
                new ItemStack(Items.ENCHANTING_TABLE),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
    }
}
