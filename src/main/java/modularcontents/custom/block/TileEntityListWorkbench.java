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
import java.util.Arrays;
import java.util.List;

public class TileEntityListWorkbench extends TileEntity implements ITickable {

    public static final int QUEUE_SIZE = 3;

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

    private final String[] queueRecipes = new String[QUEUE_SIZE];
    private final int[] queueCounts = new int[QUEUE_SIZE];
    private int progress = 0;
    private int totalTime = 0;

    public TileEntityListWorkbench() {
        Arrays.fill(queueRecipes, "");
    }

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

                    net.minecraft.util.math.BlockPos partPos = ((BlockListWorkbench) state.getBlock()).getPartPos(state, pos);
                    IBlockState partState = world.getBlockState(partPos);
                    if (partState.getBlock() instanceof BlockListWorkbenchPart) {
                        world.setBlockState(partPos, partState.withProperty(BlockListWorkbenchPart.CRAFTING, isCraftingNow), 3);
                    }
                }
            }
        }
    }

    private boolean isItemMatching(IngredientStack ingredient, ItemStack stack) {
        return ingredient.matches(stack);
    }

    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    private void finishOneCraft() {
        String id = queueRecipes[0];
        boolean moreAfter = getQueuedCrafts() > 1;
        if (!id.isEmpty()) {
            this.world.playSound(null, this.pos, net.minecraft.init.SoundEvents.BLOCK_ANVIL_USE, net.minecraft.util.SoundCategory.BLOCKS, 0.4F, this.world.rand.nextFloat() * 0.2F + 0.9F);

            ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(id);
            if (recipe != null) {
                for (IngredientStack ingredient : recipe.inputs) {
                    int toRemove = ingredient.count;
                    for (int i = 0; i < bufferSlots.getSlots(); i++) {
                        ItemStack buffered = bufferSlots.getStackInSlot(i);
                        if (!buffered.isEmpty() && isItemMatching(ingredient, buffered)) {
                            int taken = Math.min(buffered.getCount(), toRemove);

                            if (buffered.isItemStackDamageable()) {
                                ItemStack tool = bufferSlots.extractItem(i, 1, false);
                                tool.setItemDamage(tool.getItemDamage() + 1);
                                if (tool.getItemDamage() <= tool.getMaxDamage()) {
                                    ItemStack leftoverTool = ItemHandlerHelper.insertItem(moreAfter ? bufferSlots : outputSlots, tool, false);
                                    if (!leftoverTool.isEmpty()) {
                                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftoverTool);
                                    }
                                }
                                toRemove -= 1;
                            } else {
                                bufferSlots.extractItem(i, taken, false);
                                toRemove -= taken;
                            }

                            if (toRemove <= 0) break;
                        }
                    }
                }

                List<ItemStack> results = recipe.rollResults(world.rand);
                for (ItemStack result : results) {
                    if (result.isEmpty()) continue;
                    ItemStack leftover = ItemHandlerHelper.insertItem(outputSlots, result.copy(), false);
                    if (!leftover.isEmpty()) {
                        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftover);
                    }
                }
            }
        }

        queueCounts[0]--;
        progress = 0;

        if (queueCounts[0] <= 0) {
            shiftQueue();
        }

        if (!isCrafting()) {
            dumpBuffer();
            resetCrafting();
        } else {
            sync();
        }
    }

    private void shiftQueue() {
        for (int i = 0; i < QUEUE_SIZE - 1; i++) {
            queueRecipes[i] = queueRecipes[i + 1];
            queueCounts[i] = queueCounts[i + 1];
        }
        queueRecipes[QUEUE_SIZE - 1] = "";
        queueCounts[QUEUE_SIZE - 1] = 0;
        activateFront();
    }

    private void activateFront() {
        progress = 0;
        totalTime = 0;
        while (!queueRecipes[0].isEmpty()) {
            ListWorkbenchRecipe recipe = queueCounts[0] > 0 ? ListWorkbenchRecipeManager.getRecipe(queueRecipes[0]) : null;
            if (recipe != null) {
                totalTime = recipe.craftingTime;
                return;
            }
            for (int i = 0; i < QUEUE_SIZE - 1; i++) {
                queueRecipes[i] = queueRecipes[i + 1];
                queueCounts[i] = queueCounts[i + 1];
            }
            queueRecipes[QUEUE_SIZE - 1] = "";
            queueCounts[QUEUE_SIZE - 1] = 0;
        }
    }

    public boolean enqueueCraft(String recipeId, int craftTime, int amount) {
        for (int i = 0; i < QUEUE_SIZE; i++) {
            if (queueRecipes[i].isEmpty()) {
                queueRecipes[i] = recipeId;
                queueCounts[i] = amount;
                if (i == 0) {
                    progress = 0;
                    totalTime = craftTime;
                }
                updateCraftingState(true);
                sync();
                return true;
            }
        }
        return false;
    }

    public void removeQueued(int index) {
        if (index < 0 || index >= QUEUE_SIZE || queueRecipes[index].isEmpty()) return;
        for (int i = index; i < QUEUE_SIZE - 1; i++) {
            queueRecipes[i] = queueRecipes[i + 1];
            queueCounts[i] = queueCounts[i + 1];
        }
        queueRecipes[QUEUE_SIZE - 1] = "";
        queueCounts[QUEUE_SIZE - 1] = 0;
        if (index == 0) activateFront();
        if (!isCrafting()) {
            dumpBuffer();
            resetCrafting();
        } else {
            sync();
        }
    }

    private void dumpBuffer() {
        for (int i = 0; i < bufferSlots.getSlots(); i++) {
            ItemStack leftover = bufferSlots.getStackInSlot(i);
            if (!leftover.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY() + 1, pos.getZ(), leftover);
                bufferSlots.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public void resetCrafting() {
        Arrays.fill(queueRecipes, "");
        Arrays.fill(queueCounts, 0);
        progress = 0;
        totalTime = 0;
        updateCraftingState(false);
        sync();
    }

    private void sync() {
        if (world != null) {
            markDirty();
            if (!world.isRemote) {
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }
    }

    public boolean isCrafting() {
        return !queueRecipes[0].isEmpty() && queueCounts[0] > 0;
    }

    public boolean hasFreeQueueSlot() {
        for (int i = 0; i < QUEUE_SIZE; i++) {
            if (queueRecipes[i].isEmpty()) return true;
        }
        return false;
    }

    public String getQueueRecipeId(int index) {
        return index >= 0 && index < QUEUE_SIZE ? queueRecipes[index] : "";
    }

    public int getQueueCount(int index) {
        return index >= 0 && index < QUEUE_SIZE ? queueCounts[index] : 0;
    }

    public int getProgress() {
        return progress;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getQueuedCrafts() {
        int sum = 0;
        for (int i = 0; i < QUEUE_SIZE; i++) {
            if (!queueRecipes[i].isEmpty()) sum += queueCounts[i];
        }
        return sum;
    }

    public String getActiveRecipeId() {
        return queueRecipes[0];
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

        Arrays.fill(queueRecipes, "");
        Arrays.fill(queueCounts, 0);
        if (compound.hasKey("QueueId0")) {
            for (int i = 0; i < QUEUE_SIZE; i++) {
                queueRecipes[i] = compound.getString("QueueId" + i);
                queueCounts[i] = compound.getInteger("QueueCount" + i);
            }
        } else if (compound.hasKey("ActiveRecipe")) {
            queueRecipes[0] = compound.getString("ActiveRecipe");
            queueCounts[0] = compound.getInteger("QueuedCrafts");
        }

        this.progress = compound.getInteger("Progress");
        this.totalTime = compound.getInteger("TotalTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("OutputSlots", outputSlots.serializeNBT());
        compound.setTag("BufferSlots", bufferSlots.serializeNBT());
        for (int i = 0; i < QUEUE_SIZE; i++) {
            compound.setString("QueueId" + i, queueRecipes[i] != null ? queueRecipes[i] : "");
            compound.setInteger("QueueCount" + i, queueCounts[i]);
        }
        compound.setInteger("Progress", progress);
        compound.setInteger("TotalTime", totalTime);
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
