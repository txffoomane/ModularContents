package modularcontents.custom.client;

import modularcontents.custom.client.particle.ParticleAirdropSmoke;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxyUtils {
    public static void spawnAirdropSmoke(World world, double x, double y, double z, float r, float g, float b) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleAirdropSmoke(world, x, y, z, 0.0D, 0.05D, 0.0D, r, g, b));
    }
}