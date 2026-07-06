package modularcontents.custom.npc;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderNPCBullet extends Render<EntityNPCBullet> {

    public RenderNPCBullet(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EntityNPCBullet entity, double x, double y, double z, float entityYaw, float partialTicks) {
        // We don't render an actual model for the bullet, it will just leave a particle trail handled in EntityNPCBullet.java
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityNPCBullet entity) {
        return null;
    }
}