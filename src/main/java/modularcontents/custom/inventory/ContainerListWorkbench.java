package modularcontents.custom.inventory;

import modularcontents.custom.block.TileEntityListWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerListWorkbench extends Container {

    public final TileEntityListWorkbench te;
    private int lastProgress;
    private int lastTotalTime;
    private int lastQueuedCrafts;

    public ContainerListWorkbench(InventoryPlayer playerInventory, TileEntityListWorkbench te) {
        this.te = te;

        // 3 Output Slots - positioned far right
        int outX = 196;
        int outY = 108;
        for (int i = 0; i < 3; i++) {
            this.addSlotToContainer(new SlotItemHandler(te.outputSlots, i, outX + i * 18, outY) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false; // Can only take items out
                }
            });
        }

        // Player inventory
        int invX = 48;
        int invY = 157;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, invX + j * 18, invY + i * 18));
            }
        }

        // Player hotbar
        int hotbarY = 215;
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, invX + k * 18, hotbarY));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(te.getPos().getX() + 0.5D, te.getPos().getY() + 0.5D, te.getPos().getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            if (this.lastProgress != te.getProgress()) {
                listener.sendWindowProperty(this, 0, te.getProgress());
            }
            if (this.lastTotalTime != te.getTotalTime()) {
                listener.sendWindowProperty(this, 1, te.getTotalTime());
            }
            if (this.lastQueuedCrafts != te.getQueuedCrafts()) {
                listener.sendWindowProperty(this, 2, te.getQueuedCrafts());
            }
        }
        this.lastProgress = te.getProgress();
        this.lastTotalTime = te.getTotalTime();
        this.lastQueuedCrafts = te.getQueuedCrafts();
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            this.clientProgress = data;
        } else if (id == 1) {
            this.clientTotalTime = data;
        } else if (id == 2) {
            this.clientQueuedCrafts = data;
        }
    }

    public int clientProgress = 0;
    public int clientTotalTime = 0;
    public int clientQueuedCrafts = 0;

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index >= 0 && index < 3) { // 3 Output slots
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }
        return itemstack;
    }
}
