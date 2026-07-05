package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;

public class PacketZoneLoot implements IMessage {
    public int minX;
    public int minZ;
    public int maxX;
    public int maxZ;
    public boolean clear;
    public List<PresetEntry> presets = new ArrayList<>();

    public PacketZoneLoot() {
    }

    public PacketZoneLoot(int minX, int minZ, int maxX, int maxZ, boolean clear, List<PresetEntry> presets) {
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
        this.clear = clear;
        if (presets != null) {
            this.presets = presets;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.minX = buf.readInt();
        this.minZ = buf.readInt();
        this.maxX = buf.readInt();
        this.maxZ = buf.readInt();
        this.clear = buf.readBoolean();
        int count = buf.readInt();
        this.presets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            float chance = buf.readFloat();
            this.presets.add(new PresetEntry(name, chance));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.minX);
        buf.writeInt(this.minZ);
        buf.writeInt(this.maxX);
        buf.writeInt(this.maxZ);
        buf.writeBoolean(this.clear);
        buf.writeInt(this.presets.size());
        for (PresetEntry entry : this.presets) {
            ByteBufUtils.writeUTF8String(buf, entry.name);
            buf.writeFloat(entry.chance);
        }
    }

    public static class PresetEntry {
        public final String name;
        public final float chance;

        public PresetEntry(String name, float chance) {
            this.name = name;
            this.chance = chance;
        }
    }
}
