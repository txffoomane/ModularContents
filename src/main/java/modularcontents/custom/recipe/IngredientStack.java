package modularcontents.custom.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

public class IngredientStack {
    public String item;
    public int count = 1;
    public int meta = 0;
    public float chance = 100.0f;
    public String nbt;

    private transient NBTTagCompound parsedNbt;
    private transient boolean nbtParsed;

    private NBTTagCompound getParsedNbt() {
        if (!nbtParsed) {
            nbtParsed = true;
            if (nbt != null && !nbt.trim().isEmpty()) {
                try {
                    parsedNbt = JsonToNBT.getTagFromJson(nbt);
                } catch (Exception e) {
                    parsedNbt = null;
                }
            }
        }
        return parsedNbt;
    }

    public ItemStack toItemStack() {
        try {
            NBTTagCompound compound = getParsedNbt();

            if (compound != null && compound.hasKey("id", 8)) {
                ItemStack fullStack = new ItemStack(compound.copy());
                if (fullStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                return fullStack;
            }

            if (item == null || item.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ResourceLocation loc = new ResourceLocation(item);
            if (!Item.REGISTRY.containsKey(loc)) {
                return ItemStack.EMPTY;
            }

            Item mcItem = Item.REGISTRY.getObject(loc);
            if (mcItem == null) {
                return ItemStack.EMPTY;
            }

            ItemStack stack = new ItemStack(mcItem, count, meta);
            if (compound != null) {
                stack.setTagCompound(compound.copy());
            }
            return stack;
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ItemStack required = toItemStack();
        if (required.isEmpty()) return false;

        if (required.getItem() != stack.getItem()) return false;
        if (meta != OreDictionary.WILDCARD_VALUE && required.getMetadata() != stack.getMetadata()) return false;

        if (getParsedNbt() != null) {
            if (!NBTUtil.areNBTEquals(required.getTagCompound(), stack.getTagCompound(), true)) return false;
        }

        return true;
    }
}
