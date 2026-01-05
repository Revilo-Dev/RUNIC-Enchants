package net.revilodev.runic.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.revilodev.runic.network.payload.PlaceEtchingRecipePayload;

public final class RunicNetwork {
    private RunicNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToServer(PlaceEtchingRecipePayload.TYPE, PlaceEtchingRecipePayload.STREAM_CODEC, PlaceEtchingRecipePayload::handle);
    }
}
