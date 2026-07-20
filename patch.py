import re

def process_file(filename):
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. Add fields
    content = re.sub(r'private GuiTextField searchField;', 
                     'private GuiTextField searchField;\n    private GuiTextField amountField;\n    private GuiButton btnVariantPrev;\n    private GuiButton btnVariantNext;', content)
    
    content = re.sub(r'private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<\(\)>;', 
                     'private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<>();\n    private List<List<ListWorkbenchRecipe>> currentRecipeGroups = new ArrayList<>();', content)
    
    content = re.sub(r'private int selectedRecipeIndex = -1;', 
                     'private int selectedRecipeIndex = -1;\n    private int selectedVariantIndex = 0;', content)

    # 2. updateCategoryRecipes
    def replace_update_cat(m):
        code = m.group(0)
        # add grouping logic at the end of updateCategoryRecipes
        grouping = """
        currentRecipeGroups.clear();
        Map<String, List<ListWorkbenchRecipe>> groups = new HashMap<>();
        for (ListWorkbenchRecipe r : currentRecipes) {
            ItemStack res = r.getPrimaryResult();
            String key = res.isEmpty() ? r.id : res.getDisplayName();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
        }
        
        // Sort groups by key
        List<String> sortedKeys = new ArrayList<>(groups.keySet());
        sortedKeys.sort(String::compareToIgnoreCase);
        
        for (String key : sortedKeys) {
            currentRecipeGroups.add(groups.get(key));
        }
        
        selectedRecipeIndex = -1;
        selectedVariantIndex = 0;
        craftAmount = 1;
        if (amountField != null) {
            amountField.setText("1");
        }
"""
        return code.replace('        }', '        }\n' + grouping)

    # Wait, currentRecipes = ... is assigned. Then we group it.
    
    # Let's do this differently. I'll just write a script to generate the fully patched file, then I will output it using the Write tool.
