package modularcontents.custom.item;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.entity.EntitySignalFlare;
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

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            EntitySignalFlare flare = new EntitySignalFlare(worldIn, playerIn);
            flare.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(flare);
        }

        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}