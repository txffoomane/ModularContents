package modularcontents.custom.recipe;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ListWorkbenchRecipe {
    public String id;
    public String category = "general";

    // Legacy support
    public IngredientStack output;

    // New multiple outputs
    public List<IngredientStack> outputs;

    public List<IngredientStack> inputs;
    public int craftingTime = 200;

    public List<ItemStack> getResults() {
        List<ItemStack> results = new ArrayList<>();
        if (outputs != null && !outputs.isEmpty()) {
            for (IngredientStack stack : outputs) {
                if (stack != null) { // Null safety for malformed JSON arrays
                    ItemStack item = stack.toItemStack();
                    if (!item.isEmpty()) results.add(item);
                }
            }
        } else if (output != null) {
            ItemStack item = output.toItemStack();
            if (!item.isEmpty()) results.add(item);
        }
        return results;
    }

    public ItemStack getPrimaryResult() {
        List<ItemStack> results = getResults();
        if (!results.isEmpty()) {
            return results.get(0);
        }
        return ItemStack.EMPTY;
    }

    // Legacy getter just in case
    public ItemStack getResult() {
        return getPrimaryResult();
    }
}
