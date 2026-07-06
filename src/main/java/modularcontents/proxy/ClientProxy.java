package modularcontents.proxy;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.block.TileEntityAirdrop;
import modularcontents.custom.client.ModularResourcePack;
import modularcontents.custom.client.SoundAirdropSmoke;
import modularcontents.custom.client.particle.ParticleAirdropSmoke;
import modularcontents.custom.entity.EntityAirdrop;
import modularcontents.custom.entity.EntitySignalFlare;
import modularcontents.custom.entity.RenderAirdrop;
import modularcontents.custom.entity.RenderSignalFlare;
import modularcontents.custom.keybind.KeybindManager;
import modularcontents.custom.loot.EquipmentManager;
import modularcontents.custom.pack.PackZipUtils;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.tab.CustomTabManager;
import modularcontents.custom.npc.EntityCustomNPC;
import modularcontents.custom.npc.EntityNPCBullet;
import modularcontents.custom.npc.RenderCustomNPC;
import modularcontents.custom.npc.RenderNPCBullet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        KeybindManager.register();
        RenderingRegistry.registerEntityRenderingHandler(EntityAirdrop.class, RenderAirdrop::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySignalFlare.class, manager -> new RenderSignalFlare(manager, ModularcontentsMod.signal_flare, Minecraft.getMinecraft().getRenderItem()));
        RenderingRegistry.registerEntityRenderingHandler(EntityCustomNPC.class, RenderCustomNPC::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityNPCBullet.class, RenderNPCBullet::new);
        injectCustomResourcePack(event.getModConfigurationDirectory().getParentFile());
    }

    private void injectCustomResourcePack(File gameDir) {
        try {
            ModularResourcePack pack = new ModularResourcePack(gameDir);
            List<IResourcePack> defaultPacks = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
            defaultPacks.add(pack);

            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            if (manager instanceof SimpleReloadableResourceManager) {
                Map<String, FallbackResourceManager> domainManagers = ReflectionHelper.getPrivateValue(SimpleReloadableResourceManager.class, (SimpleReloadableResourceManager) manager, "domainResourceManagers", "field_110548_a");
                FallbackResourceManager fallback = domainManagers.get("modularcontents");
                if (fallback != null) {
                    fallback.addResourcePack(pack);
                } else {
                    fallback = new FallbackResourceManager(new MetadataSerializer());
                    fallback.addResourcePack(pack);
                    domainManagers.put("modularcontents", fallback);
                }
            }

            System.out.println("[ModularContents] Successfully injected ModularResourcePack for dynamic textures!");
        } catch (Exception e) {
            System.out.println("[ModularContents] Failed to inject dynamic resource pack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void spawnAirdropSmoke(World world, double x, double y, double z, float r, float g, float b) {
        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleAirdropSmoke(world, x, y, z, 0.0D, 0.05D, 0.0D, r, g, b));
    }

    @Override
    public void playAirdropSmokeSound(TileEntityAirdrop te) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new SoundAirdropSmoke(te));
    }

    @Override
    public void handleContentSync(String recipesJson, String tabsJson, String requiredPacksJson, String equipmentJson) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.isIntegratedServerRunning()) {
                return;
            }
            ListWorkbenchRecipeManager.applySyncedRecipes(recipesJson);
            CustomTabManager.applySyncedTabs(tabsJson);
            EquipmentManager.applySynced(equipmentJson);

            List<String> missing = PackZipUtils.findMissingPacks(mc.mcDataDir, requiredPacksJson);
            if (!missing.isEmpty() && mc.player != null) {
                mc.player.sendMessage(new TextComponentString(TextFormatting.RED
                        + I18n.format("modularcontents.sync.missing_packs", String.join(", ", missing))));
                mc.player.sendMessage(new TextComponentString(TextFormatting.GRAY
                        + I18n.format("modularcontents.sync.missing_packs_hint")));
            }
        });
    }
}
