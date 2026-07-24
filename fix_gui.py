import os
import re

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add isGroupCraftable
    if "private boolean isGroupCraftable" not in content:
        group_func = """
    private boolean isGroupCraftable(RecipeGroup group) {
        for (ListWorkbenchRecipe r : group.recipes) {
            if (isCraftable(r)) return true;
        }
        return false;
    }
"""
        content = content.replace("private boolean isCraftable(ListWorkbenchRecipe recipe) {", group_func.strip() + "\n\n    private boolean isCraftable(ListWorkbenchRecipe recipe) {")

    # Fix sorting
    content = content.replace("boolean ca = isCraftable(a.getCurrentRecipe());", "boolean ca = isGroupCraftable(a);")
    content = content.replace("boolean cb = isCraftable(b.getCurrentRecipe());", "boolean cb = isGroupCraftable(b);")

    # Fix drawing list
    content = re.sub(r'RecipeGroup group = currentGroups\.get\(index\);\s*ListWorkbenchRecipe recipe = group\.getCurrentRecipe\(\);\s*boolean craftable = isCraftable\(recipe\);', 
                     r'RecipeGroup group = currentGroups.get(index);\n            ListWorkbenchRecipe recipe = group.getCurrentRecipe();\n            boolean craftable = isGroupCraftable(group);', content)

    # Fix drawing grid (sometimes slightly different indentation)
    # Using a simpler replace
    if "boolean craftable = isCraftable(recipe);" in content:
        content = content.replace("boolean craftable = isCraftable(recipe);", "boolean craftable = isGroupCraftable(group);")
        # We need to ensure 'group' exists where this is called. It should, because the agent wrote it: 
        # RecipeGroup group = currentGroups.get(index);
        # ListWorkbenchRecipe recipe = group.getCurrentRecipe();
        # Let's verify by regex if the above simple replace caused issues where group is not defined.

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/gui/GuiListWorkbench.java")
fix_file("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/client/gui/GuiHandcraft.java")

