package modularcontents.custom.item;

import com.google.common.collect.Multimap;
import modularcontents.ModularcontentsMod;
import modularcontents.custom.tab.CustomTabManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustomWeapon extends ItemSword {

    public final CustomWeaponInfo info;

    public ItemCustomWeapon(CustomWeaponInfo info) {
        // Create a dummy material, we will override the modifiers anyway
        super(EnumHelper.addToolMaterial("weapon_" + info.id, 0, info.maxDamage, 1.0F, 0.0F, 0));
        this.info = info;

        this.setRegistryName(info.id);
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

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.removeAll(SharedMonsterAttributes.ATTACK_DAMAGE.getName());
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.info.attackDamage, 0));

            multimap.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.info.attackSpeed, 0));
        }
        return multimap;
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