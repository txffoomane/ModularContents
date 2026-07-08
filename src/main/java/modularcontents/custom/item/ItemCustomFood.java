package modularcontents.custom.item;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustomFood extends ItemFood {

    private final CustomFoodInfo info;

    public ItemCustomFood(CustomFoodInfo info) {
        super(info.healAmount, info.saturation, info.isMeat);
        this.info = info;

        this.setRegistryName(info.id);
        this.setUnlocalizedName(info.id);
        this.setMaxStackSize(info.maxStackSize);

        if (info.alwaysEdible) {
            this.setAlwaysEdible();
        }

        if (info.potionEffect != null && !info.potionEffect.isEmpty()) {
            Potion potion = Potion.getPotionFromResourceLocation(info.potionEffect);
            if (potion != null) {
                this.setPotionEffect(new PotionEffect(potion, info.potionDuration, info.potionAmplifier), info.potionProbability);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (info.description != null) {
            for (String line : info.description) {
                tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + line);
            }
        }
        if (I18n.hasKey("item." + info.id + ".tooltip")) {
            tooltip.add(I18n.format("item." + info.id + ".tooltip"));
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