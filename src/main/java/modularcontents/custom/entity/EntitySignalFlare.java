package modularcontents.custom.entity;

import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;

public class EntitySignalFlare extends EntityThrowable {
    public static final float SPIN_DEGREES_PER_TICK = 20.0F;

    private static final DataParameter<Boolean> LANDED = EntityDataManager.createKey(EntitySignalFlare.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> LAND_SPIN = EntityDataManager.createKey(EntitySignalFlare.class, DataSerializers.FLOAT);

    private int fuse = 0;
    private String callerName = "";
    private String customLootTable = "";

    public EntitySignalFlare(World worldIn) {
        super(worldIn);
    }

    public EntitySignalFlare(World worldIn, EntityPlayer throwerIn) {
        super(worldIn, throwerIn);
        this.callerName = throwerIn.getName();
        // (от 10 до 50 секунд)
        this.fuse = setTime(10) + worldIn.rand.nextInt(800);
        this.setSize(0.4F, 0.4F);
    }

    public int setTime(int second) {
        return second * 20;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LANDED, Boolean.FALSE);
        this.dataManager.register(LAND_SPIN, 0.0F);
    }

    public boolean isLanded() {
        return this.dataManager.get(LANDED);
    }

    public float getLandSpin() {
        return this.dataManager.get(LAND_SPIN);
    }

    private void land(double lx, double ly, double lz) {
        this.setPosition(lx, ly, lz);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        if (!this.world.isRemote) {
            this.dataManager.set(LAND_SPIN, this.ticksExisted * SPIN_DEGREES_PER_TICK);
            this.dataManager.set(LANDED, Boolean.TRUE);
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Check Entity Data for custom overrides immediately after spawning
        if (this.ticksExisted == 1 && !this.world.isRemote) {
            NBTTagCompound data = this.getEntityData();
            if (data.hasKey("Fuse")) {
                this.fuse = data.getInteger("Fuse");
            }
            if (data.hasKey("LootTable")) {
                this.customLootTable = data.getString("LootTable");
            }
        }

        if (this.isLanded()) {
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
        }

        if (!this.isLanded() && !this.world.isRemote) {
            double probe = 0.35D + Math.max(0.0D, -this.motionY);
            Vec3d start = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d end = new Vec3d(this.posX, this.posY - probe, this.posZ);
            RayTraceResult ground = this.world.rayTraceBlocks(start, end, false, true, false);
            if (ground != null && ground.typeOfHit == RayTraceResult.Type.BLOCK) {
                this.land(ground.hitVec.x, ground.hitVec.y, ground.hitVec.z);
            }
        }

        if (this.world.isRemote) {
            // Emitting red/orange smoke particles
            this.world.spawnParticle(EnumParticleTypes.REDSTONE, this.posX, this.posY + 0.5D, this.posZ, 1.0D, 0.0D, 0.0D); // Red
            this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.1D, 0.0D);
        }

        if (!this.world.isRemote) {
            this.fuse--;
            if (this.fuse <= 0) {
                // Spawn airdrop high above this flare
                EntityAirdrop airdrop = new EntityAirdrop(this.world, this.posX, 250.0D, this.posZ);
                // No delay for airdrop falling since the flare was the delay
                airdrop.setDelayAndCaller(0, this.callerName, true, this.posX, this.posZ);

                if (this.customLootTable != null && !this.customLootTable.isEmpty()) {
                    airdrop.setLootTable(this.customLootTable);
                }

                this.world.spawnEntity(airdrop);

                this.setDead();
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.isLanded()) {
            double lx = this.posX;
            double ly = this.posY;
            double lz = this.posZ;
            if (result.hitVec != null) {
                lx = result.hitVec.x;
                ly = result.hitVec.y;
                lz = result.hitVec.z;
            }
            this.land(lx, ly, lz);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.fuse = compound.getInteger("Fuse");
        this.callerName = compound.getString("Caller");
        this.dataManager.set(LANDED, compound.getBoolean("Landed"));
        this.dataManager.set(LAND_SPIN, compound.getFloat("LandSpin"));
        if (compound.hasKey("LootTable")) {
            this.customLootTable = compound.getString("LootTable");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Fuse", this.fuse);
        if (this.callerName != null) {
            compound.setString("Caller", this.callerName);
        }
        compound.setBoolean("Landed", this.isLanded());
        compound.setFloat("LandSpin", this.getLandSpin());
        if (this.customLootTable != null && !this.customLootTable.isEmpty()) {
            compound.setString("LootTable", this.customLootTable);
        }
    }
}