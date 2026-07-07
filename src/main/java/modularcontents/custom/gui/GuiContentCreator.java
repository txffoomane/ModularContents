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
    private static final int TAB_NPC = 8;
    private static final int TAB_BLOCK = 9;
    private static final int TAB_FOOD = 10;

    private GuiButton btnTabLoot;
    private GuiButton btnTabItems;
    private GuiButton btnTabRecipes;
    private GuiButton btnTabTabs;
    private GuiButton btnTabZone;
    private GuiButton btnTabNpcs;
    private GuiButton btnTabBlocks;
    private GuiButton btnTabFood;

    private GuiButton btnTabUp;
    private GuiButton btnTabDown;
    private GuiButton[] tabButtons;
    private int tabScrollIndex = 0;
    private static final int VISIBLE_TABS = 7;

    private GuiButton btnGenerate;
    private GuiButton btnNbtToggle;
    private GuiButton btnOpenMap;

    private GuiTextField txtPackName;
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

    private GuiTextField txtNpcId;
    private GuiTextField txtNpcName;
    private GuiTextField txtNpcHealth;
    private GuiTextField txtNpcSpeed;
    private GuiTextField txtNpcDamage;
    private GuiTextField txtNpcFollow;
    private GuiTextField txtNpcShoot;
    private GuiTextField txtNpcTexture;

    private GuiTextField txtBlockHardness;
    private GuiTextField txtBlockResist;
    private GuiTextField txtBlockLight;
    private GuiTextField txtBlockTool;
    private GuiTextField txtBlockMat;
    private GuiTextField txtBlockHarvest;

    private GuiTextField txtFoodHeal;
    private GuiTextField txtFoodSat;
    private GuiTextField txtFoodMeat;
    private GuiTextField txtFoodAlways;
    private GuiTextField txtFoodEffect;
    private GuiTextField txtFoodDur;
    private GuiTextField txtFoodAmp;
    private GuiTextField txtFoodProb;

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

        this.btnTabLoot = new GuiLaptop.FlatButton(0, guiLeft + 338, guiTop + 20, 50, 14, "Loot");
        this.btnTabItems = new GuiLaptop.FlatButton(1, guiLeft + 338, guiTop + 20, 50, 14, "Items");
        this.btnTabBlocks = new GuiLaptop.FlatButton(9, guiLeft + 338, guiTop + 20, 50, 14, "Blocks");
        this.btnTabFood = new GuiLaptop.FlatButton(10, guiLeft + 338, guiTop + 20, 50, 14, "Food");
        this.btnTabRecipes = new GuiLaptop.FlatButton(2, guiLeft + 338, guiTop + 20, 50, 14, "Recipes");
        this.btnTabTabs = new GuiLaptop.FlatButton(4, guiLeft + 338, guiTop + 20, 50, 14, "Tabs");
        this.btnTabZone = new GuiLaptop.FlatButton(6, guiLeft + 338, guiTop + 20, 50, 14, "Zone");
        this.btnTabNpcs = new GuiLaptop.FlatButton(8, guiLeft + 338, guiTop + 20, 50, 14, "NPCs");

        this.tabButtons = new GuiButton[] {
            btnTabItems, btnTabBlocks, btnTabFood, btnTabLoot, btnTabRecipes, btnTabTabs, btnTabZone, btnTabNpcs
        };

        this.btnTabUp = new GuiLaptop.FlatButton(20, guiLeft + 338, guiTop + 4, 50, 12, "^");
        this.btnTabDown = new GuiLaptop.FlatButton(21, guiLeft + 338, guiTop + 140, 50, 12, "v");

        this.btnGenerate = new GuiLaptop.FlatButton(3, guiLeft + 198, guiTop + 138, 130, 14, tr("generate"));
        this.btnNbtToggle = new GuiLaptop.FlatButton(5, guiLeft + 70, guiTop + 134, 60, 14, tr("nbt.off"));
        this.btnOpenMap = new GuiLaptop.FlatButton(7, guiLeft + 40, guiTop + 80, 110, 18, tr("open_map"));

        for (GuiButton btn : tabButtons) {
            this.buttonList.add(btn);
        }
        this.buttonList.add(btnTabUp);
        this.buttonList.add(btnTabDown);

        this.buttonList.add(btnGenerate);
        this.buttonList.add(btnNbtToggle);
        this.buttonList.add(btnOpenMap);

        this.txtPackName = createField(9, 45, 6, 80, 14, "example_pack");
        this.txtFileName = createField(10, 198, 42, 130, 32, "my_file");

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

        this.txtNpcId = createField(26, 15, 45, 160, 32, "custom_bandit");
        this.txtNpcName = createField(27, 15, 73, 160, 32, "Bandit");
        this.txtNpcHealth = createField(28, 15, 101, 50, 5, "20.0");
        this.txtNpcSpeed = createField(29, 75, 101, 50, 5, "0.25");
        this.txtNpcDamage = createField(30, 135, 101, 40, 5, "2.0");
        this.txtNpcFollow = createField(31, 198, 45, 60, 5, "32.0");
        this.txtNpcShoot = createField(32, 268, 45, 60, 5, "16.0");
        this.txtNpcTexture = createField(33, 198, 73, 130, 64, "minecraft:textures/entity/steve.png");

        this.txtBlockHardness = createField(34, 14, 52, 45, 5, "1.5");
        this.txtBlockResist = createField(35, 75, 52, 45, 5, "10.0");
        this.txtBlockLight = createField(36, 14, 82, 45, 5, "0.0");
        this.txtBlockTool = createField(37, 75, 82, 60, 16, "pickaxe");
        this.txtBlockMat = createField(38, 14, 112, 60, 16, "rock");
        this.txtBlockHarvest = createField(39, 85, 112, 40, 2, "0");

        this.txtFoodHeal = createField(40, 14, 52, 40, 3, "4");
        this.txtFoodSat = createField(41, 75, 52, 48, 5, "0.3");
        this.txtFoodMeat = createField(42, 14, 82, 48, 5, "false");
        this.txtFoodAlways = createField(43, 75, 82, 48, 5, "false");
        this.txtFoodEffect = createField(44, 14, 112, 160, 64, "");
        this.txtFoodDur = createField(45, 14, 138, 40, 5, "100");
        this.txtFoodAmp = createField(46, 64, 138, 30, 2, "0");
        this.txtFoodProb = createField(47, 104, 138, 40, 5, "1.0");

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
        return new GuiTextField[]{txtPackName, txtFileName, txtWeight, txtItemMin, txtItemMax, txtItemChance,
                txtItemName, txtMaxStack, txtCreativeTab, txtMaxDamage,
                txtRecipeCat, txtCraftTime, txtMinDrops, txtRecipeChance, txtRecipeNbt,
                txtTabName, txtTabIcon, txtNpcId, txtNpcName, txtNpcHealth, txtNpcSpeed, txtNpcDamage, txtNpcFollow, txtNpcShoot, txtNpcTexture,
                txtBlockHardness, txtBlockResist, txtBlockLight, txtBlockTool, txtBlockMat, txtBlockHarvest,
                txtFoodHeal, txtFoodSat, txtFoodMeat, txtFoodAlways, txtFoodEffect, txtFoodDur, txtFoodAmp, txtFoodProb};
    }

    private void updateTabState() {
        int tab = container.activeTab;

        // Dynamic vertical layout for tab buttons
        btnTabUp.visible = tabScrollIndex > 0;
        btnTabDown.visible = tabScrollIndex + VISIBLE_TABS < tabButtons.length;

        for (int i = 0; i < tabButtons.length; i++) {
            GuiButton btn = tabButtons[i];
            if (i >= tabScrollIndex && i < tabScrollIndex + VISIBLE_TABS) {
                btn.visible = true;
                btn.y = guiTop + 20 + ((i - tabScrollIndex) * 17);
            } else {
                btn.visible = false;
            }
            btn.enabled = btn.id != tab;
        }

        boolean isLoot = tab == TAB_LOOT;
        boolean isItem = tab == TAB_ITEMS;
        boolean isRecp = tab == TAB_RECIPES;
        boolean isTabs = tab == TAB_TABS;
        boolean isZone = tab == TAB_ZONE;
        boolean isNpc = tab == TAB_NPC;
        boolean isBlock = tab == TAB_BLOCK;
        boolean isFood = tab == TAB_FOOD;

        btnGenerate.visible = !isZone;
        btnOpenMap.visible = isZone;

        txtPackName.setVisible(true); // Always show pack name for all tabs
        txtFileName.setVisible(!isZone && !isNpc); // NPC uses its own ID field
        txtWeight.setVisible(isLoot);
        txtItemName.setVisible(isItem || isBlock || isFood);
        txtMaxStack.setVisible(isItem || isFood);
        txtCreativeTab.setVisible(isItem || isBlock || isFood);
        txtMaxDamage.setVisible(isItem);
        txtRecipeCat.setVisible(isRecp);
        txtCraftTime.setVisible(isRecp);
        txtMinDrops.setVisible(isRecp);
        txtTabName.setVisible(isTabs);
        txtTabIcon.setVisible(isTabs);

        txtNpcId.setVisible(isNpc);
        txtNpcName.setVisible(isNpc);
        txtNpcHealth.setVisible(isNpc);
        txtNpcSpeed.setVisible(isNpc);
        txtNpcDamage.setVisible(isNpc);
        txtNpcFollow.setVisible(isNpc);
        txtNpcShoot.setVisible(isNpc);
        txtNpcTexture.setVisible(isNpc);

        txtBlockHardness.setVisible(isBlock);
        txtBlockResist.setVisible(isBlock);
        txtBlockLight.setVisible(isBlock);
        txtBlockTool.setVisible(isBlock);
        txtBlockMat.setVisible(isBlock);
        txtBlockHarvest.setVisible(isBlock);

        txtFoodHeal.setVisible(isFood);
        txtFoodSat.setVisible(isFood);
        txtFoodMeat.setVisible(isFood);
        txtFoodAlways.setVisible(isFood);
        txtFoodEffect.setVisible(isFood);
        txtFoodDur.setVisible(isFood);
        txtFoodAmp.setVisible(isFood);
        txtFoodProb.setVisible(isFood);

        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (isLoot) {
                slot.yPos = 43 + (i / 9) * 18;
                slot.xPos = 14 + (i % 9) * 18;
            } else if (isRecp) {
                slot.yPos = i < 9 ? 41 : 73 + (i / 9 - 1) * 18;
                slot.xPos = 14 + (i % 9) * 18;
            } else if (isNpc && i < 6) { // 6 equipment slots for NPC
                slot.yPos = 31 + (i * 19);
                slot.xPos = -21;
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

        if (txtPackName.getVisible()) {
            fontRenderer.drawString("Pack:", guiLeft + 14, guiTop + 6, COL_TEXT_DIM);
        }

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
        } else if (tab == TAB_NPC) {
            drawNpcTab();
        } else if (tab == TAB_BLOCK) {
            drawBlockTab();
        } else if (tab == TAB_FOOD) {
            drawFoodTab();
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

    private void drawNpcTab() {
        fontRenderer.drawString("NPC Editor", guiLeft + 14, guiTop + 31, COL_ACCENT);

        // Left Panel Text
        fontRenderer.drawString("ID:", guiLeft + 15, guiTop + 36, COL_TEXT_DIM);
        fontRenderer.drawString("Name:", guiLeft + 15, guiTop + 64, COL_TEXT_DIM);
        fontRenderer.drawString("Health", guiLeft + 15, guiTop + 92, COL_TEXT_DIM);
        fontRenderer.drawString("Speed", guiLeft + 75, guiTop + 92, COL_TEXT_DIM);
        fontRenderer.drawString("Damage", guiLeft + 135, guiTop + 92, COL_TEXT_DIM);

        // Right Panel Text
        fontRenderer.drawString("Follow Rg", guiLeft + 198, guiTop + 36, COL_TEXT_DIM);
        fontRenderer.drawString("Shoot Rg", guiLeft + 268, guiTop + 36, COL_TEXT_DIM);
        fontRenderer.drawString("Texture Path", guiLeft + 198, guiTop + 64, COL_TEXT_DIM);

        // Vertical Equipment Slots (outside the main left panel)
        drawRect(guiLeft - 26, guiTop + 24, guiLeft, guiTop + 146, COL_BORDER);
        drawRect(guiLeft - 25, guiTop + 25, guiLeft, guiTop + 145, COL_PANEL_R);

        fontRenderer.drawString("Equip", guiLeft - 24, guiTop + 15, COL_TEXT_DIM);

        for (int i = 0; i < 6; ++i) {
            drawSlotBox(guiLeft - 22, guiTop + 30 + (i * 19), COL_SLOT_BG, false);
        }
    }

    private void drawBlockTab() {
        fontRenderer.drawString("Block Editor", guiLeft + 14, guiTop + 31, COL_ACCENT);

        fontRenderer.drawString("Hardness", guiLeft + 14, guiTop + 43, COL_TEXT_DIM);
        fontRenderer.drawString("Resistance", guiLeft + 75, guiTop + 43, COL_TEXT_DIM);
        fontRenderer.drawString("Light Lv", guiLeft + 14, guiTop + 73, COL_TEXT_DIM);
        fontRenderer.drawString("Tool", guiLeft + 75, guiTop + 73, COL_TEXT_DIM);
        fontRenderer.drawString("Material", guiLeft + 14, guiTop + 103, COL_TEXT_DIM);
        fontRenderer.drawString("Harvest Lv", guiLeft + 85, guiTop + 103, COL_TEXT_DIM);

        drawRightLabel(tr("label.item_id"), 28);
        drawRightLabel(tr("label.display_name"), 56);
        drawRightLabel(tr("label.creative_tab"), 112);
    }

    private void drawFoodTab() {
        fontRenderer.drawString("Food Editor", guiLeft + 14, guiTop + 31, COL_ACCENT);

        fontRenderer.drawString("Heal", guiLeft + 14, guiTop + 43, COL_TEXT_DIM);
        fontRenderer.drawString("Saturation", guiLeft + 75, guiTop + 43, COL_TEXT_DIM);
        fontRenderer.drawString("Is Meat", guiLeft + 14, guiTop + 73, COL_TEXT_DIM);
        fontRenderer.drawString("Always Edible", guiLeft + 75, guiTop + 73, COL_TEXT_DIM);
        fontRenderer.drawString("Potion Effect", guiLeft + 14, guiTop + 103, COL_TEXT_DIM);
        fontRenderer.drawString("Duration", guiLeft + 14, guiTop + 129, COL_TEXT_DIM);
        fontRenderer.drawString("Amp", guiLeft + 64, guiTop + 129, COL_TEXT_DIM);
        fontRenderer.drawString("Prob", guiLeft + 104, guiTop + 129, COL_TEXT_DIM);

        drawRightLabel(tr("label.item_id"), 28);
        drawRightLabel(tr("label.display_name"), 56);
        drawRightLabel(tr("label.max_stack"), 84);
        drawRightLabel(tr("label.creative_tab"), 112);
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
        if (txtPackName.getVisible() && txtPackName.textboxKeyTyped(typedChar, keyCode)) return;
        if (txtFileName.getVisible() && txtFileName.textboxKeyTyped(typedChar, keyCode)) return;

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
        } else if (tab == TAB_NPC) {
            if (txtNpcId.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcName.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcHealth.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcSpeed.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcDamage.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcFollow.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcShoot.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtNpcTexture.textboxKeyTyped(typedChar, keyCode)) return;
        } else if (tab == TAB_BLOCK) {
            if (txtBlockHardness.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtBlockResist.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtBlockLight.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtBlockTool.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtBlockMat.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtBlockHarvest.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtItemName.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtCreativeTab.textboxKeyTyped(typedChar, keyCode)) return;
        } else if (tab == TAB_FOOD) {
            if (txtFoodHeal.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodSat.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodMeat.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodAlways.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodEffect.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodDur.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodAmp.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtFoodProb.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtItemName.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtMaxStack.textboxKeyTyped(typedChar, keyCode)) return;
            if (txtCreativeTab.textboxKeyTyped(typedChar, keyCode)) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 20) { // Up
            if (tabScrollIndex > 0) tabScrollIndex--;
            updateTabState();
        } else if (button.id == 21) { // Down
            if (tabScrollIndex + VISIBLE_TABS < tabButtons.length) tabScrollIndex++;
            updateTabState();
        } else if (button.id == 0 || button.id == 1 || button.id == 2 || button.id == 4 || button.id == 6 || button.id == 8 || button.id == 9 || button.id == 10) {
            container.activeTab = button.id;
            updateTabState();
        } else if (button.id == 3) {
            if (container.activeTab == TAB_LOOT) generateLootTable();
            else if (container.activeTab == TAB_ITEMS) generateItem();
            else if (container.activeTab == TAB_RECIPES) generateRecipe();
            else if (container.activeTab == TAB_TABS) generateTab();
            else if (container.activeTab == TAB_NPC) generateNpc();
            else if (container.activeTab == TAB_BLOCK) generateBlock();
            else if (container.activeTab == TAB_FOOD) generateFood();
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

    private void generateNpc() {
        String id = txtNpcId.getText().trim();
        if (id.isEmpty()) id = "custom_npc";
        String fileName = id;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("name", txtNpcName.getText().trim());
        try { root.addProperty("maxHealth", Double.parseDouble(txtNpcHealth.getText().trim())); } catch (Exception e) {}
        try { root.addProperty("speed", Double.parseDouble(txtNpcSpeed.getText().trim())); } catch (Exception e) {}
        try { root.addProperty("attackDamage", Double.parseDouble(txtNpcDamage.getText().trim())); } catch (Exception e) {}
        try { root.addProperty("followRange", Double.parseDouble(txtNpcFollow.getText().trim())); } catch (Exception e) {}
        try { root.addProperty("shootRange", Double.parseDouble(txtNpcShoot.getText().trim())); } catch (Exception e) {}
        root.addProperty("texture", txtNpcTexture.getText().trim());

        JsonObject equipment = new JsonObject();
        String[] slots = {"mainhand", "offhand", "head", "chest", "legs", "feet"};
        for (int i = 0; i < 6; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                equipment.addProperty(slots[i], slot.getStack().getItem().getRegistryName().toString());
            }
        }
        root.add("equipment", equipment);

        saveJsonFile(root, "npcs", fileName);
    }

    private void generateBlock() {
        String id = txtFileName.getText().trim();
        if (id.isEmpty()) id = "custom_block";
        String fileName = id;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("display_name", txtItemName.getText().trim());
        root.addProperty("creative_tab", txtCreativeTab.getText().trim());
        try { root.addProperty("hardness", Float.parseFloat(txtBlockHardness.getText().trim())); } catch (Exception ignored) {}
        try { root.addProperty("resistance", Float.parseFloat(txtBlockResist.getText().trim())); } catch (Exception ignored) {}
        try { root.addProperty("light_level", Float.parseFloat(txtBlockLight.getText().trim())); } catch (Exception ignored) {}
        root.addProperty("tool_class", txtBlockTool.getText().trim());
        root.addProperty("material", txtBlockMat.getText().trim());
        try { root.addProperty("harvest_level", Integer.parseInt(txtBlockHarvest.getText().trim())); } catch (Exception ignored) {}

        saveJsonFile(root, "blocks", fileName);
    }

    private void generateFood() {
        String id = txtFileName.getText().trim();
        if (id.isEmpty()) id = "custom_food";
        String fileName = id;
        if (!fileName.endsWith(".json")) fileName += ".json";
        else id = id.substring(0, id.length() - 5);

        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("display_name", txtItemName.getText().trim());
        try { root.addProperty("max_stack_size", Integer.parseInt(txtMaxStack.getText().trim())); } catch (Exception ignored) {}
        root.addProperty("creative_tab", txtCreativeTab.getText().trim());

        try { root.addProperty("heal_amount", Integer.parseInt(txtFoodHeal.getText().trim())); } catch (Exception ignored) {}
        try { root.addProperty("saturation", Float.parseFloat(txtFoodSat.getText().trim())); } catch (Exception ignored) {}
        root.addProperty("is_meat", Boolean.parseBoolean(txtFoodMeat.getText().trim()));
        root.addProperty("always_edible", Boolean.parseBoolean(txtFoodAlways.getText().trim()));

        String effect = txtFoodEffect.getText().trim();
        if (!effect.isEmpty()) {
            root.addProperty("potion_effect", effect);
            try { root.addProperty("potion_duration", Integer.parseInt(txtFoodDur.getText().trim())); } catch (Exception ignored) {}
            try { root.addProperty("potion_amplifier", Integer.parseInt(txtFoodAmp.getText().trim())); } catch (Exception ignored) {}
            try { root.addProperty("potion_probability", Float.parseFloat(txtFoodProb.getText().trim())); } catch (Exception ignored) {}
        }

        saveJsonFile(root, "food", fileName);
    }

    public void receivePackList(String json) {
        // We will parse this and display it later. For now, it satisfies the server.
    }

    public void receiveFileContent(String packName, String filePath, String json) {
        // Will parse and load JSON here.
    }

    private void saveJsonFile(JsonObject root, String subDir, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(root);
        String packName = txtPackName.getText().trim();
        if (packName.isEmpty()) packName = "example_pack";

        modularcontents.ModularcontentsMod.PACKET_HANDLER.sendToServer(new modularcontents.custom.network.PacketSaveContent(packName, subDir + "/" + fileName, json));
    }
}
