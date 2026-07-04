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
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public class PacketCraftStartHandler implements IMessageHandler<PacketCraftStart, IMessage> {

    @Override
    public IMessage onMessage(PacketCraftStart message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        IThreadListener mainThread = (WorldServer) player.world;
        mainThread.addScheduledTask(() -> {
            if (message.recipeId == null || message.recipeId.isEmpty() || message.pos == null) return;
            if (message.amount <= 0) return;

            TileEntity te = player.world.getTileEntity(message.pos);
            if (!(te instanceof TileEntityListWorkbench)) return;
            TileEntityListWorkbench workbench = (TileEntityListWorkbench) te;

            if (!workbench.hasFreeQueueSlot()) return;

            ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe(message.recipeId);
            if (recipe == null) return;

            if (hasIngredients(player, recipe, message.amount)) {
                consumeAndBufferIngredients(player, recipe, workbench, message.amount);
                workbench.enqueueCraft(recipe.id, recipe.craftingTime, message.amount);
            }
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

    private boolean hasIngredients(EntityPlayerMP player, ListWorkbenchRecipe recipe, int amount) {
        // Create a copy of the player's inventory sizes to simulate extraction
        int[] simulatedInventory = new int[player.inventory.mainInventory.size()];
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            simulatedInventory[i] = player.inventory.mainInventory.get(i).getCount();
        }

        for (IngredientStack ingredient : recipe.inputs) {
            int needed = ingredient.count * amount;
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                if (needed <= 0) break;
                ItemStack stack = player.inventory.mainInventory.get(i);
                if (simulatedInventory[i] > 0 && isItemMatching(ingredient, stack)) {
                    int canTake = Math.min(simulatedInventory[i], needed);
                    simulatedInventory[i] -= canTake;
                    needed -= canTake;
                }
            }
            if (needed > 0) return false;
        }
        return true;
    }

    private void consumeAndBufferIngredients(EntityPlayerMP player, ListWorkbenchRecipe recipe, TileEntityListWorkbench workbench, int amount) {
        for (IngredientStack ingredient : recipe.inputs) {
            int needed = ingredient.count * amount;
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                if (needed <= 0) break;
                ItemStack stack = player.inventory.mainInventory.get(i);

                if (!stack.isEmpty() && isItemMatching(ingredient, stack)) {
                    int amountToTake = Math.min(stack.getCount(), needed);
                    ItemStack taken = stack.splitStack(amountToTake);

                    ItemStack leftover = ItemHandlerHelper.insertItem(workbench.bufferSlots, taken.copy(), false);
                    if (!leftover.isEmpty()) {
                        InventoryHelper.spawnItemStack(player.world, workbench.getPos().getX(), workbench.getPos().getY() + 1, workbench.getPos().getZ(), leftover);
                    }

                    needed -= amountToTake;
                    if (stack.getCount() == 0) {
                        player.inventory.mainInventory.set(i, ItemStack.EMPTY);
                    }
                }
            }
        }
    }
}
