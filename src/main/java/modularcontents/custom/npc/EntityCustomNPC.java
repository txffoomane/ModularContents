package modularcontents.custom.npc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityCustomNPC extends EntityMob implements IRangedAttackMob {

    private static final DataParameter<String> NPC_ID = EntityDataManager.createKey(EntityCustomNPC.class, DataSerializers.STRING);

    private CustomNPCInfo npcInfo;
    private List<ItemStack> extraEquipment = new ArrayList<>();

    public EntityCustomNPC(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 1.95F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(NPC_ID, "");
    }

    public void setNpcId(String id) {
        this.dataManager.set(NPC_ID, id);
        this.npcInfo = NPCManager.NPCS.get(id);
        if (this.npcInfo != null) {
            this.setCustomNameTag(this.npcInfo.name);
            applyStats();
            initEntityAI();
        }
    }

    public String getNpcId() {
        return this.dataManager.get(NPC_ID);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (NPC_ID.equals(key) && this.world.isRemote) {
            this.npcInfo = NPCManager.NPCS.get(this.getNpcId());
        }
    }

    private void applyStats() {
        if (this.npcInfo == null) return;
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.npcInfo.maxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.npcInfo.speed);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.npcInfo.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(this.npcInfo.followRange);
        this.setHealth((float) this.npcInfo.maxHealth);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();

        this.tasks.addTask(1, new EntityAISwimming(this));

        if (this.npcInfo != null) {
            if (this.npcInfo.shootRange > 0) {
                // Custom Ranged AI
                this.tasks.addTask(2, new EntityAIAttackRanged(this, 1.0D, 20, (float) this.npcInfo.shootRange));
            } else {
                this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.2D, false));
            }
        } else {
            this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.2D, false));
        }

        this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));

        if (this.npcInfo == null || this.npcInfo.isAggressive) {
            this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        }
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        if (this.npcInfo != null) {
            equipFromInfo();
        }
        return livingdata;
    }

    private void equipFromInfo() {
        extraEquipment.clear();
        for (Map.Entry<String, String> entry : this.npcInfo.equipment.entrySet()) {
            String slot = entry.getKey().toLowerCase();
            ItemStack stack = NPCManager.getItemFromString(entry.getValue());
            if (stack.isEmpty()) continue;

            float dropChance = this.npcInfo.dropChances.getOrDefault(slot, 0.085f);

            switch (slot) {
                case "mainhand": this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack); this.setDropChance(EntityEquipmentSlot.MAINHAND, dropChance); break;
                case "offhand": this.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack); this.setDropChance(EntityEquipmentSlot.OFFHAND, dropChance); break;
                case "head": this.setItemStackToSlot(EntityEquipmentSlot.HEAD, stack); this.setDropChance(EntityEquipmentSlot.HEAD, dropChance); break;
                case "chest": this.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack); this.setDropChance(EntityEquipmentSlot.CHEST, dropChance); break;
                case "legs": this.setItemStackToSlot(EntityEquipmentSlot.LEGS, stack); this.setDropChance(EntityEquipmentSlot.LEGS, dropChance); break;
                case "feet": this.setItemStackToSlot(EntityEquipmentSlot.FEET, stack); this.setDropChance(EntityEquipmentSlot.FEET, dropChance); break;
                default:
                    // MW Vest, Backpack, etc.
                    extraEquipment.add(stack);
                    break;
            }
        }
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        // Drop MW backpacks, vests, etc.
        for (ItemStack stack : extraEquipment) {
            if (this.rand.nextFloat() < 0.1F + (0.05F * lootingModifier)) { // Base 10% chance for extra slots, increased by looting
                this.entityDropItem(stack, 0.0F);
            }
        }
    }

    @Override
    protected ResourceLocation getLootTable() {
        if (this.npcInfo != null && this.npcInfo.lootTable != null && !this.npcInfo.lootTable.isEmpty()) {
            return new ResourceLocation(this.npcInfo.lootTable);
        }
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("NPC_ID", this.getNpcId());

        NBTTagList extraList = new NBTTagList();
        for (ItemStack stack : extraEquipment) {
            extraList.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        compound.setTag("ExtraEquipment", extraList);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("NPC_ID")) {
            this.setNpcId(compound.getString("NPC_ID"));
        }

        if (compound.hasKey("ExtraEquipment")) {
            extraEquipment.clear();
            NBTTagList extraList = compound.getTagList("ExtraEquipment", 10);
            for (int i = 0; i < extraList.tagCount(); i++) {
                extraEquipment.add(new ItemStack(extraList.getCompoundTagAt(i)));
            }
        }
    }

    // IRangedAttackMob implementation
    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        // Here we will spawn fake bullets/projectiles
        EntityNPCBullet bullet = new EntityNPCBullet(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - bullet.posY;
        double d2 = target.posZ - this.posZ;
        double d3 = (double)net.minecraft.util.math.MathHelper.sqrt(d0 * d0 + d2 * d2);
        bullet.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 3.0F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));

        // Play shooting sound
        this.playSound(net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE, 0.5F, 2.0F); // Placeholder sound, MW sounds can be tricky without dependencies

        this.world.spawnEntity(bullet);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
    }
}