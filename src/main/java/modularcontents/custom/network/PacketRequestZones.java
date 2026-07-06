package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRequestZones implements IMessage {
    public PacketRequestZones() {}

    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}
}
