package modularcontents.custom.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PacketSyncContent implements IMessage {
    public String recipesJson = "";
    public String tabsJson = "";
    public String requiredPacksJson = "";
    public String equipmentJson = "";

    public PacketSyncContent() {
    }

    public PacketSyncContent(String recipesJson, String tabsJson, String requiredPacksJson, String equipmentJson) {
        this.recipesJson = recipesJson != null ? recipesJson : "";
        this.tabsJson = tabsJson != null ? tabsJson : "";
        this.requiredPacksJson = requiredPacksJson != null ? requiredPacksJson : "";
        this.equipmentJson = equipmentJson != null ? equipmentJson : "";
    }

    @Override
    public void toBytes(ByteBuf buf) {
        writeCompressed(buf, recipesJson);
        writeCompressed(buf, tabsJson);
        writeCompressed(buf, requiredPacksJson);
        writeCompressed(buf, equipmentJson);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        recipesJson = readCompressed(buf);
        tabsJson = readCompressed(buf);
        requiredPacksJson = readCompressed(buf);
        equipmentJson = readCompressed(buf);
    }

    private static void writeCompressed(ByteBuf buf, String value) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
                gzip.write(value.getBytes(StandardCharsets.UTF_8));
            }
            byte[] compressed = bytes.toByteArray();
            buf.writeInt(compressed.length);
            buf.writeBytes(compressed);
        } catch (Exception e) {
            buf.writeInt(0);
        }
    }

    private static String readCompressed(ByteBuf buf) {
        try {
            int length = buf.readInt();
            if (length <= 0) {
                return "";
            }
            byte[] compressed = new byte[length];
            buf.readBytes(compressed);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                byte[] chunk = new byte[8192];
                int read;
                while ((read = in.read(chunk)) != -1) {
                    out.write(chunk, 0, read);
                }
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
