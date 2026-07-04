package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCraftCancel implements IMessage {
    public BlockPos pos;
    public int slot;

    public PacketCraftCancel() {
    }

    public PacketCraftCancel(BlockPos pos, int slot) {
        this.pos = pos;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.slot = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos != null ? pos.toLong() : 0L);
        buf.writeInt(slot);
    }
}
