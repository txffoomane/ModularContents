package modularcontents.custom.block;

import modularcontents.custom.recipe.IngredientStack;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityListWorkbench extends TileEntity implements ITickable {

    public final ItemStackHandler outputSlots = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    public final ItemStackHandler bufferSlots = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private String activeRecipeId = "";
    private int progress = 0;
    private int totalTime = 0;
    private int queuedCrafts = 0;

    @Override
    public void update() {
        if (!world.isRemote) {
            if (isCrafting()) {
                progress++;
                if (progress >= totalTime) {
                    finishOneCraft();
                }
                markDirty();
            }
        }
    }

    private void updateCraftingState(boolean isCraftingNow) {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockListWorkbench) {
                boolean wasCrafting = state.getValue(BlockListWorkbench.CRAFTING);
                if (wasCrafting != isCraftingNow) {
                    world.setBlockState(pos, state.withProperty(BlockListWorkbench.CRAFTING, isCraftingNow), 3);
                }
            }
        }
    }

    private boolean isItemMatching(IngredientStack ingredient, ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemStack required = ingredient.toItemStack();
        if (required.isEmpty()) return false;

        if (required.getItem() != stack.getItem()) return false;
        if (ingredient.meta != net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE && ingredient.meta != stack.getMetadata()) return false;

        return true;
    }

    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    private void finishOneCraft() {
        if (activeRecipeId != null && !activeRecipeId.isEmpty()) {
            ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(activeRecipeId);
            if (recipe != null) {
                for (IngredientStack ingredient : recipe.inputs) {
                    int toRemove = ingredient.count;
                    for (int i = 0; i < bufferSlots.getSlots(); i++) {
                        ItemStack buffered = bufferSlots.getStackInSlot(i);
                        if (!buffered.isEmpty() && isItemMatching(ingredient, buffered)) {
                            int taken = Math.min(buffered.getCount(), toRemove);

                            // Handle tool durability
                            if (buffered.isItemStackDamageable()) {
                                ItemStack tool = bufferSlots.extractItem(i, 1, false);
                                tool.setItemDamage(tool.getItemDamage() + 1);
                                if (tool.getItemDamage() <= tool.getMaxDamage()) {
                                    if (queuedCrafts > 1) {
                                        ItemStack leftoverTool = ItemHandlerHelper.insertItem(bufferSlots, tool, false);
                                        if (!leftoverTool.isEmpty()) {
                                            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftoverTool);
                                        }
                                    } else {
                                        ItemStack leftoverTool = ItemHandlerHelper.insertItem(outputSlots, tool, false);
                                        if (!leftoverTool.isEmpty()) {
                                            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftoverTool);
                                        }
                                    }
                                }
                                toRemove -= 1; // Only 1 tool is consumed per recipe input logic usually
                            } else {
                                bufferSlots.extractItem(i, taken, false);
                                toRemove -= taken;
                            }

                            if (toRemove <= 0) break;
                        }
                    }
                }

                List<ItemStack> results = recipe.getResults();
                for (ItemStack result : results) {
                    if (result.isEmpty()) continue;
                    ItemStack leftover = ItemHandlerHelper.insertItem(outputSlots, result.copy(), false);
                    if (!leftover.isEmpty()) {
                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftover);
                    }
                }
            }
        }

        queuedCrafts--;
        progress = 0;

        if (queuedCrafts <= 0) {
            for (int i = 0; i < bufferSlots.getSlots(); i++) {
                ItemStack leftover = bufferSlots.getStackInSlot(i);
                if (!leftover.isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftover);
                    bufferSlots.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            resetCrafting();
        } else {
            markDirty();
        }
    }

    public void startCrafting(String recipeId, int time, int amount) {
        this.activeRecipeId = recipeId;
        this.totalTime = time;
        this.queuedCrafts = amount;
        this.progress = 0;
        updateCraftingState(true);
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
        markDirty();
    }

    public void resetCrafting() {
        this.activeRecipeId = "";
        this.progress = 0;
        this.totalTime = 0;
        this.queuedCrafts = 0;
        updateCraftingState(false);
        markDirty();
    }

    public boolean isCrafting() {
        return activeRecipeId != null && !activeRecipeId.isEmpty() && queuedCrafts > 0;
    }

    public int getProgress() {
        return progress;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getQueuedCrafts() {
        return queuedCrafts;
    }

    public String getActiveRecipeId() {
        return activeRecipeId;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(outputSlots);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("OutputSlots")) {
            outputSlots.deserializeNBT((NBTTagCompound) compound.getTag("OutputSlots"));
        }
        if (compound.hasKey("BufferSlots")) {
            bufferSlots.deserializeNBT((NBTTagCompound) compound.getTag("BufferSlots"));
        }
        this.activeRecipeId = compound.getString("ActiveRecipe");
        this.progress = compound.getInteger("Progress");
        this.totalTime = compound.getInteger("TotalTime");
        this.queuedCrafts = compound.getInteger("QueuedCrafts");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("OutputSlots", outputSlots.serializeNBT());
        compound.setTag("BufferSlots", bufferSlots.serializeNBT());
        compound.setString("ActiveRecipe", activeRecipeId != null ? activeRecipeId : "");
        compound.setInteger("Progress", progress);
        compound.setInteger("TotalTime", totalTime);
        compound.setInteger("QueuedCrafts", queuedCrafts);
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
