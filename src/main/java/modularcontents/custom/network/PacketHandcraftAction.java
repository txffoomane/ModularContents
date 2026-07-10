package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.custom.inventory.ContainerHandcraft;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHandcraftAction implements IMessage {
    public String recipeId;

    public PacketHandcraftAction() {}
    public PacketHandcraftAction(String recipeId) { this.recipeId = recipeId; }

    @Override
    public void fromBytes(ByteBuf buf) { this.recipeId = ByteBufUtils.readUTF8String(buf); }

    @Override
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeUTF8String(buf, this.recipeId); }

    public static class Handler implements IMessageHandler<PacketHandcraftAction, IMessage> {
        @Override
        public IMessage onMessage(PacketHandcraftAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerHandcraft) {
                    ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe("handcraft", message.recipeId);
                    if (recipe != null) {
                        // Instant craft logic for handcraft. 
                        // It takes ingredients from inventory and gives result.
                        // For a real implementation we would iterate inventory and remove ingredients, 
                        // then add the result item to the player.
                    }
                }
            });
            return null;
        }
    }
}
