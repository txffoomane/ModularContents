package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketCraftStart implements IMessage {
    public String recipeId;
    public BlockPos pos;
    public int amount;

    public PacketCraftStart() {
    }

    public PacketCraftStart(String recipeId, BlockPos pos, int amount) {
        this.recipeId = recipeId;
        this.pos = pos;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.recipeId = ByteBufUtils.readUTF8String(buf);
        this.pos = BlockPos.fromLong(buf.readLong());
        this.amount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, recipeId != null ? recipeId : "");
        buf.writeLong(pos != null ? pos.toLong() : 0L);
        buf.writeInt(amount);
    }
}
