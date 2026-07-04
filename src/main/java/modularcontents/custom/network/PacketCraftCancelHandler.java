package modularcontents.custom.network;

import modularcontents.custom.block.TileEntityListWorkbench;
import modularcontents.custom.recipe.IngredientStack;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.oredict.OreDictionary;

public class PacketCraftCancelHandler implements IMessageHandler<PacketCraftCancel, IMessage> {

    @Override
    public IMessage onMessage(PacketCraftCancel message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        IThreadListener mainThread = (WorldServer) player.world;
        mainThread.addScheduledTask(() -> {
            if (message.pos == null) return;
            if (message.slot < 0 || message.slot >= TileEntityListWorkbench.QUEUE_SIZE) return;

            TileEntity te = player.world.getTileEntity(message.pos);
            if (!(te instanceof TileEntityListWorkbench)) return;
            TileEntityListWorkbench workbench = (TileEntityListWorkbench) te;

            String recipeId = workbench.getQueueRecipeId(message.slot);
            if (recipeId.isEmpty()) return;

            ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(recipeId);
            int remaining = workbench.getQueueCount(message.slot);

            if (recipe != null && remaining > 0) {
                for (IngredientStack ingredient : recipe.inputs) {
                    int needed = ingredient.count * remaining;
                    for (int i = 0; i < workbench.bufferSlots.getSlots() && needed > 0; i++) {
                        ItemStack buffered = workbench.bufferSlots.getStackInSlot(i);
                        if (!buffered.isEmpty() && isItemMatching(ingredient, buffered)) {
                            int take = Math.min(needed, buffered.getCount());
                            ItemStack extracted = workbench.bufferSlots.extractItem(i, take, false);
                            if (!extracted.isEmpty()) {
                                needed -= extracted.getCount();
                                if (!player.inventory.addItemStackToInventory(extracted)) {
                                    InventoryHelper.spawnItemStack(player.world, message.pos.getX(), message.pos.getY() + 1, message.pos.getZ(), extracted);
                                }
                            }
                        }
                    }
                }
            }

            workbench.removeQueued(message.slot);
        });
        return null;
    }

    private boolean isItemMatching(IngredientStack ingredient, ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemStack required = ingredient.toItemStack();
        if (required.isEmpty()) return false;

        if (required.getItem() != stack.getItem()) return false;
        if (ingredient.meta != OreDictionary.WILDCARD_VALUE && ingredient.meta != stack.getMetadata()) return false;

        return true;
    }
}
