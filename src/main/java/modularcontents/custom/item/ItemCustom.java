package modularcontents.custom.item;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.entity.EntityAirdrop;
import modularcontents.custom.entity.EntitySignalFlare;
import modularcontents.custom.config.ModularContentsConfig;
import modularcontents.custom.tab.CustomTabManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustom extends Item {

    public final CustomItemInfo info;

    public ItemCustom(CustomItemInfo info) {
        this.info = info;
        this.setRegistryName("modularcontents", info.id);
        this.setUnlocalizedName("custom." + info.id);
        this.setMaxStackSize(info.maxStackSize);
        if (info.maxDamage > 0) {
            this.setMaxDamage(info.maxDamage);
            this.setNoRepair(); // Optional: prevent combining in vanilla anvil if it's purely a crafting tool
        }

        // Creative Tab Logic
        boolean foundTab = false;

        // Check our custom tabs first
        if (CustomTabManager.CUSTOM_TABS.containsKey(info.creativeTab)) {
            this.setCreativeTab(CustomTabManager.CUSTOM_TABS.get(info.creativeTab));
            foundTab = true;
        } else {
            // Check vanilla/other mod tabs
            for (CreativeTabs tab : CreativeTabs.CREATIVE_TAB_ARRAY) {
                if (tab.getTabLabel().equalsIgnoreCase(info.creativeTab)) {
                    this.setCreativeTab(tab);
                    foundTab = true;
                    break;
                }
            }
        }

        if (!foundTab) {
            this.setCreativeTab(ModularcontentsMod.MODULAR_TAB);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (this.info.airdropCaller != null) {
            CustomItemInfo.AirdropCallerInfo callerInfo = this.info.airdropCaller;

            if (callerInfo.isFlare) {
                worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

                if (!worldIn.isRemote) {
                    EntitySignalFlare flare = new EntitySignalFlare(worldIn, playerIn);

                    // override fuse time if provided
                    int fuse = callerInfo.delayMin;
                    if (callerInfo.delayMax > callerInfo.delayMin) {
                        fuse += worldIn.rand.nextInt(callerInfo.delayMax - callerInfo.delayMin + 1);
                    }
                    flare.getEntityData().setInteger("Fuse", fuse); // Need to adjust flare to accept custom fuse but NBT works

                    // If custom loot table specified, save it for the flare to pass on (Requires minor EntitySignalFlare update)
                    if (callerInfo.lootTable != null && !callerInfo.lootTable.isEmpty()) {
                        flare.getEntityData().setString("LootTable", callerInfo.lootTable);
                    }

                    flare.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
                    worldIn.spawnEntity(flare);
                }
                playerIn.getCooldownTracker().setCooldown(this, ModularContentsConfig.airdropCallerCooldown);
            } else {
                if (!worldIn.isRemote) {
                    double distance = callerInfo.distanceMin;
                    if (callerInfo.distanceMax > callerInfo.distanceMin) {
                        distance += worldIn.rand.nextDouble() * (callerInfo.distanceMax - callerInfo.distanceMin);
                    }

                    double angle = worldIn.rand.nextDouble() * Math.PI * 2;
                    double targetX = playerIn.posX + Math.cos(angle) * distance;
                    double targetZ = playerIn.posZ + Math.sin(angle) * distance;
                    double targetY = 250.0D;

                    int delayTicks = callerInfo.delayMin;
                    if (callerInfo.delayMax > callerInfo.delayMin) {
                        delayTicks += worldIn.rand.nextInt(callerInfo.delayMax - callerInfo.delayMin + 1);
                    }

                    EntityAirdrop airdrop = new EntityAirdrop(worldIn, targetX, targetY, targetZ);
                    airdrop.setDelayAndCaller(delayTicks, playerIn.getName(), false, targetX, targetZ);

                    if (callerInfo.lootTable != null && !callerInfo.lootTable.isEmpty()) {
                        airdrop.setLootTable(callerInfo.lootTable);
                    }

                    worldIn.spawnEntity(airdrop);

                    int seconds = delayTicks / 20;
                    playerIn.sendMessage(new TextComponentString(TextFormatting.GREEN + "Airdrop requested! Coordinates: X: " + (int)targetX + ", Z: " + (int)targetZ + ". ETA: " + seconds + " seconds."));
                    playerIn.getCooldownTracker().setCooldown(this, ModularContentsConfig.airdropCallerCooldown);
                }
            }

            if (!playerIn.capabilities.isCreativeMode) {
                if (callerInfo.consumeOnUse) {
                    if (this.isDamageable()) {
                        itemstack.damageItem(1, playerIn);
                    } else {
                        itemstack.shrink(1);
                    }
                }
            }

            return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (info.description != null) {
            for (String line : info.description) {
                tooltip.add(TextFormatting.GRAY + line);
            }
        }
    }

    public String getItemStackDisplayName(ItemStack stack) {
        return info.displayName;
    }
}