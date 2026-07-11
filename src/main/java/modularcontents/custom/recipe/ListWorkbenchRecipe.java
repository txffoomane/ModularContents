package modularcontents.custom.recipe;

import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ListWorkbenchRecipe {
    public String id;
    public String type = "workbench"; // "workbench", "handcraft", or "both"
    public String workbench = "custom_workbench"; // which workbench it belongs to (e.g. custom_workbench, metal_workbench)
    public String category = "general";

    // Legacy support
    public IngredientStack output;

    // New multiple outputs
    public List<IngredientStack> outputs;

    public List<IngredientStack> inputs;
    public int craftingTime = 200;
    public int minDrops = 0;
    public float movementModifier = 1.0f; // 1.0 = нормальная скорость, 0.0 = стоять на месте

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

    public List<ItemStack> rollResults(Random rand) {
        List<ItemStack> results = new ArrayList<>();
        if (outputs != null && !outputs.isEmpty()) {
            List<IngredientStack> missed = new ArrayList<>();
            for (IngredientStack stack : outputs) {
                if (stack == null) continue;
                ItemStack item = stack.toItemStack();
                if (item.isEmpty()) continue;
                if (stack.chance >= 100.0f || rand.nextFloat() * 100.0f < stack.chance) {
                    results.add(item);
                } else {
                    missed.add(stack);
                }
            }
            if (minDrops > 0 && results.size() < minDrops && !missed.isEmpty()) {
                missed.sort((a, b) -> Float.compare(b.chance, a.chance));
                for (IngredientStack stack : missed) {
                    if (results.size() >= minDrops) break;
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

    public boolean hasChanceOutputs() {
        if (outputs == null) return false;
        for (IngredientStack stack : outputs) {
            if (stack != null && stack.chance < 100.0f) return true;
        }
        return false;
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
