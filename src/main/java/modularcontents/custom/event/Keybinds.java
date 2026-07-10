package modularcontents.custom.event;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.network.PacketOpenHandcraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class Keybinds {
    public static KeyBinding handcraftKey;

    public static void register() {
        handcraftKey = new KeyBinding("key.handcraft", Keyboard.KEY_H, "key.categories.modularcontents");
        ClientRegistry.registerKeyBinding(handcraftKey);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (handcraftKey.isPressed()) {
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketOpenHandcraft());
        }
    }
}
