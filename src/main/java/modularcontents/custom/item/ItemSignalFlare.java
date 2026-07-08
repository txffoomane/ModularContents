package modularcontents.custom.item;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.entity.EntitySignalFlare;
import modularcontents.custom.config.ModularContentsConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;

public class ItemSignalFlare extends Item {
    public ItemSignalFlare() {
        this.setRegistryName("signal_flare");
        this.setUnlocalizedName("signal_flare");
        this.setMaxStackSize(16);
        this.setCreativeTab(ModularcontentsMod.MODULAR_TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        boolean hasClearSky = true;
        net.minecraft.util.math.BlockPos playerPos = new net.minecraft.util.math.BlockPos(playerIn);
        for (int y = playerPos.getY(); y < 256; y++) {
            net.minecraft.block.state.IBlockState state = worldIn.getBlockState(new net.minecraft.util.math.BlockPos(playerPos.getX(), y, playerPos.getZ()));
            if (state.getMaterial() != net.minecraft.block.material.Material.AIR && state.getMaterial() != net.minecraft.block.material.Material.LEAVES && state.getMaterial() != net.minecraft.block.material.Material.GLASS) {
                if (state.isOpaqueCube() || state.getMaterial().blocksMovement()) {
                    hasClearSky = false;
                    break;
                }
            }
        }

        if (!hasClearSky) {
            if (!worldIn.isRemote) {
                playerIn.sendMessage(new net.minecraft.util.text.TextComponentString(net.minecraft.util.text.TextFormatting.RED + "You must be outdoors to use a signal flare."));
            }
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            EntitySignalFlare flare = new EntitySignalFlare(worldIn, playerIn);
            flare.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(flare);
            playerIn.getCooldownTracker().setCooldown(this, ModularContentsConfig.airdropCallerCooldown);
        }

        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}