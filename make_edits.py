import json

def process(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    edits = []
    
    # 1. Add fields
    old1 = """    private int selectedRecipeIndex = -1;
    private int craftAmount = 1;"""
    new1 = """    private int selectedRecipeIndex = -1;
    private int selectedVariantIndex = 0;
    private int craftAmount = 1;
    private GuiTextField amountField;
    private GuiButton btnVariantPrev;
    private GuiButton btnVariantNext;"""
    edits.append({"file_path": filepath, "old_string": old1, "new_string": new1})
    
    # 2. Add currentRecipeGroups
    old2 = """    private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<>();"""
    new2 = """    private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<>();
    private List<List<ListWorkbenchRecipe>> currentRecipeGroups = new ArrayList<>();"""
    edits.append({"file_path": filepath, "old_string": old2, "new_string": new2})

    # 3. initGui buttons
    old3 = """        btnMinus = new FlatButton(8, guiLeft + 140, guiTop + 122, 14, 14, "-");
        btnPlus = new FlatButton(9, guiLeft + 184, guiTop + 122, 14, 14, "+");"""
    new3 = """        btnMinus = new FlatButton(8, guiLeft + 140, guiTop + 122, 14, 14, "-");
        btnPlus = new FlatButton(9, guiLeft + 184, guiTop + 122, 14, 14, "+");
        btnVariantPrev = new FlatButton(11, guiLeft + 192, guiTop + 45, 12, 12, "<");
        btnVariantNext = new FlatButton(12, guiLeft + 236, guiTop + 45, 12, 12, ">");"""
    edits.append({"file_path": filepath, "old_string": old3, "new_string": new3})

    old3b = """        this.buttonList.add(btnPlus);
        this.buttonList.add(btnViewMode);"""
    new3b = """        this.buttonList.add(btnPlus);
        this.buttonList.add(btnVariantPrev);
        this.buttonList.add(btnVariantNext);
        this.buttonList.add(btnViewMode);"""
    edits.append({"file_path": filepath, "old_string": old3b, "new_string": new3b})

    old3c = """        searchField.setTextColor(16777215);"""
    new3c = """        searchField.setTextColor(16777215);

        amountField = new GuiTextField(13, this.fontRenderer, guiLeft + 158, guiTop + 125, 22, 10);
        amountField.setMaxStringLength(2);
        amountField.setEnableBackgroundDrawing(false);
        amountField.setTextColor(0xFFFFFFFF);
        amountField.setText(String.valueOf(craftAmount));"""
    edits.append({"file_path": filepath, "old_string": old3c, "new_string": new3c})

    # 4. updateScreen (btn visibility)
    # This might be slightly different between the two files, but similar.
    # We will just write a python script to do all replacements properly and then print out the Edit JSONs.
