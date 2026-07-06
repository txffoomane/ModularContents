package modularcontents.custom.npc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityNPCBullet extends EntityThrowable {

    private double damage = 4.0; // Default damage

    public EntityNPCBullet(World worldIn) {
        super(worldIn);
    }

    public EntityNPCBullet(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
        this.setNoGravity(true); // Bullets fly straight
    }

    public EntityNPCBullet(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    @Override
    protected float getGravityVelocity() {
        return 0.0F; // No gravity drop
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Despawn after flying for too long (e.g. 100 ticks = 5 seconds)
        if (this.ticksExisted > 100) {
            this.setDead();
        }

        // Add a small trail particle so players can see they are being shot at
        if (this.world.isRemote) {
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote) {
            if (result.entityHit != null && result.entityHit != this.thrower) {
                // Apply damage
                result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.thrower), (float) this.damage);
            }
            this.setDead(); // Destroy bullet on impact with anything (entity or block)
        }
    }
}