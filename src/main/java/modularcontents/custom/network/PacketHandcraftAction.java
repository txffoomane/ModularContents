package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.custom.inventory.ContainerHandcraft;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketHandcraftAction implements IMessage {
    public String recipeId;
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

    public static class Handler implements IMessageHandler<PacketHandcraftAction, IMessage> {
        @Override
        public IMessage onMessage(PacketHandcraftAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerHandcraft) {
                    ((ContainerHandcraft) player.openContainer).startCrafting(message.recipeId, message.amount);
                }
            });
            return null;
        }
    }
}
