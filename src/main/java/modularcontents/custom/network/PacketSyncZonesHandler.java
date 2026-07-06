package modularcontents.custom.network;

import modularcontents.custom.gui.GuiZoneEquipment;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncZonesHandler implements IMessageHandler<PacketSyncZones, IMessage> {
    @Override
    public IMessage onMessage(PacketSyncZones message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().currentScreen instanceof GuiZoneEquipment) {
                ((GuiZoneEquipment) Minecraft.getMinecraft().currentScreen).receiveZones(message.zones);
            }
        });
        return null;
    }
}
