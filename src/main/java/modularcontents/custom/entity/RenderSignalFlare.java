package modularcontents.custom.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderSignalFlare extends Render<EntitySignalFlare> {
    private final RenderItem itemRenderer;
    private final Item item;

    public RenderSignalFlare(RenderManager renderManager, Item item, RenderItem itemRenderer) {
        super(renderManager);
        this.item = item;
        this.itemRenderer = itemRenderer;
        this.shadowSize = 0.15F;
    }

    @Override
    public void doRender(EntitySignalFlare entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y + 0.15F, (float) z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();

        GlStateManager.scale(0.75F, 0.75F, 0.75F);

        float spin = entity.isLanded()
                ? entity.getLandSpin()
                : (entity.ticksExisted + partialTicks) * EntitySignalFlare.SPIN_DEGREES_PER_TICK;
        GlStateManager.rotate(spin, 1.0F, 0.2F, 0.0F);

        this.itemRenderer.renderItem(new ItemStack(this.item), ItemCameraTransforms.TransformType.FIXED);
        
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySignalFlare entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
