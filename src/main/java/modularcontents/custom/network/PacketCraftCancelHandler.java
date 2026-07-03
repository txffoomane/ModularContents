package modularcontents.custom.network;

import modularcontents.custom.block.TileEntityListWorkbench;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketCraftCancelHandler implements IMessageHandler<PacketCraftCancel, IMessage> {

    @Override
    public IMessage onMessage(PacketCraftCancel message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        IThreadListener mainThread = (WorldServer) player.world;
        mainThread.addScheduledTask(() -> {
            if (message.pos == null) return;

            TileEntity te = player.world.getTileEntity(message.pos);
            if (!(te instanceof TileEntityListWorkbench)) return;
            TileEntityListWorkbench workbench = (TileEntityListWorkbench) te;

            if (workbench.isCrafting()) {
                // Refund items
                for (int i = 0; i < workbench.bufferSlots.getSlots(); i++) {
                    ItemStack buffered = workbench.bufferSlots.getStackInSlot(i);
                    if (!buffered.isEmpty()) {
                        if (!player.inventory.addItemStackToInventory(buffered.copy())) {
                            // If inventory full, drop at block
                            InventoryHelper.spawnItemStack(player.world, message.pos.getX(), message.pos.getY() + 1, message.pos.getZ(), buffered.copy());
                        }
                        workbench.bufferSlots.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
                workbench.resetCrafting();
            }
        });
        return null;
    }
}
