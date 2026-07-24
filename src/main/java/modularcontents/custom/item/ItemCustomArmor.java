package modularcontents.custom.item;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.tab.CustomTabManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustomArmor extends ItemArmor {

    public final CustomArmorInfo info;

    public ItemCustomArmor(CustomArmorInfo info, EntityEquipmentSlot slot) {
        super(createMaterial(info), 0, slot);
        this.info = info;

        this.setRegistryName("modularcontents", info.id);
        this.setUnlocalizedName("custom." + info.id);
        this.setMaxStackSize(info.maxStackSize);

        if (info.maxDamage > 0) {
            this.setMaxDamage(info.maxDamage);
        }

        boolean foundTab = false;
        if (CustomTabManager.CUSTOM_TABS.containsKey(info.creativeTab)) {
            this.setCreativeTab(CustomTabManager.CUSTOM_TABS.get(info.creativeTab));
            foundTab = true;
        } else {
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

    private static ArmorMaterial createMaterial(CustomArmorInfo info) {
        String name = "custom_" + info.id;
        String textureName = info.armorTexture != null && !info.armorTexture.isEmpty() ? info.armorTexture : "modularcontents:" + info.id;
        SoundEvent soundEvent = net.minecraft.init.SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;

        int[] reductionAmounts = new int[]{info.damageReduction, info.damageReduction, info.damageReduction, info.damageReduction};
        return EnumHelper.addArmorMaterial(name, textureName, info.maxDamage, reductionAmounts, info.enchantability, soundEvent, info.toughness);
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

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (info.displayName != null && !info.displayName.isEmpty()) {
            return info.displayName;
        }
        return super.getItemStackDisplayName(stack);
    }
}