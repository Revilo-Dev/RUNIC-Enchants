package net.revilodev.runic.loot.rarity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.revilodev.runic.RunicMod;

@EventBusSubscriber(modid = RunicMod.MOD_ID)
public final class EnhancementRarityReloadListenerRegistrar {
    private EnhancementRarityReloadListenerRegistrar() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent e) {
        e.addListener(new EnhancementRarityData());
    }
}
