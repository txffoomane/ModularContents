package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.custom.inventory.ContainerHandcraft;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandcraftSync implements IMessage {
    public String recipeId;
    public int amount;
    public int progress;
    public int totalTime;

    public PacketHandcraftSync() {}

    public PacketHandcraftSync(String recipeId, int amount, int progress, int totalTime) {
        this.recipeId = recipeId != null ? recipeId : "";
        this.amount = amount;
        this.progress = progress;
        this.totalTime = totalTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.recipeId = ByteBufUtils.readUTF8String(buf);
        this.amount = buf.readInt();
        this.progress = buf.readInt();
        this.totalTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.recipeId);
        buf.writeInt(this.amount);
        buf.writeInt(this.progress);
        buf.writeInt(this.totalTime);
    }

    public static class Handler implements IMessageHandler<PacketHandcraftSync, IMessage> {
        @Override
        public IMessage onMessage(PacketHandcraftSync message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (Minecraft.getMinecraft().player.openContainer instanceof ContainerHandcraft) {
                        ContainerHandcraft c = (ContainerHandcraft) Minecraft.getMinecraft().player.openContainer;
                        c.activeRecipeId = message.recipeId;
                        c.clientCraftAmount = message.amount;
                        c.clientProgress = message.progress;
                        c.clientTotalTime = message.totalTime;
                    }
                });
            }
            return null;
        }
    }
}
