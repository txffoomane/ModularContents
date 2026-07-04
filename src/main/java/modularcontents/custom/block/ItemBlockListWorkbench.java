package modularcontents.custom.block;

import modularcontents.ModularcontentsMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBlockListWorkbench extends ItemBlock {

    public ItemBlockListWorkbench(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        EnumFacing facing = newState.getValue(BlockListWorkbench.FACING);
        BlockPos partPos = pos.offset(facing.rotateYCCW());

        IBlockState partSpot = world.getBlockState(partPos);
        if (!partSpot.getBlock().isReplaceable(world, partPos)) {
            return false;
        }

        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            return false;
        }

        IBlockState placed = world.getBlockState(pos);
        if (placed.getBlock() instanceof BlockListWorkbench) {
            world.setBlockState(partPos, ModularcontentsMod.custom_workbench_part.getDefaultState()
                    .withProperty(BlockListWorkbenchPart.FACING, placed.getValue(BlockListWorkbench.FACING))
                    .withProperty(BlockListWorkbenchPart.CRAFTING, false), 3);
        }
        return true;
    }
}
