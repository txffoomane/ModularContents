package modularcontents.custom.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class ParticleAirdropSmoke extends Particle {

    private float smokeParticleScale;

    public ParticleAirdropSmoke(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float r, float g, float b) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += xSpeedIn;
        this.motionY += ySpeedIn;
        this.motionZ += zSpeedIn;
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        this.particleScale *= 0.75F;
        this.particleScale *= 2.5F; // 2.5 times larger
        this.smokeParticleScale = this.particleScale;
        this.particleMaxAge = (int)(100.0D / (Math.random() * 0.8D + 0.2D));
        this.particleMaxAge = (int)(this.particleMaxAge * 1.5F);
        this.canCollide = false;
        this.setParticleTextureIndex((int)(Math.random() * 8.0D));
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.motionY += 0.004D;

        // Simulating wind
        this.motionX += (Math.random() - 0.5D) * 0.005D;
        this.motionZ += (Math.random() - 0.5D) * 0.005D;

        // Add random strong gust occasionally
        if (this.rand.nextInt(40) == 0) {
             this.motionX += (this.rand.nextFloat() - 0.5F) * 0.05F;
             this.motionZ += (this.rand.nextFloat() - 0.5F) * 0.05F;
        }

        this.move(this.motionX, this.motionY, this.motionZ);

        if (this.posY == this.prevPosY) {
            this.motionX *= 1.1D;
            this.motionZ *= 1.1D;
        }

        this.motionX *= 0.9599999785423279D;
        this.motionY *= 0.9599999785423279D;
        this.motionZ *= 0.9599999785423279D;

        // Expand as it goes up
        this.particleScale = this.smokeParticleScale + (float)this.particleAge / (float)this.particleMaxAge * 3.0F;
    }
}