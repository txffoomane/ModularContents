package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.custom.inventory.ContainerHandcraft;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import net.minecraft.entity.player.EntityPlayerMP;
<<<<<<< HEAD
import net.minecraft.item.ItemStack;
=======
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHandcraftAction implements IMessage {
    public String recipeId;
<<<<<<< HEAD

    public PacketHandcraftAction() {}
    public PacketHandcraftAction(String recipeId) { this.recipeId = recipeId; }

    @Override
    public void fromBytes(ByteBuf buf) { this.recipeId = ByteBufUtils.readUTF8String(buf); }

    @Override
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeUTF8String(buf, this.recipeId); }
=======
    public int amount;

    public PacketHandcraftAction() {}
    public PacketHandcraftAction(String recipeId, int amount) { 
        this.recipeId = recipeId; 
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) { 
        this.recipeId = ByteBufUtils.readUTF8String(buf); 
        this.amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) { 
        ByteBufUtils.writeUTF8String(buf, this.recipeId); 
        buf.writeInt(this.amount);
    }
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)

    public static class Handler implements IMessageHandler<PacketHandcraftAction, IMessage> {
        @Override
        public IMessage onMessage(PacketHandcraftAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerHandcraft) {
<<<<<<< HEAD
                    ListWorkbenchRecipe recipe = ListWorkbenchRecipeManager.getRecipe("handcraft", message.recipeId);
                    if (recipe != null) {
                        // Instant craft logic for handcraft. 
                        // It takes ingredients from inventory and gives result.
                        // For a real implementation we would iterate inventory and remove ingredients, 
                        // then add the result item to the player.
                    }
=======
                    ((ContainerHandcraft) player.openContainer).startCrafting(message.recipeId, message.amount);
>>>>>>> 842bc66 (Feat: Custom Workbenches, Handcrafting, and Folder-based Recipe separation)
                }
            });
            return null;
        }
    }
}
