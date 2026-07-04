package modularcontents.custom.entity;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.loot.AirdropLootManager;
import modularcontents.custom.block.TileEntityAirdrop;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class EntityAirdrop extends Entity {
    private String lootTableName = "";
    private int delayTimer = 0;
    private String callerName = "";
    private boolean isFlare = false;
    private double originalTargetX = 0;
    private double originalTargetZ = 0;
    private boolean isRedSmoke;

    public EntityAirdrop(World worldIn) {
        super(worldIn);
        this.setSize(0.98F, 0.98F);
        this.preventEntitySpawning = true;
        this.isRedSmoke = worldIn.rand.nextBoolean();
    }

    public EntityAirdrop(World worldIn, double x, double y, double z) {
        this(worldIn);
        this.setPosition(x, y, z);
        this.motionX = 0.0D;
        this.motionY = -0.2D; // Falling speed
        this.motionZ = 0.0D;
        this.prevPosX = x;
        this.prevPosY = y;
        this.prevPosZ = z;
        this.lootTableName = AirdropLootManager.getRandomLootTable();
    }

    public void setLootTable(String name) {
        this.lootTableName = name;
    }

    public void setDelayAndCaller(int delay, String caller, boolean flare, double tX, double tZ) {
        this.delayTimer = delay;
        this.callerName = caller;
        this.isFlare = flare;
        this.originalTargetX = tX;
        this.originalTargetZ = tZ;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead && this.delayTimer <= 0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.delayTimer > 0) {
            this.delayTimer--;

            // Plane sound right as it appears (delay ends soon or starts)
            if (this.delayTimer == 10 && !this.world.isRemote) {
                 this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_FLAP, net.minecraft.util.SoundCategory.AMBIENT, 5.0F, 0.5F);
                 this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_MINECART_RIDING, net.minecraft.util.SoundCategory.AMBIENT, 3.0F, 0.2F);
            }

            this.motionY = 0;
            this.motionX = 0;
            this.motionZ = 0;
            return;
        }

        if (this.ticksExisted == 1 && !this.world.isRemote) {
             this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_ENDERDRAGON_FLAP, net.minecraft.util.SoundCategory.AMBIENT, 5.0F, 0.5F);
             this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_MINECART_RIDING, net.minecraft.util.SoundCategory.AMBIENT, 3.0F, 0.2F);
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionY -= 0.04D; // Gravity
        // Terminal velocity for parachute effect
        if (this.motionY < -0.3D) {
            this.motionY = -0.3D;
        }

        this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        // Smoke particles
        if (this.world.isRemote) {
            float r = this.isRedSmoke ? 1.0F : 0.0F;
            float g = (r == 0.0F) ? 1.0F : 0.0F;
            float b = 0.0F;
            try {
                Class<?> clazz = Class.forName("modularcontents.custom.client.ClientProxyUtils");
                java.lang.reflect.Method method = clazz.getMethod("spawnAirdropSmoke", net.minecraft.world.World.class, double.class, double.class, double.class, float.class, float.class, float.class);
                method.invoke(null, this.world, this.posX, this.posY + 1.5D, this.posZ, r, g, b);
            } catch (Exception e) {}
        }

        if (!this.world.isRemote) {
            BlockPos pos = new BlockPos(this);
            IBlockState state = this.world.getBlockState(pos.down());

            if (this.onGround || !state.getBlock().isReplaceable(this.world, pos.down())) {
                // Landed!
                this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND, net.minecraft.util.SoundCategory.BLOCKS, 2.0F, 0.7F);
                this.world.playSound(null, this.posX, this.posY, this.posZ, net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.2F);

                this.world.setBlockState(pos, ModularcontentsMod.airdrop.getDefaultState());
                TileEntity te = this.world.getTileEntity(pos);
                if (te instanceof TileEntityAirdrop) {
                    ((TileEntityAirdrop) te).setLootTableName(this.lootTableName);
                    ((TileEntityAirdrop) te).setRedSmoke(this.isRedSmoke);
                }

                // Notify player
                if (this.callerName != null && !this.callerName.isEmpty()) {
                    EntityPlayer player = this.world.getPlayerEntityByName(this.callerName);
                    if (player != null) {
                        if (this.isFlare) {
                            double dx = this.posX - player.posX;
                            double dz = this.posZ - player.posZ;
                            int distance = (int) Math.sqrt(dx * dx + dz * dz);
                            String dir = getDirection(dx, dz);
                            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Airdrop landed! Approx. " + distance + " blocks to the " + dir + "."));
                        } else {
                            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Radio Airdrop landed at exactly X: " + (int)this.posX + ", Z: " + (int)this.posZ));
                        }
                    }
                }

                this.setDead();
            } else if (this.posY < 0) {
                this.setDead();
            }
        }
    }

    private String getDirection(double dx, double dz) {
        double angle = Math.atan2(dz, dx) * 180 / Math.PI;
        if (angle > -22.5 && angle <= 22.5) return "East";
        else if (angle > 22.5 && angle <= 67.5) return "South-East";
        else if (angle > 67.5 && angle <= 112.5) return "South";
        else if (angle > 112.5 && angle <= 157.5) return "South-West";
        else if (angle > 157.5 || angle <= -157.5) return "West";
        else if (angle > -157.5 && angle <= -112.5) return "North-West";
        else if (angle > -112.5 && angle <= -67.5) return "North";
        else if (angle > -67.5 && angle <= -22.5) return "North-East";
        return "Unknown";
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("LootTable")) {
            this.lootTableName = compound.getString("LootTable");
        }
        this.delayTimer = compound.getInteger("Delay");
        this.callerName = compound.getString("Caller");
        this.isFlare = compound.getBoolean("IsFlare");
        this.originalTargetX = compound.getDouble("OriginalX");
        this.originalTargetZ = compound.getDouble("OriginalZ");
        if (compound.hasKey("IsRedSmoke")) {
            this.isRedSmoke = compound.getBoolean("IsRedSmoke");
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (this.lootTableName != null && !this.lootTableName.isEmpty()) {
            compound.setString("LootTable", this.lootTableName);
        }
        compound.setInteger("Delay", this.delayTimer);
        if (this.callerName != null) {
            compound.setString("Caller", this.callerName);
        }
        compound.setBoolean("IsFlare", this.isFlare);
        compound.setDouble("OriginalX", this.originalTargetX);
        compound.setDouble("OriginalZ", this.originalTargetZ);
        compound.setBoolean("IsRedSmoke", this.isRedSmoke);
    }
}