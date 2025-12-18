package net.revilodev.runic.event;

import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class BlacklistStrippingEvents {
    private BlacklistStrippingEvents() {}

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity entity = event.getItemEntity();
        ItemStack stack = entity.getItem();

        if (stack.is(Items.ENCHANTING_TABLE)) {
            entity.discard();
            return;
        }

        if (EnchantBlacklist.strip(stack)) {
            entity.setItem(stack);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(Items.ENCHANTING_TABLE)) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
                continue;
            }

            if (!stack.isEmpty()) {
                EnchantBlacklist.strip(stack);
            }
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        AbstractContainerMenu menu = event.getContainer();

        for (Slot slot : menu.slots) {
            Container container = slot.container;
            int index = slot.getSlotIndex();

            if (index >= 0 && index < container.getContainerSize()) {
                ItemStack stack = container.getItem(index);
                if (!stack.isEmpty()) {
                    EnchantBlacklist.strip(stack);
                }
            }
        }
    }
}
