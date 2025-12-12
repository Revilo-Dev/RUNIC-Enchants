package net.revilodev.runic.enchants.soulbound;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.revilodev.runic.RunicMod;

import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class SoulboundEvents {
    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.isSpectator()) return;
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        Iterator<net.minecraft.world.entity.item.ItemEntity> it = event.getDrops().iterator();
        while (it.hasNext()) {
            net.minecraft.world.entity.item.ItemEntity ie = it.next();
            ItemStack stack = ie.getItem();
            if (SoulboundData.hasSoulbound(player.level(), stack)) {
                if (SoulboundData.shouldDamage(player, stack)) {
                    SoulboundData.damageRandomly(player, stack);
                    if (SoulboundData.isBroken(stack)) {
                        if (SoulboundConfig.allowBreakItem) {
                            it.remove();
                            continue;
                        }
                        if (stack.isEmpty()) stack.grow(1);
                        stack.setDamageValue(stack.getMaxDamage() - 1);
                    }
                }
                SoulboundStorage.add(player.getUUID(), stack);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        ServerPlayer newPlayer = event.getEntity() instanceof ServerPlayer sp ? sp : null;
        if (newPlayer == null) return;
        if (newPlayer.isSpectator()) return;
        if (newPlayer.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) return;
        List<ItemStack> restore = SoulboundStorage.take(event.getOriginal().getUUID());
        for (ItemStack s : restore) {
            if (s.isEmpty()) continue;
            if (!newPlayer.getInventory().add(s.copy())) {
                newPlayer.getInventory().placeItemBackInInventory(s.copy());
            }
        }
    }

    private SoulboundEvents() {}
}
