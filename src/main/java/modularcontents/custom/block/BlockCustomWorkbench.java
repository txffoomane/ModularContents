package modularcontents.custom.block;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.pack.WorkbenchConfig;
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
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockCustomWorkbench extends Block implements ITileEntityProvider {

    public static final CustomPropertyBool CRAFTING = CustomPropertyBool.create("crafting");
    public static final CustomPropertyDirection FACING = CustomPropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    private final WorkbenchConfig config;

    public BlockCustomWorkbench(WorkbenchConfig config) {
        super(Material.ROCK); // Or choose based on config.material
        this.config = config;
        this.setHardness(config.hardness > 0 ? config.hardness : 2.5F);
        this.setResistance(10.0F);
        this.setCreativeTab(ModularcontentsMod.MODULAR_TAB);
        this.setRegistryName(config.id);
        this.setUnlocalizedName(config.id);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CRAFTING, false).withProperty(FACING, EnumFacing.NORTH));
    }

    public String getWorkbenchId() {
        return config.id;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CRAFTING, FACING);
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

        if (tileentity instanceof TileEntityCustomWorkbench) {
            TileEntityCustomWorkbench te = (TileEntityCustomWorkbench) tileentity;

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
        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileEntityCustomWorkbench te = new TileEntityCustomWorkbench();
        te.setWorkbenchId(config.id);
        return te;
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
}
