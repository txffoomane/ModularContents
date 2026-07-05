package modularcontents.proxy;

import modularcontents.custom.block.TileEntityAirdrop;
import modularcontents.custom.client.SoundAirdropSmoke;
import modularcontents.custom.client.particle.ParticleAirdropSmoke;
import modularcontents.custom.loot.EquipmentManager;
import modularcontents.custom.pack.PackZipUtils;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.tab.CustomTabManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ClientProxy extends CommonProxy {

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
