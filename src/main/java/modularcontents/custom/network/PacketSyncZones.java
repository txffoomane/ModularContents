package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import modularcontents.custom.zone.LootZone;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PacketSyncZones implements IMessage {
    public List<ClientZoneInfo> zones = new ArrayList<>();

    public PacketSyncZones() {
    }

    public PacketSyncZones(List<LootZone> list) {
        for (LootZone z : list) {
            zones.add(new ClientZoneInfo(z.id, z.name, z.minX, z.minZ, z.maxX, z.maxZ, z.color, z.respawnIntervalTicks, z.presets));
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        zones = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID id = new UUID(buf.readLong(), buf.readLong());
            String name = ByteBufUtils.readUTF8String(buf);
            int minX = buf.readInt();
            int minZ = buf.readInt();
            int maxX = buf.readInt();
            int maxZ = buf.readInt();
            int color = buf.readInt();
            int respawnIntervalTicks = buf.readInt();

            int presetCount = buf.readInt();
            Map<String, Float> presets = new HashMap<>();
            for (int j = 0; j < presetCount; j++) {
                presets.put(ByteBufUtils.readUTF8String(buf), buf.readFloat());
            }

            zones.add(new ClientZoneInfo(id, name, minX, minZ, maxX, maxZ, color, respawnIntervalTicks, presets));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(zones.size());
        for (ClientZoneInfo z : zones) {
            buf.writeLong(z.id.getMostSignificantBits());
            buf.writeLong(z.id.getLeastSignificantBits());
            ByteBufUtils.writeUTF8String(buf, z.name);
            buf.writeInt(z.minX);
            buf.writeInt(z.minZ);
            buf.writeInt(z.maxX);
            buf.writeInt(z.maxZ);
            buf.writeInt(z.color);
            buf.writeInt(z.respawnIntervalTicks);

            buf.writeInt(z.presets.size());
            for (Map.Entry<String, Float> entry : z.presets.entrySet()) {
                ByteBufUtils.writeUTF8String(buf, entry.getKey());
                buf.writeFloat(entry.getValue());
            }
        }
    }

    public static class ClientZoneInfo {
        public UUID id;
        public String name;
        public int minX, minZ, maxX, maxZ;
        public int color;
        public int respawnIntervalTicks;
        public Map<String, Float> presets;

        public ClientZoneInfo(UUID id, String name, int minX, int minZ, int maxX, int maxZ, int color, int respawnIntervalTicks, Map<String, Float> presets) {
            this.id = id;
            this.name = name != null ? name : "Zone";
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxZ = maxZ;
            this.color = color;
            this.respawnIntervalTicks = respawnIntervalTicks;
            this.presets = presets != null ? new HashMap<>(presets) : new HashMap<>();
        }
    }
}