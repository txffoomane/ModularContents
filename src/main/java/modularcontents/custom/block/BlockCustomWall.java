package modularcontents.custom.block;

import modularcontents.custom.item.CustomBlockInfo;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockCustomWall extends BlockWall {
    public BlockCustomWall(CustomBlockInfo info) {
        super(net.minecraft.init.Blocks.STONE);
        this.setRegistryName("modularcontents", info.id);
        this.setUnlocalizedName(info.id);
        this.setHardness(info.hardness);
        this.setResistance(info.resistance);
        this.setLightLevel(info.lightLevel);
        if (info.toolClass != null && !info.toolClass.isEmpty()) {
            this.setHarvestLevel(info.toolClass, info.harvestLevel);
        }
        setSoundFromName(info.material);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this));
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
}
