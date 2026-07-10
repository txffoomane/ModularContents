package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.ModularcontentsMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenHandcraft implements IMessage {
    public PacketOpenHandcraft() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    public static class Handler implements IMessageHandler<PacketOpenHandcraft, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenHandcraft message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                player.openGui(ModularcontentsMod.instance, 5, player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
            });
            return null;
        }
    }
}
