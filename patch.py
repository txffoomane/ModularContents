import re

def process_file(filepath, is_handcraft):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    group_class = """    public static class RecipeGroup {
        public java.util.List<modularcontents.custom.recipe.ListWorkbenchRecipe> recipes = new java.util.ArrayList<>();
        public int currentVariation = 0;
        public modularcontents.custom.recipe.ListWorkbenchRecipe getCurrentRecipe() {
            if (recipes.isEmpty()) return null;
            return recipes.get(currentVariation);
        }
    }
"""
    if "public static class RecipeGroup" not in content:
        content = content.replace("    private List<String> categories", group_class + "\n    private List<String> categories")

    content = content.replace("private List<ListWorkbenchRecipe> currentRecipes", "private List<RecipeGroup> currentGroups")
    content = content.replace("selectedRecipeIndex", "selectedGroupIndex")

    if "private GuiButton btnVarPrev;" not in content:
        content = content.replace("private GuiButton btnViewMode;", "private GuiButton btnViewMode;\n    private GuiButton btnVarPrev;\n    private GuiButton btnVarNext;")

    if "btnVarPrev = new FlatButton" not in content:
        content = content.replace("this.buttonList.add(btnViewMode);", "this.buttonList.add(btnViewMode);\n        btnVarPrev = new FlatButton(11, guiLeft + 258, guiTop + 8, 14, 14, \"<\");\n        btnVarNext = new FlatButton(12, guiLeft + 294, guiTop + 8, 14, 14, \">\");\n        this.buttonList.add(btnVarPrev);\n        this.buttonList.add(btnVarNext);")

    if "btnVarPrev.visible" not in content:
        content = content.replace("btnFavorite.visible = false;", "btnFavorite.visible = false;\n            btnVarPrev.visible = false;\n            btnVarNext.visible = false;")
        vis_logic = """
            RecipeGroup groupToShow = (selectedGroupIndex >= 0 && selectedGroupIndex < currentGroups.size()) ? currentGroups.get(selectedGroupIndex) : null;
            if (groupToShow != null && groupToShow.recipes.size() > 1) {
                btnVarPrev.visible = true;
                btnVarNext.visible = true;
                btnVarPrev.enabled = groupToShow.currentVariation > 0;
                btnVarNext.enabled = groupToShow.currentVariation < groupToShow.recipes.size() - 1;
            } else {
                btnVarPrev.visible = false;
                btnVarNext.visible = false;
            }
"""
        content = content.replace("btnFavorite.visible = true;", vis_logic + "\n            btnFavorite.visible = true;")

    if "varStr" not in content:
        var_text = """
            if (selectedGroupIndex >= 0 && selectedGroupIndex < currentGroups.size()) {
                RecipeGroup g = currentGroups.get(selectedGroupIndex);
                if (g.recipes.size() > 1) {
                    String varStr = (g.currentVariation + 1) + "/" + g.recipes.size();
                    int vw = this.fontRenderer.getStringWidth(varStr);
                    this.fontRenderer.drawString(varStr, rightX + 136 - vw / 2, guiTop + 11, COL_TEXT_DIM);
                }
            }
"""
        content = content.replace("String displayName = this.fontRenderer.trimStringToWidth(primaryResult.getDisplayName(), 130);", var_text + "\n                String displayName = this.fontRenderer.trimStringToWidth(primaryResult.getDisplayName(), 130);")

    if "button.id == 11" not in content:
        act_logic = """} else if (button.id == 11 && selectedGroupIndex >= 0 && selectedGroupIndex < currentGroups.size()) {
            RecipeGroup g = currentGroups.get(selectedGroupIndex);
            if (g.currentVariation > 0) g.currentVariation--;
        } else if (button.id == 12 && selectedGroupIndex >= 0 && selectedGroupIndex < currentGroups.size()) {
            RecipeGroup g = currentGroups.get(selectedGroupIndex);
            if (g.currentVariation < g.recipes.size() - 1) g.currentVariation++;
"""
        content = content.replace("} else if (button.id == 10)", act_logic + "        } else if (button.id == 10)")

    if "isGroupCraftable" not in content:
        group_craft = """
    private boolean isGroupCraftable(RecipeGroup group) {
        for (modularcontents.custom.recipe.ListWorkbenchRecipe r : group.recipes) {
            if (getMaxAffordable(r) > 0) return true;
        }
        return false;
    }
"""
        content = content.replace("private boolean isCraftable", group_craft + "\n    private boolean isCraftable")

    get_recipe = """private modularcontents.custom.recipe.ListWorkbenchRecipe getRecipeToShow() {
        if (selectedGroupIndex >= 0 && selectedGroupIndex < currentGroups.size()) {
            return currentGroups.get(selectedGroupIndex).getCurrentRecipe();
        }
"""
    if is_handcraft:
        get_recipe += """        if (container.activeRecipeId != null && !container.activeRecipeId.isEmpty()) {
            return modularcontents.custom.recipe.ListWorkbenchRecipeManager.getRecipe(container.activeRecipeId);
        }
        return null;
    }"""
    else:
        get_recipe += """        if (te.isCrafting()) {
            String activeId = te.getActiveRecipeId();
            if (activeId != null && !activeId.isEmpty()) {
                return modularcontents.custom.recipe.ListWorkbenchRecipeManager.getRecipe(activeId);
            }
        }
        return null;
    }"""

    content = re.sub(r'private ListWorkbenchRecipe getRecipeToShow\(\) \{.*?return null;\n    \}', get_recipe, content, flags=re.DOTALL)

    update_cat = """private void updateCategoryRecipes() {
        String currentCat = categories.get(currentCategoryIndex);
        java.util.List<modularcontents.custom.recipe.ListWorkbenchRecipe> rawRecipes;

        if (currentCat.equals("Favorites")) {
            rawRecipes = modularcontents.custom.recipe.ListWorkbenchRecipeManager.getAllRecipes().stream().filter(r -> r.type.equalsIgnoreCase("""" + ("handcraft" if is_handcraft else "workbench") + """") || r.type.equalsIgnoreCase("both"))\n"""
    if not is_handcraft:
        update_cat += """                    .filter(r -> r.workbench != null && r.workbench.equalsIgnoreCase(te.getWorkbenchId()))\n"""
    update_cat += """                    .filter(r -> FAVORITES.contains(r.id))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            rawRecipes = modularcontents.custom.recipe.ListWorkbenchRecipeManager.getRecipesInCategory(currentCat, """ + ("\"handcraft\"" if is_handcraft else "\"workbench\", te.getWorkbenchId()") + """);
        }

        if (searchField != null && !searchField.getText().trim().isEmpty()) {
            String search = searchField.getText().trim().toLowerCase();
            rawRecipes = rawRecipes.stream()
                    .filter(r -> {
                        net.minecraft.item.ItemStack res = r.getPrimaryResult();
                        return !res.isEmpty() && res.getDisplayName().toLowerCase().contains(search);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        java.util.Map<String, RecipeGroup> groupsMap = new java.util.LinkedHashMap<>();
        for (modularcontents.custom.recipe.ListWorkbenchRecipe r : rawRecipes) {
            net.minecraft.item.ItemStack res = r.getPrimaryResult();
            String key = res.isEmpty() ? r.id : res.getItem().getRegistryName().toString() + ":" + res.getItemDamage();
            if (!groupsMap.containsKey(key)) {
                groupsMap.put(key, new RecipeGroup());
            }
            groupsMap.get(key).recipes.add(r);
        }

        currentGroups = new java.util.ArrayList<>(groupsMap.values());

        currentGroups.sort((a, b) -> {
            boolean ca = isGroupCraftable(a);
            boolean cb = isGroupCraftable(b);
            if (ca != cb) return ca ? -1 : 1;
            net.minecraft.item.ItemStack ra = a.getCurrentRecipe().getPrimaryResult();
            net.minecraft.item.ItemStack rb = b.getCurrentRecipe().getPrimaryResult();
            String na = ra.isEmpty() ? "" : ra.getDisplayName();
            String nb = rb.isEmpty() ? "" : rb.getDisplayName();
            return na.compareToIgnoreCase(nb);
        });

        scrollPos = 0.0f;
        scrollTarget = 0.0f;
        reqScrollPos = 0.0f;
        reqScrollTarget = 0.0f;
        selectedGroupIndex = -1;
        craftAmount = 1;
    }"""

    content = re.sub(r'private void updateCategoryRecipes\(\) \{.*?scrollPos = 0\.0f;.*?craftAmount = 1;\n    \}', update_cat, content, flags=re.DOTALL)

    content = content.replace("currentRecipes.size()", "currentGroups.size()")
    content = content.replace("ListWorkbenchRecipe recipe = currentRecipes.get(index);", "RecipeGroup group = currentGroups.get(index);\n            modularcontents.custom.recipe.ListWorkbenchRecipe recipe = group.getCurrentRecipe();")
    content = content.replace("boolean craftable = isCraftable(recipe);", "boolean craftable = isGroupCraftable(group);")

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

process_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/client/gui/GuiHandcraft.java", True)
process_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/gui/GuiListWorkbench.java", False)
print("Patch successful!")
