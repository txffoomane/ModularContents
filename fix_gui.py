import re

filepath = "D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/client/gui/GuiHandcraft.java"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

replacement = """    private int getMaxAffordable(ListWorkbenchRecipe recipe) {
        int max = 64;

        Map<String, Integer> requiredTotals = new HashMap<>();
        for (IngredientStack ing : recipe.inputs) {
            if (ing.count <= 0) continue;
            ItemStack stack = ing.toItemStack();
            if (stack.isEmpty()) continue;

            String key = stack.getItem().getRegistryName().toString() + ":" + ing.meta;
            requiredTotals.put(key, requiredTotals.getOrDefault(key, 0) + ing.count);
        }

        for (IngredientStack ing : recipe.inputs) {
            if (ing.count <= 0) continue;
            ItemStack stack = ing.toItemStack();
            if (stack.isEmpty()) continue;

            String key = stack.getItem().getRegistryName().toString() + ":" + ing.meta;
            int totalNeeded = requiredTotals.get(key);

            int have = countItemInInventory(ing);
            int affordable = have / totalNeeded;
            if (affordable < max) max = affordable;
        }

        if (max > 0) {
            boolean canFit = false;
            if (this.mc.player.inventory.getFirstEmptyStack() != -1) {
                canFit = true;
            } else {
                for (IngredientStack out : recipe.outputs) {
                    if (out.chance < 100.0f) continue;
                    ItemStack res = out.toItemStack();
                    if (!res.isEmpty()) {
                        for (ItemStack invStack : this.mc.player.inventory.mainInventory) {
                            if (!invStack.isEmpty() && invStack.isItemEqual(res) && ItemStack.areItemStackTagsEqual(invStack, res)) {
                                if (invStack.getCount() + res.getCount() <= invStack.getMaxStackSize()) {
                                    canFit = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!canFit) {
                max = 0;
            }
        }

        return max;
    }"""

pattern = r'    private int getMaxAffordable\(ListWorkbenchRecipe recipe\)\s*\{.*?(?=    private int countItemInInventory)'
content = re.sub(pattern, replacement + "\n\n", content, flags=re.DOTALL)

with open(filepath, "w", encoding="utf-8") as f:
    f.write(content)
