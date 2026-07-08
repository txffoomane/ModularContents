package modularcontents.custom.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import modularcontents.ModularcontentsMod;
import modularcontents.custom.inventory.ContainerAirdrop;
import modularcontents.custom.loot.AirdropLootManager;
import java.util.List;
import java.util.Random;

public class TileEntityAirdrop extends TileEntityLockableLoot implements ITickable {
    private NonNullList<ItemStack> airdropContents = NonNullList.withSize(27, ItemStack.EMPTY);
    private String customLootTableName = "";
    private boolean isCustomLootGenerated = false;
    private boolean isRedSmoke = true;
    private boolean smokeSoundStarted = false;

    public void setLootTableName(String name) {
        this.customLootTableName = name;
    }

    public boolean isRedSmoke() {
        return this.isRedSmoke;
    }

    public void setRedSmoke(boolean isRed) {
        this.isRedSmoke = isRed;
        this.markDirty();
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void update() {
        if (this.world.isRemote && !this.smokeSoundStarted) {
            this.smokeSoundStarted = true;
            ModularcontentsMod.proxy.playAirdropSmokeSound(this);
        }

        if (this.world.isRemote && this.world.getTotalWorldTime() % 2 == 0) {
            float r = this.isRedSmoke ? 1.0F : 0.0F;
            float g = this.isRedSmoke ? 0.0F : 1.0F;
            float b = 0.0F;
            double d0 = (double)this.pos.getX() + 0.5D + (this.world.rand.nextDouble() - 0.5D) * 0.2D;
            double d1 = (double)this.pos.getY() + 1.0D + (this.world.rand.nextDouble() - 0.5D) * 0.2D;
            double d2 = (double)this.pos.getZ() + 0.5D + (this.world.rand.nextDouble() - 0.5D) * 0.2D;

            ModularcontentsMod.proxy.spawnAirdropSmoke(this.world, d0, d1, d2, r, g, b);
        }
    }

    @Override
    public int getSizeInventory() { return 27; }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.airdropContents) {
            if (!itemstack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.airdrop";
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.airdropContents;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.airdropContents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.airdropContents);
        }
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
        if (compound.hasKey("CustomLootTable")) {
            this.customLootTableName = compound.getString("CustomLootTable");
        }
        if (compound.hasKey("CustomLootGenerated")) {
            this.isCustomLootGenerated = compound.getBoolean("CustomLootGenerated");
        }
        if (compound.hasKey("IsRedSmoke")) {
            this.isRedSmoke = compound.getBoolean("IsRedSmoke");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.airdropContents);
        }
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        if (this.customLootTableName != null && !this.customLootTableName.isEmpty()) {
            compound.setString("CustomLootTable", this.customLootTableName);
        }
        compound.setBoolean("CustomLootGenerated", this.isCustomLootGenerated);
        compound.setBoolean("IsRedSmoke", this.isRedSmoke);
        return compound;
    }

    @Override
    public void fillWithLoot(EntityPlayer player) {
        // Also call super in case it's using vanilla loot tables
        super.fillWithLoot(player);

        if (!this.world.isRemote && !this.isCustomLootGenerated && this.customLootTableName != null && !this.customLootTableName.isEmpty()) {
            List<ItemStack> generated = AirdropLootManager.generateLoot(this.customLootTableName);
            Random random = this.world.rand;

            // Randomly scatter items into the inventory slots
            for (ItemStack stack : generated) {
                int attempts = 0;
                while (attempts < 50) {
                    int slot = random.nextInt(this.getSizeInventory());
                    if (this.airdropContents.get(slot).isEmpty()) {
                        this.airdropContents.set(slot, stack);
                        break;
                    }
                    attempts++;
                }
            }
            this.isCustomLootGenerated = true;
            this.markDirty();
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        this.fillWithLoot(playerIn);
        return new ContainerAirdrop(playerInventory, this);
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (!this.world.isRemote && this.isEmpty()) {
            this.world.setBlockToAir(this.pos);
        }
    }

    @Override
    public String getGuiID() {
        return "modularcontents:airdrop";
    }
}