package modularcontents.custom.block;

import modularcontents.ModularcontentsMod;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockListWorkbenchPart extends Block {

    public static final CustomPropertyBool CRAFTING = BlockListWorkbench.CRAFTING;
    public static final CustomPropertyDirection FACING = BlockListWorkbench.FACING;

    private static final net.minecraft.util.math.AxisAlignedBB AABB_NORTH = new net.minecraft.util.math.AxisAlignedBB(0.0D, 0.0D, 0.0D, 2.0D, 1.0D, 1.0D);
    private static final net.minecraft.util.math.AxisAlignedBB AABB_SOUTH = new net.minecraft.util.math.AxisAlignedBB(-1.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final net.minecraft.util.math.AxisAlignedBB AABB_WEST = new net.minecraft.util.math.AxisAlignedBB(0.0D, 0.0D, -1.0D, 1.0D, 1.0D, 1.0D);
    private static final net.minecraft.util.math.AxisAlignedBB AABB_EAST = new net.minecraft.util.math.AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 2.0D);

    public BlockListWorkbenchPart(Block master) {
        super(master.getDefaultState().getMaterial());
        this.setHardness(2.5F);
        this.setResistance(10.0F);
        this.setSoundType(master.getSoundType());
        this.setDefaultState(this.blockState.getBaseState().withProperty(CRAFTING, false).withProperty(FACING, EnumFacing.NORTH));
    }

    public static BlockPos getMasterPos(IBlockState state, BlockPos partPos) {
        return partPos.offset(state.getValue(FACING).rotateY());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CRAFTING, FACING);
    }

    @Override
    public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case SOUTH: return AABB_SOUTH;
            case WEST: return AABB_WEST;
            case EAST: return AABB_EAST;
            case NORTH:
            default: return AABB_NORTH;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean crafting = (meta & 8) != 0;
        EnumFacing facing = EnumFacing.getFront(meta & 7);
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(CRAFTING, crafting).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getIndex();
        if (state.getValue(CRAFTING)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getValue(CRAFTING) ? 5 : 0;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos masterPos = getMasterPos(state, pos);
        IBlockState masterState = worldIn.getBlockState(masterPos);
        if (masterState.getBlock() instanceof BlockListWorkbench) {
            return masterState.getBlock().onBlockActivated(worldIn, masterPos, masterState, playerIn, hand, facing, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!BlockListWorkbench.isBreakingMultiblock) {
            BlockPos masterPos = getMasterPos(state, pos);
            IBlockState masterState = world.getBlockState(masterPos);
            if (masterState.getBlock() instanceof BlockListWorkbench) {
                boolean drop = !player.capabilities.isCreativeMode;
                world.destroyBlock(masterPos, drop);
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!BlockListWorkbench.isBreakingMultiblock) {
            BlockPos masterPos = getMasterPos(state, pos);
            IBlockState masterState = worldIn.getBlockState(masterPos);
            if (masterState.getBlock() instanceof BlockListWorkbench) {
                worldIn.destroyBlock(masterPos, true);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ModularcontentsMod.custom_workbench_item);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
