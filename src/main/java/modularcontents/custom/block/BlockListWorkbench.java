package modularcontents.custom.block;

import modularcontents.ModularcontentsMod;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockListWorkbench extends Block implements ITileEntityProvider {

    public static final CustomPropertyBool CRAFTING = CustomPropertyBool.create("crafting");
    public static final CustomPropertyDirection FACING = CustomPropertyDirection.create("facing", net.minecraft.util.EnumFacing.Plane.HORIZONTAL);

    public static boolean isBreakingMultiblock = false;

    // Hitboxes for 2x1x1 dimensions
    // "Центр коллизии слева" - значит якорный блок находится слева, а вторая половина уходит вправо (относительно взгляда игрока)
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(-1.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 2.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 2.0D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.0D, -1.0D, 1.0D, 1.0D, 1.0D);

    public BlockListWorkbench() {
        super(Material.ROCK); // Changed from WOOD to ROCK temporarily to bypass the missing field
        this.setHardness(2.5F);
        this.setResistance(10.0F);
        this.setCreativeTab(modularcontents.ModularcontentsMod.MODULAR_TAB);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CRAFTING, false).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CRAFTING, FACING);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
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
        int facingIndex = meta & 7;
        EnumFacing facing = EnumFacing.getFront(facingIndex);
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
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(CRAFTING, false);
    }

    public BlockPos getPartPos(IBlockState state, BlockPos pos) {
        return pos.offset(state.getValue(FACING).rotateYCCW());
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(CRAFTING)) {
            return 5;
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(ModularcontentsMod.instance, 1, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileEntityListWorkbench) {
            TileEntityListWorkbench te = (TileEntityListWorkbench) tileentity;

            // Drop output items
            for (int i = 0; i < te.outputSlots.getSlots(); i++) {
                ItemStack output = te.outputSlots.getStackInSlot(i);
                if (!output.isEmpty()) {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), output);
                }
            }

            // Drop buffered ingredients if crafting was interrupted
            for (int i = 0; i < te.bufferSlots.getSlots(); i++) {
                ItemStack bufferItem = te.bufferSlots.getStackInSlot(i);
                if (!bufferItem.isEmpty()) {
                    InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), bufferItem);
                }
            }
        }

        boolean wasBreaking = isBreakingMultiblock;
        isBreakingMultiblock = true;
        try {
            BlockPos partPos = getPartPos(state, pos);
            IBlockState partState = worldIn.getBlockState(partPos);
            if (partState.getBlock() instanceof BlockListWorkbenchPart) {
                worldIn.setBlockState(partPos, net.minecraft.init.Blocks.AIR.getDefaultState(), 3);
            }
        } finally {
            isBreakingMultiblock = wasBreaking;
        }

        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityListWorkbench();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
