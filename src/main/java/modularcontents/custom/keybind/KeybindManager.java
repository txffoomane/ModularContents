package modularcontents.custom.keybind;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeybindManager {
    public static KeyBinding openCreatorGui;
    public static KeyBinding handcraftGui;

    public static void register() {
        openCreatorGui = new KeyBinding("key.modularcontents.open_creator", Keyboard.KEY_F8, "key.categories.modularcontents");
        ClientRegistry.registerKeyBinding(openCreatorGui);

        handcraftGui = new KeyBinding("key.modularcontents.handcraft", Keyboard.KEY_H, "key.categories.modularcontents");
        ClientRegistry.registerKeyBinding(handcraftGui);
    }
}