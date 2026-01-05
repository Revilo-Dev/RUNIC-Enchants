package net.revilodev.runic.client;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.revilodev.runic.screen.ModMenuTypes;
import net.revilodev.runic.screen.custom.ArtisansWorkbenchScreen;
import net.revilodev.runic.screen.custom.EtchingTableScreen;

public final class RunicClient {
    private RunicClient() {
    }

    public static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ARTISANS_WORKBENCH.get(), ArtisansWorkbenchScreen::new);
        event.register(ModMenuTypes.ETCHING_TABLE.get(), EtchingTableScreen::new);
    }
}
