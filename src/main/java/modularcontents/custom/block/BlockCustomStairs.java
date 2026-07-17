package modularcontents.custom.block;

import modularcontents.custom.item.CustomBlockInfo;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockCustomStairs extends BlockStairs {

    private final CustomBlockInfo info;

    public BlockCustomStairs(CustomBlockInfo info) {
        super(Blocks.STONE.getDefaultState());
        this.info = info;

        this.setRegistryName(info.id);
        this.setUnlocalizedName(info.id);

        this.setHardness(info.hardness);
        this.setResistance(info.resistance);
        this.setLightLevel(info.lightLevel);

        if (info.toolClass != null && !info.toolClass.isEmpty()) {
            this.setHarvestLevel(info.toolClass, info.harvestLevel);
        }

        setSoundFromName(info.material);

        // Ensure proper lighting for stairs
        this.useNeighborBrightness = true;
    }

    private void setSoundFromName(String name) {
        if (name == null) {
            this.setSoundType(SoundType.STONE);
            return;
        }
        switch (name.toLowerCase()) {
            case "wood": this.setSoundType(SoundType.WOOD); break;
            case "earth": this.setSoundType(SoundType.GROUND); break;
            case "iron": this.setSoundType(SoundType.METAL); break;
            case "glass": this.setSoundType(SoundType.GLASS); break;
            case "leaves": this.setSoundType(SoundType.PLANT); break;
            case "cloth": this.setSoundType(SoundType.CLOTH); break;
            case "sand": this.setSoundType(SoundType.SAND); break;
            default: this.setSoundType(SoundType.STONE); break;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (I18n.hasKey("item." + info.id + ".tooltip")) {
            tooltip.add(I18n.format("item." + info.id + ".tooltip"));
        }
    }

    @Override
    public String getLocalizedName() {
        if (info.displayName != null && !info.displayName.isEmpty()) {
            return info.displayName;
        }
        return super.getLocalizedName();
    }
}
