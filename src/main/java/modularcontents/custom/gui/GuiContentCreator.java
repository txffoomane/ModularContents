package modularcontents.custom.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import modularcontents.custom.inventory.ContainerContentCreator;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GuiContentCreator extends GuiContainer {

    private static final int COL_ACCENT = 0xFFFFAA00;
    private static final int COL_BORDER = 0xFF4A4A4A;
    private static final int COL_BORDER_DARK = 0xFF2A2A2A;
    private static final int COL_PANEL_L = 0xFF151515;
    private static final int COL_PANEL_R = 0xFF18181A;
    private static final int COL_SLOT_BG = 0xFF111111;
    private static final int COL_TEXT = 0xFFDDDDDD;
    private static final int COL_TEXT_DIM = 0xFF888888;
    private static final int COL_LINE = 0xFF333333;
    private static final int COL_OUTPUT_BG = 0xFF221111;
    private static final int COL_GREEN = 0xFF55DD55;
    private static final int COL_RED = 0xFFFF5555;

    private static final int TAB_LOOT = 0;
    private static final int TAB_ITEMS = 1;
    private static final int TAB_RECIPES = 2;
    private static final int TAB_TABS = 4;
    private static final int TAB_ZONE = 6;

    private GuiButton btnTabLoot;
    private GuiButton btnTabItems;
    private GuiButton btnTabRecipes;
    private GuiButton btnTabTabs;
    private GuiButton btnTabZone;
    private GuiButton btnGenerate;
    private GuiButton btnNbtToggle;
    private GuiButton btnOpenMap;

    private GuiTextField txtFileName;

    private GuiTextField txtWeight;
    private GuiTextField txtItemMin;
    private GuiTextField txtItemMax;
    private GuiTextField txtItemChance;

    private GuiTextField txtItemName;
    private GuiTextField txtMaxStack;
    private GuiTextField txtCreativeTab;
    private GuiTextField txtMaxDamage;

    private GuiTextField txtRecipeCat;
    private GuiTextField txtCraftTime;
    private GuiTextField txtMinDrops;
    private GuiTextField txtRecipeChance;
    private GuiTextField txtRecipeNbt;

    private GuiTextField txtTabName;
    private GuiTextField txtTabIcon;

    private final ContainerContentCreator container;
    private int selectedSlot = -1;
    private final ItemSettings[] slotSettings = new ItemSettings[27];
    private final RecipeSlotSettings[] recipeSettings = new RecipeSlotSettings[27];

    private static class ItemSettings {
        double chance = 0.5;
        int min = 1;
        int max = 1;
        boolean customized = false;
    }

    private static class RecipeSlotSettings {
        float chance = 100.0f;
        boolean useNbt = false;
        String nbt = "";
        boolean touched = false;
    }

    public GuiContentCreator(InventoryPlayer playerInv) {
        super(new ContainerContentCreator(playerInv));
        this.container = (ContainerContentCreator) this.inventorySlots;
        this.xSize = 338;
        this.ySize = 240;
        for (int i = 0; i < 27; i++) {
            slotSettings[i] = new ItemSettings();
            recipeSettings[i] = new RecipeSlotSettings();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        Keyboard.enableRepeatEvents(true);

        this.btnTabLoot = new GuiLaptop.FlatButton(0, guiLeft + 8, guiTop + 6, 50, 14, tr("tab.loot"));
        this.btnTabItems = new GuiLaptop.FlatButton(1, guiLeft + 60, guiTop + 6, 50, 14, tr("tab.items"));
        this.btnTabRecipes = new GuiLaptop.FlatButton(2, guiLeft + 112, guiTop + 6, 50, 14, tr("tab.recipes"));
        this.btnTabTabs = new GuiLaptop.FlatButton(4, guiLeft + 164, guiTop + 6, 50, 14, tr("tab.tabs"));
        this.btnTabZone = new GuiLaptop.FlatButton(6, guiLeft + 216, guiTop + 6, 50, 14, tr("tab.zone"));
        this.btnGenerate = new GuiLaptop.FlatButton(3, guiLeft + 198, guiTop + 138, 130, 14, tr("generate"));
        this.btnNbtToggle = new GuiLaptop.FlatButton(5, guiLeft + 70, guiTop + 134, 60, 14, tr("nbt.off"));
        this.btnOpenMap = new GuiLaptop.FlatButton(7, guiLeft + 40, guiTop + 80, 110, 18, tr("open_map"));

        this.buttonList.add(btnTabLoot);
        this.buttonList.add(btnTabItems);
        this.buttonList.add(btnTabRecipes);
        this.buttonList.add(btnTabTabs);
        this.buttonList.add(btnTabZone);
        this.buttonList.add(btnGenerate);
        this.buttonList.add(btnNbtToggle);
        this.buttonList.add(btnOpenMap);

        this.txtFileName = createField(10, 198, 37, 130, 32, "my_file");

        this.txtWeight = createField(11, 198, 65, 50, 4, "50");

        this.txtItemMin = createField(12, 15, 136, 30, 3, "1");
        this.txtItemMax = createField(13, 55, 136, 30, 3, "1");
        this.txtItemChance = createField(14, 95, 136, 55, 5, "0.5");

        this.txtItemName = createField(15, 198, 65, 130, 64, "My Custom Item");
        this.txtMaxStack = createField(16, 198, 93, 40, 2, "64");
        this.txtMaxDamage = createField(18, 258, 93, 70, 5, "0");
        this.txtCreativeTab = createField(17, 198, 121, 130, 32, "misc");

        this.txtRecipeCat = createField(19, 198, 65, 130, 32, "general");
        this.txtCraftTime = createField(20, 198, 93, 50, 5, "200");
        this.txtMinDrops = createField(21, 258, 93, 70, 5, "1");
        this.txtRecipeChance = createField(24, 15, 136, 40, 5, "100");
        this.txtRecipeNbt = createField(25, 198, 121, 130, 32000, "");

        this.txtTabName = createField(22, 198, 65, 130, 32, "My Custom Tab");
        this.txtTabIcon = createField(23, 198, 93, 130, 64, "minecraft:diamond_sword");

        updateTabState();
    }

    private static String tr(String key, Object... args) {
        return I18n.format("modularcontents.creator." + key, args);
    }

    private GuiTextField createField(int id, int x, int y, int width, int maxLength, String text) {
        GuiTextField field = new GuiTextField(id, fontRenderer, guiLeft + x, guiTop + y, width, 12);
        field.setMaxStringLength(maxLength);
        field.setText(text);
        return field;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiTextField field : allFields()) {
            field.updateCursorCounter();
        }
    }

    private GuiTextField[] allFields() {
        return new GuiTextField[]{txtFileName, txtWeight, txtItemMin, txtItemMax, txtItemChance,
                txtItemName, txtMaxStack, txtCreativeTab, txtMaxDamage,
                txtRecipeCat, txtCraftTime, txtMinDrops, txtRecipeChance, txtRecipeNbt,
                txtTabName, txtTabIcon};
    }

    private void updateTabState() {
        int tab = container.activeTab;
        btnTabLoot.enabled = tab != TAB_LOOT;
        btnTabItems.enabled = tab != TAB_ITEMS;
        btnTabRecipes.enabled = tab != TAB_RECIPES;
        btnTabTabs.enabled = tab != TAB_TABS;
        btnTabZone.enabled = tab != TAB_ZONE;

        boolean isLoot = tab == TAB_LOOT;
        boolean isItem = tab == TAB_ITEMS;
        boolean isRecp = tab == TAB_RECIPES;
        boolean isTabs = tab == TAB_TABS;
        boolean isZone = tab == TAB_ZONE;

        btnGenerate.visible = !isZone;
        btnOpenMap.visible = isZone;

        txtFileName.setVisible(!isZone);
        txtWeight.setVisible(isLoot);
        txtItemName.setVisible(isItem);
        txtMaxStack.setVisible(isItem);
        txtCreativeTab.setVisible(isItem);
        txtMaxDamage.setVisible(isItem);
        txtRecipeCat.setVisible(isRecp);
        txtCraftTime.setVisible(isRecp);
        txtMinDrops.setVisible(isRecp);
        txtTabName.setVisible(isTabs);
        txtTabIcon.setVisible(isTabs);

        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (isLoot) {
                slot.yPos = 43 + (i / 9) * 18;
            } else if (isRecp) {
                slot.yPos = i < 9 ? 41 : 73 + (i / 9 - 1) * 18;
            } else {
                slot.yPos = -9999;
            }
        }

        selectedSlot = -1;
        updateSidePanel();
    }

    private void updateSidePanel() {
        boolean lootSel = container.activeTab == TAB_LOOT && selectedSlot != -1;
        txtItemMin.setVisible(lootSel);
        txtItemMax.setVisible(lootSel);
        txtItemChance.setVisible(lootSel);

        if (lootSel) {
            ItemSettings set = slotSettings[selectedSlot];
            txtItemChance.setText(String.valueOf(set.chance));
            txtItemMin.setText(String.valueOf(set.min));

            Slot slot = container.inventorySlots.get(selectedSlot);
            if (slot.getHasStack() && !set.customized) {
                txtItemMax.setText(String.valueOf(slot.getStack().getCount()));
            } else {
                txtItemMax.setText(String.valueOf(set.max));
            }
        }

        boolean recpSel = container.activeTab == TAB_RECIPES && selectedSlot != -1 && getSelectedStack() != null;
        boolean recpOutSel = recpSel && selectedSlot < 9;

        txtRecipeChance.setVisible(recpOutSel);
        txtRecipeNbt.setVisible(recpSel);
        btnNbtToggle.visible = recpSel;

        if (recpSel) {
            RecipeSlotSettings set = recipeSettings[selectedSlot];
            ItemStack stack = getSelectedStack();
            if (!set.touched && stack.hasTagCompound()) {
                set.nbt = stack.getTagCompound().toString();
                set.useNbt = true;
                set.touched = true;
            }
            txtRecipeChance.setText(formatChance(set.chance));
            txtRecipeNbt.setText(set.nbt);
            btnNbtToggle.displayString = set.useNbt ? tr("nbt.on") : tr("nbt.off");
        }
    }

    private String formatChance(float chance) {
        if (chance == (int) chance) return String.valueOf((int) chance);
        return String.valueOf(chance);
    }

    private ItemStack getSelectedStack() {
        if (selectedSlot < 0 || selectedSlot >= 27) return null;
        Slot slot = container.inventorySlots.get(selectedSlot);
        return slot.getHasStack() ? slot.getStack() : null;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    private void drawSlotBox(int x, int y, int bgColor, boolean selected) {
        drawRect(x, y, x + 18, y + 18, selected ? COL_ACCENT : 0xFF3A3A3A);
        drawRect(x + 1, y + 1, x + 17, y + 17, bgColor);
        drawRect(x + 1, y + 1, x + 17, y + 2, 0x88000000);
        drawRect(x + 1, y + 1, x + 2, y + 17, 0x88000000);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawRect(guiLeft + 3, guiTop + 3, guiLeft + 335, guiTop + 155, COL_BORDER);
        drawRect(guiLeft + 4, guiTop + 4, guiLeft + 190, guiTop + 154, COL_PANEL_L);
        drawRect(guiLeft + 192, guiTop + 4, guiLeft + 334, guiTop + 154, COL_PANEL_R);
        drawRect(guiLeft + 4, guiTop + 4, guiLeft + 334, guiTop + 24, COL_PANEL_L);
        drawRect(guiLeft + 4, guiTop + 24, guiLeft + 334, guiTop + 25, COL_LINE);

        String title = tr("title");
        fontRenderer.drawStringWithShadow(title, guiLeft + 328 - fontRenderer.getStringWidth(title), guiTop + 9, COL_ACCENT);

        drawRect(guiLeft + 79, guiTop + 154, guiLeft + 259, guiTop + 238, COL_BORDER);
        drawRect(guiLeft + 80, guiTop + 155, guiLeft + 258, guiTop + 237, COL_PANEL_R);
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                drawSlotBox(guiLeft + 87 + j * 18, guiTop + 157 + i * 18, COL_SLOT_BG, false);
            }
        }
        for (int k = 0; k < 9; ++k) {
            drawSlotBox(guiLeft + 87 + k * 18, guiTop + 215, COL_SLOT_BG, false);
        }

        int tab = container.activeTab;
        if (tab == TAB_LOOT) {
            drawLootTab();
        } else if (tab == TAB_ITEMS) {
            drawItemsTab();
        } else if (tab == TAB_RECIPES) {
            drawRecipesTab();
        } else if (tab == TAB_TABS) {
            drawTabsTab();
        } else if (tab == TAB_ZONE) {
            drawZoneTab();
        }

        for (GuiTextField field : allFields()) {
            if (field.getVisible()) field.drawTextBox();
        }
    }

    private void drawLootTab() {
        fontRenderer.drawString(tr("header.loot"), guiLeft + 14, guiTop + 31, COL_ACCENT);
        for (int i = 0; i < 27; ++i) {
            drawSlotBox(guiLeft + 14 + (i % 9) * 18, guiTop + 42 + (i / 9) * 18, COL_SLOT_BG, i == selectedSlot);
        }

        drawSelectedBox();
        if (selectedSlot != -1) {
            ItemStack stack = getSelectedStack();
            String header = tr("slot", selectedSlot, stack != null ? stack.getDisplayName() : tr("empty"));
            fontRenderer.drawString(fontRenderer.trimStringToWidth(header, 166), guiLeft + 14, guiTop + 116, COL_ACCENT);
            fontRenderer.drawString(tr("label.min"), guiLeft + 15, guiTop + 127, COL_TEXT_DIM);
            fontRenderer.drawString(tr("label.max"), guiLeft + 55, guiTop + 127, COL_TEXT_DIM);
            fontRenderer.drawString(tr("label.chance01"), guiLeft + 95, guiTop + 127, COL_TEXT_DIM);
        } else {
            fontRenderer.drawString(tr("hint.loot.1"), guiLeft + 14, guiTop + 122, COL_TEXT_DIM);
            fontRenderer.drawString(tr("hint.loot.2"), guiLeft + 14, guiTop + 133, COL_TEXT_DIM);
        }

        drawRightLabel(tr("label.file_name"), 28);
        drawRightLabel(tr("label.weight"), 56);
        drawRightInfo(infoLines("info.loot", 5), 86);
    }

    private void drawItemsTab() {
        fontRenderer.drawString(tr("header.item"), guiLeft + 14, guiTop + 31, COL_ACCENT);
        drawLeftInfo(infoLines("info.items", 10));

        drawRightLabel(tr("label.item_id"), 28);
        drawRightLabel(tr("label.display_name"), 56);
        drawRightLabel(tr("label.max_stack"), 84);
        fontRenderer.drawString(tr("label.durability"), guiLeft + 258, guiTop + 84, COL_TEXT_DIM);
        drawRightLabel(tr("label.creative_tab"), 112);
    }

    private void drawRecipesTab() {
        fontRenderer.drawString(tr("header.outputs"), guiLeft + 14, guiTop + 29, COL_ACCENT);
        for (int i = 0; i < 9; ++i) {
            drawSlotBox(guiLeft + 14 + i * 18, guiTop + 40, COL_OUTPUT_BG, i == selectedSlot);
        }
        fontRenderer.drawString(tr("header.inputs"), guiLeft + 14, guiTop + 61, COL_ACCENT);
        for (int i = 9; i < 27; ++i) {
            drawSlotBox(guiLeft + 14 + (i % 9) * 18, guiTop + 72 + (i / 9 - 1) * 18, COL_SLOT_BG, i == selectedSlot);
        }

        drawSelectedBox();
        ItemStack stack = getSelectedStack();
        if (selectedSlot != -1 && stack != null) {
            String kind = selectedSlot < 9 ? tr("output") : tr("input");
            String header = kind + ": " + stack.getDisplayName() + " x" + stack.getCount();
            fontRenderer.drawString(fontRenderer.trimStringToWidth(header, 168), guiLeft + 14, guiTop + 116, COL_ACCENT);
            if (selectedSlot < 9) {
                fontRenderer.drawString(tr("label.chance_pct"), guiLeft + 15, guiTop + 127, COL_TEXT_DIM);
            }
            fontRenderer.drawString(tr("label.item_nbt"), guiLeft + 136, guiTop + 127, COL_TEXT_DIM);
            boolean hasNbt = stack.hasTagCompound();
            fontRenderer.drawString(hasNbt ? tr("yes") : tr("no"), guiLeft + 136, guiTop + 138, hasNbt ? COL_GREEN : COL_RED);
        } else {
            fontRenderer.drawString(tr("hint.recipe.1"), guiLeft + 14, guiTop + 122, COL_TEXT_DIM);
            fontRenderer.drawString(tr("hint.recipe.2"), guiLeft + 14, guiTop + 133, COL_TEXT_DIM);
        }

        drawRightLabel(tr("label.recipe_id"), 28);
        drawRightLabel(tr("label.category"), 56);
        drawRightLabel(tr("label.craft_ticks"), 84);
        fontRenderer.drawString(tr("label.min_drops"), guiLeft + 258, guiTop + 84, COL_TEXT_DIM);
        if (txtRecipeNbt.getVisible()) {
            drawRightLabel(tr("label.nbt"), 112);
        }
    }

    private void drawTabsTab() {
        fontRenderer.drawString(tr("header.tab"), guiLeft + 14, guiTop + 31, COL_ACCENT);
        drawLeftInfo(infoLines("info.tabs", 10));

        drawRightLabel(tr("label.tab_id"), 28);
        drawRightLabel(tr("label.display_name"), 56);
        drawRightLabel(tr("label.icon"), 84);
    }

    private void drawZoneTab() {
        fontRenderer.drawString(tr("header.zone"), guiLeft + 14, guiTop + 31, COL_ACCENT);
        drawLeftInfo(infoLines("info.zone", 3));
        drawRightInfo(infoLines("info.zone.right", 8), 30);
    }

    private String[] infoLines(String key, int count) {
        String[] lines = new String[count];
        for (int i = 0; i < count; i++) {
            String full = "modularcontents.creator." + key + "." + (i + 1);
            String value = I18n.format(full);
            lines[i] = value.equals(full) ? "" : value;
        }
        return lines;
    }

    private void drawRightLabel(String text, int y) {
        fontRenderer.drawString(text, guiLeft + 198, guiTop + y, COL_TEXT_DIM);
    }

    private void drawLeftInfo(String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            fontRenderer.drawString(lines[i], guiLeft + 14, guiTop + 44 + i * 11, COL_TEXT_DIM);
        }
    }

    private void drawRightInfo(String[] lines, int y) {
        for (int i = 0; i < lines.length; i++) {
            fontRenderer.drawString(lines[i], guiLeft + 198, guiTop + y + i * 10, COL_TEXT_DIM);
        }
    }

    private void drawSelectedBox() {
        drawRect(guiLeft + 9, guiTop + 111, guiLeft + 185, guiTop + 152, COL_BORDER_DARK);
        drawRect(guiLeft + 10, guiTop + 112, guiLeft + 184, guiTop + 151, COL_SLOT_BG);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for (GuiTextField field : allFields()) {
            if (field.getVisible()) field.mouseClicked(mouseX, mouseY, mouseButton);
        }

        int tab = container.activeTab;
        if (tab == TAB_LOOT || tab == TAB_RECIPES) {
            for (int i = 0; i < 27; i++) {
                Slot slot = container.inventorySlots.get(i);
                if (slot.yPos < 0) continue;
                int sx = guiLeft + slot.xPos - 1;
                int sy = guiTop + slot.yPos - 1;
                if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                    if (selectedSlot != i) {
                        selectedSlot = i;
                        updateSidePanel();
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (txtFileName.textboxKeyTyped(typedChar, keyCode)) return;

        int tab = container.activeTab;
        if (tab == TAB_LOOT) {
            if (txtWeight.textboxKeyTyped(typedChar, keyCode)) return;
            if (selectedSlot != -1) {
                boolean changed = txtItemMin.textboxKeyTyped(typedChar, keyCode)
                        || txtItemMax.textboxKeyTyped(typedChar, keyCode)
                        || txtItemChance.textboxKeyTyped(typedChar, keyCode);
                if (changed) {
                    ItemSettings set = slotSettings[selectedSlot];
                    set.customized = true;
                    try { set.min = Integer.parseInt(txtItemMin.getText()); } catch (Exception ignored) {}
                    try { set.max = Integer.parseInt(txtItemMax.getText()); } catch (Exception ignored) {}
                    try { set.chance = Double.parseDouble(txtItemChance.getText()); } catch (Exception ignored) {}
                    return;
                }
            }
        } else if (tab == TAB_ITEMS) {
            if (txtItemName.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtMaxStack.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtCreativeTab.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtMaxDamage.textboxKeyTyped(typedChar, keyCode)) return;
        } else if (tab == TAB_RECIPES) {
            if (txtRecipeCat.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtCraftTime.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtMinDrops.textboxKeyTyped(typedChar, keyCode)) return;
            if (selectedSlot != -1) {
                boolean changed = (txtRecipeChance.getVisible() && txtRecipeChance.textboxKeyTyped(typedChar, keyCode))
                        || (txtRecipeNbt.getVisible() && txtRecipeNbt.textboxKeyTyped(typedChar, keyCode));
                if (changed) {
                    RecipeSlotSettings set = recipeSettings[selectedSlot];
                    set.touched = true;
                    try { set.chance = Float.parseFloat(txtRecipeChance.getText()); } catch (Exception ignored) {}
                    set.nbt = txtRecipeNbt.getText();
                    return;
                }
            }
        } else if (tab == TAB_TABS) {
            if (txtTabName.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtTabIcon.textboxKeyTyped(typedChar, keyCode)) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 || button.id == 1 || button.id == 2 || button.id == 4 || button.id == 6) {
            container.activeTab = button.id;
            updateTabState();
        } else if (button.id == 3) {
            if (container.activeTab == TAB_LOOT) generateLootTable();
            else if (container.activeTab == TAB_ITEMS) generateItem();
            else if (container.activeTab == TAB_RECIPES) generateRecipe();
            else if (container.activeTab == TAB_TABS) generateTab();
        } else if (button.id == 7) {
            this.mc.displayGuiScreen(new GuiZoneEquipment());
        } else if (button.id == 5 && selectedSlot != -1) {
            RecipeSlotSettings set = recipeSettings[selectedSlot];
            set.useNbt = !set.useNbt;
            set.touched = true;
            ItemStack stack = getSelectedStack();
            if (set.useNbt && set.nbt.isEmpty() && stack != null && stack.hasTagCompound()) {
                set.nbt = stack.getTagCompound().toString();
                txtRecipeNbt.setText(set.nbt);
            }
            btnNbtToggle.displayString = set.useNbt ? tr("nbt.on") : tr("nbt.off");
        }
    }

    private void generateLootTable() {
        String fileName = txtFileName.getText().trim();
        if (fileName.isEmpty()) fileName = "custom_loot";
        if (!fileName.endsWith(".json")) fileName += ".json";

        int weight = 10;
        try { weight = Integer.parseInt(txtWeight.getText()); } catch (Exception ignored) {}

        JsonArray itemsArray = new JsonArray();
        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                ItemSettings set = slotSettings[i];

                int finalMax = set.customized ? set.max : stack.getCount();
                int finalMin = set.min;
                if (finalMax < finalMin) finalMax = finalMin;

                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("item", stack.getItem().getRegistryName().toString());
                if (stack.getMetadata() > 0) itemObj.addProperty("meta", stack.getMetadata());
                itemObj.addProperty("min", finalMin);
                itemObj.addProperty("max", finalMax);
                itemObj.addProperty("chance", set.chance);
                itemsArray.add(itemObj);
            }
        }

        if (itemsArray.size() == 0) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.RED + tr("msg.empty_loot")));
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("weight", weight);
        root.add("items", itemsArray);
        saveJsonFile(root, "loot_tables/airdrops", fileName);
    }

    private void generateItem() {
        String id = txtFileName.getText().trim();
        if (id.isEmpty()) id = "custom_item";
        String fileName = id;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("display_name", txtItemName.getText().trim());

        try { root.addProperty("max_stack_size", Integer.parseInt(txtMaxStack.getText())); }
        catch (Exception e) { root.addProperty("max_stack_size", 64); }

        root.addProperty("creative_tab", txtCreativeTab.getText().trim());

        try { root.addProperty("max_damage", Integer.parseInt(txtMaxDamage.getText())); }
        catch (Exception e) { root.addProperty("max_damage", 0); }

        saveJsonFile(root, "items", fileName);
    }

    private JsonObject buildRecipeStack(ItemStack stack, RecipeSlotSettings set, boolean isOutput) {
        JsonObject itemObj = new JsonObject();
        itemObj.addProperty("item", stack.getItem().getRegistryName().toString());
        itemObj.addProperty("count", stack.getCount());
        if (stack.getMetadata() > 0) itemObj.addProperty("meta", stack.getMetadata());
        if (isOutput) itemObj.addProperty("chance", set.chance);
        if (set.useNbt && !set.nbt.trim().isEmpty()) itemObj.addProperty("nbt", set.nbt.trim());
        return itemObj;
    }

    private void generateRecipe() {
        String fileName = txtFileName.getText().trim();
        if (fileName.isEmpty()) fileName = "custom_recipe";
        String id = fileName;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("category", txtRecipeCat.getText().trim());

        JsonArray outputsArray = new JsonArray();
        for (int i = 0; i < 9; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                outputsArray.add(buildRecipeStack(slot.getStack(), recipeSettings[i], true));
            }
        }

        JsonArray inputsArray = new JsonArray();
        for (int i = 9; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                inputsArray.add(buildRecipeStack(slot.getStack(), recipeSettings[i], false));
            }
        }

        if (inputsArray.size() == 0 || outputsArray.size() == 0) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.RED + tr("msg.recipe_needs")));
            return;
        }

        root.add("outputs", outputsArray);
        root.add("inputs", inputsArray);

        try { root.addProperty("craftingTime", Integer.parseInt(txtCraftTime.getText())); }
        catch (Exception e) { root.addProperty("craftingTime", 200); }

        try { root.addProperty("minDrops", Integer.parseInt(txtMinDrops.getText())); }
        catch (Exception e) { root.addProperty("minDrops", 1); }

        saveJsonFile(root, "recipes", fileName);
    }

    private void generateTab() {
        String id = txtFileName.getText().trim();
        if (id.isEmpty()) id = "custom_tab";
        String fileName = id;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("display_name", txtTabName.getText().trim());
        root.addProperty("icon", txtTabIcon.getText().trim());

        saveJsonFile(root, "tabs", fileName);
    }

    private void saveJsonFile(JsonObject root, String subDir, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(root);

        File gameDir = mc.mcDataDir;
        File genDir = new File(gameDir, "ModularContents/generated/" + subDir);
        genDir.mkdirs();
        File outFile = new File(genDir, fileName);

        try (FileWriter writer = new FileWriter(outFile)) {
            writer.write(json);
            mc.player.sendMessage(new TextComponentString(TextFormatting.GREEN + tr("msg.generated", outFile.getAbsolutePath())));
        } catch (IOException e) {
            mc.player.sendMessage(new TextComponentString(TextFormatting.RED + tr("msg.failed", e.getMessage())));
        }
    }
}
