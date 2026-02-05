package net.revilodev.runic.loot.rarity;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.revilodev.runic.RunicMod;
import net.revilodev.runic.network.EnhancementRarityDataSync;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public class EnhancementRarityDatapackSyncHandler {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent e) {
        EnhancementRarityDataSync payload = new EnhancementRarityDataSync(
                EnhancementRarities.exportDefaultKey(),
                EnhancementRarities.exportKeyMap()
        );

        ServerPlayer player = e.getPlayer();
        if (player != null) {
            PacketDistributor.sendToPlayer(player, payload);
        } else {
            PacketDistributor.sendToAllPlayers(payload);
        }
    }
}
