package net.revilodev.runic.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class StunStarParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprites;

    public StunStarParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double dx, double dy, double dz) {
        // dx = start angle, dy = angular speed, dz = lifetime
        return new StunStarParticle(level, x, y, z,
                0.5, dx, dy, (int) dz, sprites);
    }
}
