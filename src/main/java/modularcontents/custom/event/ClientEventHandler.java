package modularcontents.custom.event;

import modularcontents.custom.gui.GuiContentCreator;
import modularcontents.custom.keybind.KeybindManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import modularcontents.ModularcontentsMod;
import modularcontents.custom.network.PacketOpenCreator;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ModularcontentsMod.MODID)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeybindManager.handcraftGui != null && KeybindManager.handcraftGui.isPressed()) {
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new modularcontents.custom.network.PacketOpenHandcraft());
        }

        if (KeybindManager.openCreatorGui != null && KeybindManager.openCreatorGui.isPressed()) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;

            if (player != null) {
                if (player.isCreative()) {
                    ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketOpenCreator());
                } else {
                    player.sendMessage(new TextComponentString(TextFormatting.RED + "Content Creator requires Creative Mode!"));
                }
            }
        }
    }
}