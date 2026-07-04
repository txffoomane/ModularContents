package modularcontents.custom.recipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class IngredientStack {
    public String item;
    public int count = 1;
    public int meta = 0;
    public float chance = 100.0f;

    public ItemStack toItemStack() {
        if (item == null || item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try {
            ResourceLocation loc = new ResourceLocation(item);
            if (!Item.REGISTRY.containsKey(loc)) {
                return ItemStack.EMPTY;
            }

            Item mcItem = Item.REGISTRY.getObject(loc);
            if (mcItem == null) {
                return ItemStack.EMPTY;
            }

            return new ItemStack(mcItem, count, meta);
        } catch (Exception e) {
            // Prevent any malformed resource location from crashing the game
            return ItemStack.EMPTY;
        }
    }
}
