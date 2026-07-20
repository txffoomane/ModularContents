package modularcontents.custom.block;

import modularcontents.custom.item.CustomBlockInfo;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCustomLog extends BlockCustom {

    public static final PropertyEnum<BlockLog.EnumAxis> AXIS = BlockLog.LOG_AXIS;

    public BlockCustomLog(CustomBlockInfo info) {
        super(info);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, BlockLog.EnumAxis.Y));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        BlockLog.EnumAxis axis = BlockLog.EnumAxis.Y;
        int i = meta & 12;
        if (i == 4) {
            axis = BlockLog.EnumAxis.X;
        } else if (i == 8) {
            axis = BlockLog.EnumAxis.Z;
        } else if (i == 12) {
            axis = BlockLog.EnumAxis.NONE;
        }
        return this.getDefaultState().withProperty(AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        BlockLog.EnumAxis axis = state.getValue(AXIS);
        if (axis == BlockLog.EnumAxis.X) {
            i |= 4;
        } else if (axis == BlockLog.EnumAxis.Z) {
            i |= 8;
        } else if (axis == BlockLog.EnumAxis.NONE) {
            i |= 12;
        }
        return i;
    }
}
