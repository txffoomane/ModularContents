package modularcontents.custom.block;

import modularcontents.custom.item.CustomBlockInfo;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockCustomSlab extends BlockSlab {
    public static final java.util.Map<BlockCustomSlab, BlockCustomSlab> SLABS = new java.util.HashMap<>();
    private final boolean isDouble;

    private final CustomBlockInfo info;

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);

    public BlockCustomSlab(CustomBlockInfo info, boolean isDouble) {
        super(getMaterialFromName(info.material));
        this.isDouble = isDouble;
        this.info = info;

        this.setRegistryName(isDouble ? info.id + "_double" : info.id);
        this.setUnlocalizedName(info.id);

        this.setHardness(info.hardness);
        this.setResistance(info.resistance);
        this.setLightLevel(info.lightLevel);

        if (info.toolClass != null && !info.toolClass.isEmpty()) {
            this.setHarvestLevel(info.toolClass, info.harvestLevel);
        }

        setSoundFromName(info.material);

        IBlockState iblockstate = this.blockState.getBaseState();
        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        }
        this.setDefaultState(iblockstate.withProperty(VARIANT, Variant.DEFAULT));
        this.useNeighborBrightness = true;
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

    @Override
    public void onBlockAdded(World worldIn, net.minecraft.util.math.BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (this.isDouble) {
            String parentId = info.id;
            if (parentId.endsWith("_slab")) {
                parentId = parentId.substring(0, parentId.length() - 5);
            }
            net.minecraft.block.Block parent = net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation("modularcontents", parentId));
            if (parent != null && parent != net.minecraft.init.Blocks.AIR) {
                worldIn.setBlockState(pos, parent.getDefaultState(), 3);
            }
        }
    }

    @Override
    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName();
    }

    @Override
    public boolean isDouble() {
        return this.isDouble;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);
        if (!this.isDouble()) {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }
        return iblockstate;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i |= 8;
        }
        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
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

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }
}
