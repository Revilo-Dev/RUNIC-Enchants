package net.revilodev.runic.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.revilodev.runic.RunicMod;

import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class ItemBlacklistEvents {
    private ItemBlacklistEvents() {}

    /** Prevent picking up blacklisted items (server-side). */
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack stack = itemEntity.getItem();

        if (stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.ENCHANTING_TABLE)) {
            itemEntity.discard();
        }
    }

    /** Disable master-level librarian book trades (server-side). */
    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            List<VillagerTrades.ItemListing> master = trades.get(5); // 5 = master level
            if (master != null) {
                master.clear();
            }
        }
    }

    /** Remove all enchanted books and enchanting tables from creative inventory (client-side). */
    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (Minecraft.getInstance().level != null) {
            var registryAccess = Minecraft.getInstance().level.registryAccess();
            var enchRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);

            enchRegistry.holders().forEach(ench -> {
                Enchantment e = ench.value();
                for (int lvl = e.getMinLevel(); lvl <= e.getMaxLevel(); lvl++) {
                    ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ench, lvl));
                    event.remove(book, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            });
        }

        // Remove enchanting table globally
        event.remove(new ItemStack(Items.ENCHANTING_TABLE), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
