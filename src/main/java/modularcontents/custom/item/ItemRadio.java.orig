package modularcontents.custom.item;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.entity.EntityAirdrop;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class ItemRadio extends Item {
    public ItemRadio() {
        this.setRegistryName("radio");
        this.setUnlocalizedName("radio");
        this.setMaxStackSize(1);
        this.setMaxDamage(10); // 10 uses
        this.setCreativeTab(ModularcontentsMod.MODULAR_TAB);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            // Random distance between 50 and 200
            double distance = 50.0D + worldIn.rand.nextDouble() * 150.0D;
            // Random angle
            double angle = worldIn.rand.nextDouble() * Math.PI * 2;

            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;

            double targetX = playerIn.posX + offsetX;
            double targetZ = playerIn.posZ + offsetZ;
            double targetY = 250.0D; // Spawn high up

            // delay between 200 and 1000 ticks (10 to 50 seconds)
            int delayTicks = 200 + worldIn.rand.nextInt(800);

            EntityAirdrop airdrop = new EntityAirdrop(worldIn, targetX, targetY, targetZ);
            airdrop.setDelayAndCaller(delayTicks, playerIn.getName(), false, targetX, targetZ);
            worldIn.spawnEntity(airdrop);

            int seconds = delayTicks / 20;
            playerIn.sendMessage(new TextComponentString(TextFormatting.GREEN + "Airdrop requested! Coordinates: X: " + (int)targetX + ", Z: " + (int)targetZ + ". ETA: " + seconds + " seconds."));
        }

        if (!playerIn.capabilities.isCreativeMode) {
            itemstack.damageItem(1, playerIn);
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }
}