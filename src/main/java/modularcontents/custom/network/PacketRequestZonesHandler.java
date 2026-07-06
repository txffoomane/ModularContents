package modularcontents.custom.network;

import modularcontents.custom.zone.LootZoneHandler;
import modularcontents.custom.zone.LootZoneManager;
import modularcontents.ModularcontentsMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestZonesHandler implements IMessageHandler<PacketRequestZones, IMessage> {
    @Override
    public IMessage onMessage(PacketRequestZones message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        player.getServerWorld().addScheduledTask(() -> {
            if (player.isCreative()) {
                LootZoneManager manager = LootZoneHandler.get(player.getServerWorld());
                ModularcontentsMod.PACKET_HANDLER.sendTo(new PacketSyncZones(manager.zones), player);
            }
        });
        return null;
    }
}
