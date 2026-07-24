const fs = require('fs');

function fixFile(filepath) {
    let content = fs.readFileSync(filepath, 'utf8');

    if (!content.includes("private boolean isGroupCraftable")) {
        let groupFunc = `
    private boolean isGroupCraftable(RecipeGroup group) {
        for (ListWorkbenchRecipe r : group.recipes) {
            if (isCraftable(r)) return true;
        }
        return false;
    }
`;
        content = content.replace("private boolean isCraftable(ListWorkbenchRecipe recipe) {", groupFunc.trim() + "\n\n    private boolean isCraftable(ListWorkbenchRecipe recipe) {");
    }

    content = content.replace("boolean ca = isCraftable(a.getCurrentRecipe());", "boolean ca = isGroupCraftable(a);");
    content = content.replace("boolean cb = isCraftable(b.getCurrentRecipe());", "boolean cb = isGroupCraftable(b);");

    // Replace the boolean craftable calculation when drawing
    content = content.replace(/boolean craftable = isCraftable\(recipe\);/g, "boolean craftable = isGroupCraftable(group);");

    fs.writeFileSync(filepath, content, 'utf8');
}

fixFile("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/gui/GuiListWorkbench.java");
fixFile("D:/CONTRACT/modularcontents/src/main/java/modularcontents/custom/client/gui/GuiHandcraft.java");
