package modularcontents.custom.network;

import modularcontents.ModularcontentsMod;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSyncContentHandler implements IMessageHandler<PacketSyncContent, IMessage> {

    @Override
    public IMessage onMessage(PacketSyncContent message, MessageContext ctx) {
        ModularcontentsMod.proxy.handleContentSync(message.recipesJson, message.tabsJson, message.requiredPacksJson, message.equipmentJson);
        return null;
    }
}
