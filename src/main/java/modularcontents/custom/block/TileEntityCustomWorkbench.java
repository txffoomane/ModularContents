package modularcontents.custom.block;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityCustomWorkbench extends TileEntityListWorkbench {
    private String workbenchId = "list_workbench";

    public void setWorkbenchId(String workbenchId) {
        this.workbenchId = workbenchId;
    }

    public String getWorkbenchId() {
        return workbenchId;
    }

    @Override
    protected void updateCraftingState(boolean isCraftingNow) {
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockCustomWorkbench) {
                boolean wasCrafting = state.getValue(BlockCustomWorkbench.CRAFTING);
                if (wasCrafting != isCraftingNow) {
                    world.setBlockState(pos, state.withProperty(BlockCustomWorkbench.CRAFTING, isCraftingNow), 3);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("WorkbenchId")) {
            this.workbenchId = compound.getString("WorkbenchId");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setString("WorkbenchId", this.workbenchId);
        return compound;
    }
}
