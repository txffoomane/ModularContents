package modularcontents.custom.npc;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;

public class RenderCustomNPC extends RenderBiped<EntityCustomNPC> {

    private final ModelPlayer modelSteve;
    private final ModelPlayer modelAlex;

    public RenderCustomNPC(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPlayer(0.0F, false), 0.5F);
        this.modelSteve = new ModelPlayer(0.0F, false);
        this.modelAlex = new ModelPlayer(0.0F, true);

        this.addLayer(new LayerHeldItem(this));
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
            protected void initArmor() {
                this.modelLeggings = new ModelBiped(0.5F);
                this.modelArmor = new ModelBiped(1.0F);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
    public void doRender(EntityCustomNPC entity, double x, double y, double z, float entityYaw, float partialTicks) {
        CustomNPCInfo info = NPCManager.NPCS.get(entity.getNpcId());
        if (info != null && info.slimModel) {
            this.mainModel = this.modelAlex;
        } else {
            this.mainModel = this.modelSteve;
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityCustomNPC entity) {
        CustomNPCInfo info = NPCManager.NPCS.get(entity.getNpcId());
        if (info != null && info.texture != null && !info.texture.isEmpty()) {
            return new ResourceLocation(info.texture);
        }
        return new ResourceLocation("minecraft:textures/entity/steve.png");
    }
}