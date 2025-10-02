package net.revilodev.runic.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.revilodev.runic.particle.ModParticles;
import net.revilodev.runic.particle.StunStarParticleProvider;

public class RunicClient {
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.STUN_STAR.get(), StunStarParticleProvider::new);
    }
}
