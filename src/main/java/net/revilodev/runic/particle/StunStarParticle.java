package net.revilodev.runic.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class StunStarParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final double radius;
    private final double angularSpeed;
    private final double startAngle;
    private final double originX;
    private final double originY;
    private final double originZ;

    public StunStarParticle(ClientLevel level, double x, double y, double z,
                            double radius, double startAngle, double angularSpeed,
                            int lifetime, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.radius = radius;
        this.startAngle = startAngle;
        this.angularSpeed = angularSpeed;
        this.lifetime = lifetime <= 0 ? 40 : lifetime;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.setSpriteFromAge(sprites);
        this.gravity = 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        double angle = startAngle + angularSpeed * this.age;
        double offsetX = radius * Math.cos(angle);
        double offsetZ = radius * Math.sin(angle);
        this.setPos(originX + offsetX, originY, originZ + offsetZ);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return (type, level, x, y, z, dx, dy, dz) ->
                new StunStarParticle(level, x, y, z, 0.5, dx, dy, (int) dz, sprites);
    }
}
