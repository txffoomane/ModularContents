package modularcontents.custom.block;

import modularcontents.custom.item.CustomBlockInfo;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockCustomFence extends BlockFence {
    public BlockCustomFence(CustomBlockInfo info) {
        super(getMaterialFromName(info.material), getMaterialFromName(info.material).getMaterialMapColor());
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

    private static Material getMaterialFromName(String name) {
        if (name == null) return Material.ROCK;
        switch (name.toLowerCase()) {
            case "wood": return Material.WOOD;
            case "earth": return Material.GROUND;
            case "iron": return Material.IRON;
            case "glass": return Material.GLASS;
            case "leaves": return Material.LEAVES;
            case "cloth": return Material.CLOTH;
            case "sand": return Material.SAND;
            default: return Material.ROCK;
        }
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
