package modularcontents.custom.gui;

import modularcontents.custom.client.GuiTheme;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import modularcontents.ModularcontentsMod;
import modularcontents.custom.inventory.ContainerContentCreator;
import modularcontents.custom.network.PacketRequestFileContent;
import modularcontents.custom.network.PacketRequestPackList;
import modularcontents.custom.network.PacketSaveContent;
import modularcontents.custom.network.PacketTogglePack;
import modularcontents.custom.pack.PackMeta;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GuiContentCreator extends GuiContainer {


    private static final int TAB_LOOT = 0;
    private static final int TAB_ITEMS = 1;
    private static final int TAB_RECIPES = 2;
    private static final int TAB_TABS = 4;
    private static final int TAB_ZONE = 6;
    private static final int TAB_NPC = 8;
    private static final int TAB_BLOCK = 9;
    private static final int TAB_FOOD = 10;
    private static final int TAB_THEME = 11;
    private static final int TAB_FUEL = 12;

    private static final int[] TAB_ORDER = {TAB_BLOCK, TAB_ITEMS, TAB_FOOD, TAB_FUEL, TAB_LOOT, TAB_RECIPES, TAB_TABS, TAB_ZONE, TAB_NPC, TAB_THEME};
    private static final String[] TAB_LABELS = {"Blocks", "Items", "Food", "Fuel", "Loot", "Recipes", "Tabs", "Zone", "NPC", "Theme"};

    private static final int BTN_TAB_BASE = 100;
    private static final int BTN_SAVE = 1;
    private static final int BTN_APPLY = 2;
    private static final int BTN_MAP = 3;
    private static final int BTN_NBT = 4;
    private static final int BTN_WHOLE = 5;

    private static final int M = 5;
    private static final int G = 4;
    private static final int TAB_W = 74;
    private static final int TAB_H = 15;
    private static final int ROW_H = 11;
    private static final int SCROLL_W = 6;
    private static final int INV_W = 9 * 18 + 8;
    private static final int INV_H = 4 * 18 + 4 + 10;

    private enum FieldType { STRING, INT, FLOAT, BOOL }

    private static class EditorField {
        final String jsonKey;
        final String label;
        final FieldType type;
        final GuiTextField field;
        final int span;

        EditorField(String jsonKey, String label, FieldType type, GuiTextField field, int span) {
            this.jsonKey = jsonKey;
            this.label = label;
            this.type = type;
            this.field = field;
            this.span = span;
        }
    }

    private static class BrowserRow {
        final boolean isPack;
        final String pack;
        final String path;
        final String label;

        BrowserRow(boolean isPack, String pack, String path, String label) {
            this.isPack = isPack;
            this.pack = pack;
            this.path = path;
            this.label = label;
        }
    }

    private static class ItemSettings {
        double chance = 0.5;
        int min = 1;
        int max = 1;
        boolean customized = false;
    }

    private static class RecipeSlotSettings {
        float chance = 100.0f;
        int cost = -1;
        boolean useNbt = false;
        boolean consumeWhole = false;
        String nbt = "";
        boolean touched = false;
    }

    private final ContainerContentCreator container;
    private final Map<Integer, List<EditorField>> tabFields = new LinkedHashMap<>();
    private final ItemSettings[] slotSettings = new ItemSettings[27];
    private final RecipeSlotSettings[] recipeSettings = new RecipeSlotSettings[27];
    private final Map<String, List<String>> packFiles = new TreeMap<>();
    private final List<BrowserRow> browserRows = new ArrayList<>();
    private final Set<String> disabledPacks = new HashSet<>();

    private int leftX, leftW, centerX, centerW, textX, textW, topY, topH, bottomY, bottomH;
    private int browserY, browserH, slotsY;
    private int nextFieldId = 1000;

    private GuiTextField txtPackName;
    private GuiTextField txtItemMin;
    private GuiTextField txtItemMax;
    private GuiTextField txtItemChance;
    private GuiTextField txtRecipeChance;
    private GuiTextField txtRecipeCost;
    private GuiTextField txtRecipeNbt;
    private GuiTextArea jsonEditor;

    private GuiButton btnSave;
    private GuiButton btnApply;
    private GuiButton btnMap;
    private GuiButton btnNbtToggle;
    private GuiButton btnWholeToggle;

    private int selectedSlot = -1;
    private String selectedPack = "";
    private String openedPath = null;
    private JsonObject baseJson = null;
    private PackMeta packMeta = null;
    private boolean jsonDirty = false;
    private String formSignature = "";
    private int browserScroll = 0;
    private float fieldScroll = 0.0f;
    private float fieldScrollTarget = 0.0f;
    private int fieldContentHeight = 0;
    private boolean draggingFieldBar = false;
    private long lastFrameTime = 0L;

    private String themeKey = "accent";
    private float themeH = 0.0f;
    private float themeS = 0.0f;
    private float themeV = 0.0f;
    private int themeA = 255;
    private int draggingThemeSlider = -1;
    private GuiTextField txtThemeHex;
    private float themeListScroll = 0.0f;
    private float themeListScrollTarget = 0.0f;
    private boolean draggingThemeList = false;

    public GuiContentCreator(InventoryPlayer playerInv) {
        super(new ContainerContentCreator(playerInv));
        this.container = (ContainerContentCreator) this.inventorySlots;
        for (int i = 0; i < 27; i++) {
            slotSettings[i] = new ItemSettings();
            recipeSettings[i] = new RecipeSlotSettings();
        }
    }

    @Override
    public void initGui() {
        this.xSize = this.width;
        this.ySize = this.height;
        super.initGui();
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        computeLayout();
        buildFields();
        buildButtons();

        this.txtPackName = plainField(centerX, 0, 100, 64, selectedPack.isEmpty() ? "example_pack" : selectedPack);
        this.txtItemMin = plainField(0, 0, 34, 3, "1");
        this.txtItemMax = plainField(0, 0, 34, 3, "1");
        this.txtItemChance = plainField(0, 0, 44, 5, "0.5");
        this.txtRecipeChance = plainField(0, 0, 44, 5, "100");
        this.txtRecipeCost = plainField(0, 0, 44, 5, "");
        this.txtRecipeNbt = plainField(0, 0, 120, 32000, "");
        this.txtThemeHex = plainField(0, 0, 70, 8, "FFFFAA00");
        loadThemeSelection();

        this.jsonEditor = new GuiTextArea(fontRenderer, textX, topY + 11, textW, height - M - 22 - topY - 11);

        layoutStatic();
        updateTabState();
        ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketRequestPackList());
    }

    private void computeLayout() {
        leftX = M;
        leftW = TAB_W + 8;
        textW = Math.max(170, Math.min(320, width / 3));
        textX = width - M - textW;
        centerX = leftX + leftW + G;
        centerW = textX - G - centerX;

        bottomH = INV_H;
        bottomY = height - M - bottomH;
        topY = M;
        topH = bottomY - G - topY;

        browserY = topY + 4 + TAB_ORDER.length * (TAB_H + 2) + 18;
        browserH = Math.max(ROW_H, topY + topH - browserY - 4);
    }

    private void buildButtons() {
        for (int i = 0; i < TAB_ORDER.length; i++) {
            GuiButton btn = new GuiLaptop.FlatButton(BTN_TAB_BASE + i, leftX + 4, topY + 4 + i * (TAB_H + 2), TAB_W, TAB_H, TAB_LABELS[i]);
            this.buttonList.add(btn);
        }

        int btnY = height - M - 18;
        int halfW = (textW - 4) / 2;
        this.btnApply = new GuiLaptop.FlatButton(BTN_APPLY, textX, btnY, halfW, 16, tr("btn.apply"));
        this.btnSave = new GuiLaptop.FlatButton(BTN_SAVE, textX + halfW + 4, btnY, textW - halfW - 4, 16, tr("btn.save"));
        this.btnMap = new GuiLaptop.FlatButton(BTN_MAP, centerX + 8, topY + 40, 110, 18, tr("open_map"));
        this.btnNbtToggle = new GuiLaptop.FlatButton(BTN_NBT, 0, 0, 60, 14, tr("nbt.off"));
        this.btnWholeToggle = new GuiLaptop.FlatButton(BTN_WHOLE, 0, 0, 96, 14, tr("whole.off"));

        this.buttonList.add(btnApply);
        this.buttonList.add(btnSave);
        this.buttonList.add(btnMap);
        this.buttonList.add(btnNbtToggle);
        this.buttonList.add(btnWholeToggle);
    }

    private GuiTextField plainField(int x, int y, int w, int maxLength, String text) {
        GuiTextField field = new GuiTextField(nextFieldId++, fontRenderer, x, y, w, 12);
        field.setMaxStringLength(maxLength);
        field.setText(text);
        return field;
    }

    private void addField(int tab, String jsonKey, String label, FieldType type, String def, int maxLength, int span) {
        tabFields.computeIfAbsent(tab, k -> new ArrayList<>())
                .add(new EditorField(jsonKey, label, type, plainField(0, 0, 10, maxLength, def), span));
    }

    private void buildFields() {
        tabFields.clear();

        addField(TAB_ITEMS, "id", "File name / ID", FieldType.STRING, "my_item", 64, 2);
        addField(TAB_ITEMS, "display_name", "Display Name", FieldType.STRING, "My Custom Item", 64, 2);
        addField(TAB_ITEMS, "max_stack_size", "Max Stack", FieldType.INT, "64", 3, 1);
        addField(TAB_ITEMS, "max_damage", "Durability", FieldType.INT, "0", 6, 1);
        addField(TAB_ITEMS, "creative_tab", "Creative Tab", FieldType.STRING, "misc", 32, 1);
        addField(TAB_ITEMS, "burn_time", "Burn Time", FieldType.INT, "0", 6, 1);

        addField(TAB_BLOCK, "id", "File name / ID", FieldType.STRING, "my_block", 64, 2);
        addField(TAB_BLOCK, "display_name", "Display Name", FieldType.STRING, "My Custom Block", 64, 2);
        addField(TAB_BLOCK, "creative_tab", "Creative Tab", FieldType.STRING, "buildingBlocks", 32, 1);
        addField(TAB_BLOCK, "material", "Material", FieldType.STRING, "rock", 16, 1);
        addField(TAB_BLOCK, "hardness", "Hardness", FieldType.FLOAT, "1.5", 6, 1);
        addField(TAB_BLOCK, "resistance", "Resistance", FieldType.FLOAT, "10.0", 6, 1);
        addField(TAB_BLOCK, "light_level", "Light Level 0-1", FieldType.FLOAT, "0.0", 6, 1);
        addField(TAB_BLOCK, "tool_class", "Tool Class", FieldType.STRING, "pickaxe", 16, 1);
        addField(TAB_BLOCK, "harvest_level", "Harvest Level", FieldType.INT, "0", 2, 1);
        addField(TAB_BLOCK, "block_type", "Block Type", FieldType.STRING, "block", 16, 1);
        addField(TAB_BLOCK, "rotation_type", "Rotation", FieldType.STRING, "none", 16, 1);
        addField(TAB_BLOCK, "biome_tint", "Biome Tint", FieldType.STRING, "", 16, 1);
        addField(TAB_BLOCK, "texture", "Texture", FieldType.STRING, "", 64, 2);
        addField(TAB_BLOCK, "has_stairs", "Has Stairs", FieldType.BOOL, "false", 5, 1);
        addField(TAB_BLOCK, "has_slab", "Has Slab", FieldType.BOOL, "false", 5, 1);
        addField(TAB_BLOCK, "has_fence", "Has Fence", FieldType.BOOL, "false", 5, 1);
        addField(TAB_BLOCK, "has_wall", "Has Wall", FieldType.BOOL, "false", 5, 1);
        addField(TAB_BLOCK, "burn_time", "Burn Time", FieldType.INT, "0", 6, 1);

        addField(TAB_FOOD, "id", "File name / ID", FieldType.STRING, "my_food", 64, 2);
        addField(TAB_FOOD, "display_name", "Display Name", FieldType.STRING, "My Custom Food", 64, 2);
        addField(TAB_FOOD, "max_stack_size", "Max Stack", FieldType.INT, "64", 3, 1);
        addField(TAB_FOOD, "creative_tab", "Creative Tab", FieldType.STRING, "food", 32, 1);
        addField(TAB_FOOD, "heal_amount", "Heal Amount", FieldType.INT, "4", 3, 1);
        addField(TAB_FOOD, "saturation", "Saturation", FieldType.FLOAT, "0.3", 6, 1);
        addField(TAB_FOOD, "is_meat", "Is Meat", FieldType.BOOL, "false", 5, 1);
        addField(TAB_FOOD, "always_edible", "Always Edible", FieldType.BOOL, "false", 5, 1);
        addField(TAB_FOOD, "potion_effect", "Potion Effect", FieldType.STRING, "", 64, 2);
        addField(TAB_FOOD, "potion_duration", "Duration", FieldType.INT, "100", 6, 1);
        addField(TAB_FOOD, "potion_amplifier", "Amplifier", FieldType.INT, "0", 2, 1);
        addField(TAB_FOOD, "potion_probability", "Probability", FieldType.FLOAT, "1.0", 5, 1);

        addField(TAB_FUEL, "id", "File name / ID", FieldType.STRING, "my_fuel", 64, 2);
        addField(TAB_FUEL, "item", "Item ID", FieldType.STRING, "modularcontents:my_item", 128, 2);
        addField(TAB_FUEL, "meta", "Meta (-1 = any)", FieldType.INT, "-1", 6, 1);
        addField(TAB_FUEL, "burn_time", "Burn Time (ticks)", FieldType.INT, "1600", 6, 1);

        addField(TAB_TABS, "id", "File name / ID", FieldType.STRING, "my_tab", 64, 2);
        addField(TAB_TABS, "display_name", "Display Name", FieldType.STRING, "My Custom Tab", 64, 2);
        addField(TAB_TABS, "icon", "Icon Item", FieldType.STRING, "minecraft:diamond_sword", 64, 2);

        addField(TAB_LOOT, null, "File name", FieldType.STRING, "custom_loot", 64, 2);
        addField(TAB_LOOT, "weight", "Weight", FieldType.INT, "50", 4, 1);

        addField(TAB_RECIPES, "id", "File name / ID", FieldType.STRING, "custom_recipe", 64, 2);
        addField(TAB_RECIPES, "category", "Category", FieldType.STRING, "general", 32, 1);
        addField(TAB_RECIPES, "craftingTime", "Craft Ticks", FieldType.INT, "200", 6, 1);
        addField(TAB_RECIPES, "minDrops", "Min Drops", FieldType.INT, "1", 3, 1);

        addField(TAB_NPC, "id", "File name / ID", FieldType.STRING, "custom_bandit", 64, 2);
        addField(TAB_NPC, "name", "Name", FieldType.STRING, "Bandit", 32, 2);
        addField(TAB_NPC, "maxHealth", "Health", FieldType.FLOAT, "20.0", 6, 1);
        addField(TAB_NPC, "speed", "Speed", FieldType.FLOAT, "0.25", 6, 1);
        addField(TAB_NPC, "attackDamage", "Damage", FieldType.FLOAT, "2.0", 6, 1);
        addField(TAB_NPC, "followRange", "Follow Range", FieldType.FLOAT, "32.0", 6, 1);
        addField(TAB_NPC, "shootRange", "Shoot Range", FieldType.FLOAT, "16.0", 6, 1);
        addField(TAB_NPC, "texture", "Texture Path", FieldType.STRING, "minecraft:textures/entity/steve.png", 128, 2);

    }

    private void loadThemeSelection() {
        Integer color = GuiTheme.snapshot().get(themeKey);
        if (color == null) return;
        themeA = (color >> 24) & 0xFF;
        float[] hsv = rgbToHsv((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        themeH = hsv[0];
        themeS = hsv[1];
        themeV = hsv[2];
        txtThemeHex.setText(String.format("%08X", color));
    }

    private int currentThemeColor() {
        return (themeA << 24) | (hsvToRgb(themeH, themeS, themeV) & 0xFFFFFF);
    }

    private void pushThemeColor() {
        int color = currentThemeColor();
        GuiTheme.apply(themeKey, color);
        txtThemeHex.setText(String.format("%08X", color));
        if (!jsonDirty) refreshJsonText();
    }

    private static float[] rgbToHsv(int r, int g, int b) {
        float max = Math.max(r, Math.max(g, b)) / 255.0f;
        float min = Math.min(r, Math.min(g, b)) / 255.0f;
        float delta = max - min;

        float h = 0.0f;
        if (delta > 0.0001f) {
            float rf = r / 255.0f, gf = g / 255.0f, bf = b / 255.0f;
            if (max == rf) h = 60.0f * (((gf - bf) / delta) % 6.0f);
            else if (max == gf) h = 60.0f * ((bf - rf) / delta + 2.0f);
            else h = 60.0f * ((rf - gf) / delta + 4.0f);
        }
        if (h < 0.0f) h += 360.0f;
        return new float[]{h, max <= 0.0f ? 0.0f : delta / max, max};
    }

    private static int hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1.0f - Math.abs((h / 60.0f) % 2.0f - 1.0f));
        float m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        int ri = Math.round((r + m) * 255.0f);
        int gi = Math.round((g + m) * 255.0f);
        int bi = Math.round((b + m) * 255.0f);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    private void layoutStatic() {
        txtPackName.x = packInfoX() + 8;
        txtPackName.y = bottomY + 18;
        txtPackName.width = Math.min(140, packInfoWidth() - 16);
    }

    private int packInfoX() {
        return M + INV_W + G;
    }

    private int packInfoWidth() {
        return textX - G - packInfoX();
    }

    private int contentTop() {
        return topY + 24;
    }

    private int contentBottom() {
        return topY + topH - 4;
    }

    private int contentViewHeight() {
        return Math.max(1, contentBottom() - contentTop());
    }

    private int maxFieldScroll() {
        return Math.max(0, fieldContentHeight - contentViewHeight());
    }

    private int layoutTabFields() {
        List<EditorField> fields = tabFields.get(container.activeTab);
        int y = contentTop();
        if (fields == null) return y;

        int pad = 8;
        int usableW = centerW - pad * 2 - SCROLL_W;
        int colW = (usableW - pad) / 2;
        int column = 0;

        for (EditorField ef : fields) {
            if (ef.span >= 2 && column == 1) {
                column = 0;
                y += 26;
            }
            ef.field.x = centerX + pad + column * (colW + pad);
            ef.field.y = y + 9;
            ef.field.width = ef.span >= 2 ? usableW : colW;

            if (ef.span >= 2) {
                y += 26;
            } else if (column == 1) {
                column = 0;
                y += 26;
            } else {
                column = 1;
            }
        }
        if (column == 1) y += 26;
        return y;
    }

    private int extraContentHeight() {
        switch (container.activeTab) {
            case TAB_LOOT: return 112;
            case TAB_RECIPES: return 150;
            case TAB_NPC: return 46;
            case TAB_THEME: return 0;
            default: return 0;
        }
    }

    private void relayout() {
        slotsY = layoutTabFields() + 6;
        fieldContentHeight = slotsY + extraContentHeight() - contentTop();
        clampFieldScroll();
        layoutSlots();
        positionSidePanel();
    }

    private void clampFieldScroll() {
        int max = maxFieldScroll();
        fieldScrollTarget = Math.max(0.0f, Math.min(max, fieldScrollTarget));
        fieldScroll = Math.max(0.0f, Math.min(max, fieldScroll));
    }

    private static String tr(String key, Object... args) {
        return I18n.format("modularcontents.creator." + key, args);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuiTextField field : activeFields()) {
            field.updateCursorCounter();
        }
        jsonEditor.updateCursorCounter();

        if (jsonEditor.consumeChanged()) {
            jsonDirty = true;
        }
        String signature = formSignature();
        if (!signature.equals(formSignature)) {
            formSignature = signature;
            if (!jsonDirty) refreshJsonText();
        }

    }

    private String formSignature() {
        StringBuilder sb = new StringBuilder();
        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields != null) {
            for (EditorField ef : fields) {
                sb.append(ef.field.getText()).append('|');
            }
        }
        sb.append(container.activeTab).append('|');
        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot.getHasStack()) {
                sb.append(i).append('=').append(slot.getStack().getItem().getRegistryName())
                        .append('x').append(slot.getStack().getCount()).append('|');
            }
        }
        return sb.toString();
    }

    private List<GuiTextField> activeFields() {
        List<GuiTextField> list = new ArrayList<>();
        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields != null) {
            for (EditorField ef : fields) list.add(ef.field);
        }
        list.add(txtPackName);
        if (container.activeTab == TAB_THEME) list.add(txtThemeHex);
        if (txtItemMin.getVisible()) list.addAll(Arrays.asList(txtItemMin, txtItemMax, txtItemChance));
        if (txtRecipeChance.getVisible()) list.add(txtRecipeChance);
        if (txtRecipeCost.getVisible()) list.add(txtRecipeCost);
        if (txtRecipeNbt.getVisible()) list.add(txtRecipeNbt);
        return list;
    }

    private void updateTabState() {
        int tab = container.activeTab;

        for (GuiButton btn : this.buttonList) {
            if (btn.id >= BTN_TAB_BASE) {
                btn.enabled = TAB_ORDER[btn.id - BTN_TAB_BASE] != tab;
            }
        }

        boolean isZone = tab == TAB_ZONE;
        btnMap.visible = isZone;
        btnSave.visible = !isZone;
        btnApply.visible = !isZone;

        fieldScroll = 0.0f;
        fieldScrollTarget = 0.0f;
        relayout();
        buildBrowserRows();

        selectedSlot = -1;
        updateSidePanel();
        refreshJsonText();
    }

    private void layoutSlots() {
        int tab = container.activeTab;
        int gridX = centerX + 9;
        int offset = Math.round(fieldScroll) - 1;

        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (tab == TAB_LOOT) {
                slot.xPos = gridX + (i % 9) * 18;
                slot.yPos = slotsY + 10 + (i / 9) * 18 - offset;
            } else if (tab == TAB_RECIPES) {
                slot.xPos = gridX + (i % 9) * 18;
                slot.yPos = (i < 9 ? slotsY + 10 : slotsY + 50 + (i / 9 - 1) * 18) - offset;
            } else if (tab == TAB_NPC && i < 6) {
                slot.xPos = gridX + i * 20;
                slot.yPos = slotsY + 10 - offset;
            } else {
                slot.yPos = -9999;
            }
            if (slot.yPos > -9999 && (slot.yPos < contentTop() || slot.yPos + 18 > contentBottom())) {
                slot.yPos = -9999;
            }
        }

        int invX = M + 5;
        int invY = bottomY + 7;
        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(27 + i);
            slot.xPos = invX + (i % 9) * 18;
            slot.yPos = invY + (i / 9) * 18;
        }
        for (int i = 0; i < 9; i++) {
            Slot slot = container.inventorySlots.get(54 + i);
            slot.xPos = invX + i * 18;
            slot.yPos = invY + 3 * 18 + 4;
        }
    }

    private void positionSidePanel() {
        int panelY = slotsY + 70;
        int panelX = centerX + 8;
        txtItemMin.x = panelX;
        txtItemMin.y = panelY + 10;
        txtItemMax.x = panelX + 44;
        txtItemMax.y = panelY + 10;
        txtItemChance.x = panelX + 88;
        txtItemChance.y = panelY + 10;

        int recpY = slotsY + 106;
        txtRecipeChance.x = panelX;
        txtRecipeChance.y = recpY + 10;
        txtRecipeCost.x = panelX;
        txtRecipeCost.y = recpY + 10;
        txtRecipeNbt.x = panelX + 54;
        txtRecipeNbt.y = recpY + 10;
        txtRecipeNbt.width = Math.max(60, centerW - 70 - SCROLL_W);
        btnNbtToggle.x = panelX;
        btnNbtToggle.y = recpY + 26 - Math.round(fieldScroll);
        btnWholeToggle.x = panelX + 66;
        btnWholeToggle.y = btnNbtToggle.y;
        boolean recpSel = container.activeTab == TAB_RECIPES && selectedSlot != -1 && getSelectedStack() != null;
        boolean rowVisible = btnNbtToggle.y >= contentTop() && btnNbtToggle.y + 14 <= contentBottom();
        btnNbtToggle.visible = recpSel && rowVisible;
        btnWholeToggle.visible = recpSel && rowVisible && isToolInputSelected();
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
        txtRecipeCost.setVisible(recpSel && !recpOutSel);
        txtRecipeNbt.setVisible(recpSel);
        btnNbtToggle.visible = recpSel;
        btnWholeToggle.visible = recpSel && isToolInputSelected();

        if (recpSel) {
            RecipeSlotSettings set = recipeSettings[selectedSlot];
            ItemStack stack = getSelectedStack();
            if (!set.touched && stack.hasTagCompound()) {
                set.nbt = stack.getTagCompound().toString();
                set.useNbt = true;
                set.touched = true;
            }
            txtRecipeChance.setText(formatChance(set.chance));
            txtRecipeCost.setText(set.cost < 0 ? "" : String.valueOf(set.cost));
            txtRecipeNbt.setText(set.nbt);
            btnNbtToggle.displayString = set.useNbt ? tr("nbt.on") : tr("nbt.off");
            btnWholeToggle.displayString = set.consumeWhole ? tr("whole.on") : tr("whole.off");
        }
    }

    private boolean isToolInputSelected() {
        ItemStack stack = getSelectedStack();
        return selectedSlot >= 9 && stack != null && stack.isItemStackDamageable();
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

    private void drawPanel(int x1, int y1, int x2, int y2, int color) {
        drawRect(x1, y1, x2, y2, GuiTheme.BORDER);
        drawRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private void drawSlotBox(int x, int y, int bgColor, boolean selected) {
        drawRect(x, y, x + 18, y + 18, selected ? GuiTheme.ACCENT : 0xFF3A3A3A);
        drawRect(x + 1, y + 1, x + 17, y + 17, bgColor);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawPanel(leftX, topY, leftX + leftW, topY + topH, GuiTheme.PANEL);
        drawPanel(centerX, topY, centerX + centerW, topY + topH, GuiTheme.PANEL_ALT);
        drawPanel(M, bottomY, M + INV_W, bottomY + bottomH, GuiTheme.PANEL);
        drawPanel(packInfoX(), bottomY, packInfoX() + packInfoWidth(), bottomY + bottomH, GuiTheme.PANEL_ALT);

        String title = tr("title");
        fontRenderer.drawStringWithShadow(title, centerX + centerW - fontRenderer.getStringWidth(title) - 8, topY + 8, GuiTheme.ACCENT);

        drawBrowser(mouseX, mouseY);
        drawInventoryPanel();
        drawPackInfo();
        drawTextEditor();

        int tab = container.activeTab;
        String header = TAB_LABELS[tabIndex(tab)] + " Editor";
        fontRenderer.drawString(header, centerX + 8, topY + 8, GuiTheme.ACCENT);
        drawRect(centerX + 1, topY + 19, centerX + centerW - 1, topY + 20, GuiTheme.LINE);

        advanceScroll();
        relayout();
        beginClip(centerX + 1, contentTop(), centerW - 2, contentBottom() - contentTop());
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, -fieldScroll, 0.0f);

        List<EditorField> fields = tabFields.get(tab);
        if (fields != null) {
            for (EditorField ef : fields) {
                fontRenderer.drawString(ef.label, ef.field.x, ef.field.y - 9, GuiTheme.TEXT_DIM);
                ef.field.drawTextBox();
            }
        }

        if (tab == TAB_LOOT) drawLootSlots();
        else if (tab == TAB_RECIPES) drawRecipeSlots();
        else if (tab == TAB_NPC) drawNpcSlots();
        else if (tab == TAB_ZONE) drawZoneInfo();
        else if (tab == TAB_THEME) drawThemeTab();

        if (txtItemMin.getVisible()) txtItemMin.drawTextBox();
        if (txtItemMax.getVisible()) txtItemMax.drawTextBox();
        if (txtItemChance.getVisible()) txtItemChance.drawTextBox();
        if (txtRecipeChance.getVisible()) txtRecipeChance.drawTextBox();
        if (txtRecipeCost.getVisible()) txtRecipeCost.drawTextBox();
        if (txtRecipeNbt.getVisible()) txtRecipeNbt.drawTextBox();

        GlStateManager.popMatrix();
        endClip();
        drawFieldScrollBar();
        txtPackName.drawTextBox();
    }

    private void advanceScroll() {
        long now = System.nanoTime();
        float dt = lastFrameTime == 0L ? 0.016f : Math.min(0.1f, (now - lastFrameTime) / 1.0e9f);
        lastFrameTime = now;

        float blend = 1.0f - (float) Math.exp(-16.0f * dt);

        themeListScrollTarget = Math.max(0.0f, Math.min(maxThemeListScroll(), themeListScrollTarget));
        if (draggingThemeList) {
            themeListScroll = themeListScrollTarget;
        } else {
            themeListScroll += (themeListScrollTarget - themeListScroll) * blend;
            if (Math.abs(themeListScrollTarget - themeListScroll) < 0.05f) themeListScroll = themeListScrollTarget;
        }

        if (draggingFieldBar) {
            fieldScroll = fieldScrollTarget;
            return;
        }
        fieldScroll += (fieldScrollTarget - fieldScroll) * blend;
        if (Math.abs(fieldScrollTarget - fieldScroll) < 0.05f) fieldScroll = fieldScrollTarget;
    }

    private void beginClip(int x, int y, int w, int h) {
        int factor = new ScaledResolution(this.mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * factor, (this.height - (y + h)) * factor, Math.max(0, w) * factor, Math.max(0, h) * factor);
    }

    private void endClip() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void drawFieldScrollBar() {
        int max = maxFieldScroll();
        if (max <= 0) return;

        int barX = centerX + centerW - SCROLL_W - 2;
        int trackTop = contentTop();
        int trackH = contentBottom() - trackTop;
        drawRect(barX, trackTop, barX + SCROLL_W, trackTop + trackH, GuiTheme.SLOT_BG);

        int thumbH = Math.max(12, trackH * contentViewHeight() / Math.max(1, fieldContentHeight));
        int thumbY = trackTop + Math.round((trackH - thumbH) * (fieldScroll / max));
        drawRect(barX, thumbY, barX + SCROLL_W, thumbY + thumbH, GuiTheme.BORDER);
    }

    private boolean insideContent(int mouseX, int mouseY) {
        return mouseX >= centerX && mouseX < centerX + centerW && mouseY >= contentTop() && mouseY < contentBottom();
    }

    private int tabIndex(int tab) {
        for (int i = 0; i < TAB_ORDER.length; i++) {
            if (TAB_ORDER[i] == tab) return i;
        }
        return 0;
    }

    private void drawTextEditor() {
        fontRenderer.drawString(tr("text_editor"), textX, topY - 1, GuiTheme.ACCENT);
        jsonEditor.draw(0, 0);
        if (jsonDirty) {
            String mark = tr("edited");
            fontRenderer.drawString(mark, textX + textW - fontRenderer.getStringWidth(mark), topY - 1, GuiTheme.RED);
        }
    }

    private void drawBrowser(int mouseX, int mouseY) {
        fontRenderer.drawString(tr("packs"), leftX + 4, browserY - 10, GuiTheme.TEXT_DIM);
        drawRect(leftX + 3, browserY, leftX + leftW - 3, browserY + browserH, GuiTheme.SLOT_BG);

        if (browserRows.isEmpty()) {
            fontRenderer.drawString(tr("hint.no_packs"), leftX + 6, browserY + 4, GuiTheme.TEXT_DIM);
            return;
        }

        int visible = Math.max(1, browserH / ROW_H);
        for (int i = 0; i < visible; i++) {
            int index = browserScroll + i;
            if (index >= browserRows.size()) break;
            BrowserRow row = browserRows.get(index);
            int rowY = browserY + i * ROW_H;

            boolean selected = row.isPack ? row.pack.equals(selectedPack) : row.path.equals(openedPath);
            if (selected) drawRect(leftX + 4, rowY, leftX + leftW - 4, rowY + ROW_H, GuiTheme.SELECTED);

            int color = row.isPack ? GuiTheme.TEXT : GuiTheme.TEXT_DIM;
            if (row.isPack && disabledPacks.contains(row.pack)) color = GuiTheme.RED;
            if (mouseX >= leftX + 4 && mouseX < leftX + leftW - 4 && mouseY >= rowY && mouseY < rowY + ROW_H) {
                color = GuiTheme.ACCENT;
            }
            fontRenderer.drawString(fontRenderer.trimStringToWidth(row.label, leftW - 12), leftX + 6, rowY + 2, color);
        }
    }

    private void drawInventoryPanel() {
        int invX = M + 4;
        int invY = bottomY + 6;
        for (int i = 0; i < 27; i++) {
            drawSlotBox(invX + (i % 9) * 18, invY + (i / 9) * 18, GuiTheme.SLOT_BG, false);
        }
        for (int i = 0; i < 9; i++) {
            drawSlotBox(invX + i * 18, invY + 3 * 18 + 4, GuiTheme.SLOT_BG, false);
        }
    }

    private void drawPackInfo() {
        int x = packInfoX() + 8;
        int w = packInfoWidth();
        drawRect(packInfoX() + 1, bottomY + 1, packInfoX() + w - 1, bottomY + 14, GuiTheme.BORDER);
        fontRenderer.drawString(tr("pack_info"), x, bottomY + 4, GuiTheme.TEXT);

        String author = packMeta != null && !packMeta.author.isEmpty() ? packMeta.author : "-";
        String version = packMeta != null && !packMeta.version.isEmpty() ? packMeta.version : "-";
        String description = packMeta != null && !packMeta.description.isEmpty() ? packMeta.description : "-";

        int y = bottomY + 36;
        fontRenderer.drawString(tr("label.author") + ": " + author, x, y, GuiTheme.TEXT_DIM);
        fontRenderer.drawString(tr("label.version") + ": " + version, x, y + 11, GuiTheme.TEXT_DIM);
        fontRenderer.drawString(fontRenderer.trimStringToWidth(tr("label.description") + ": " + description, w - 16), x, y + 22, GuiTheme.TEXT_DIM);
    }

    private void drawLootSlots() {
        fontRenderer.drawString(tr("header.loot"), centerX + 8, slotsY, GuiTheme.ACCENT);
        for (int i = 0; i < 27; ++i) {
            drawSlotBox(centerX + 8 + (i % 9) * 18, slotsY + 10 + (i / 9) * 18, GuiTheme.SLOT_BG, i == selectedSlot);
        }

        int panelY = slotsY + 70;
        if (selectedSlot != -1) {
            ItemStack stack = getSelectedStack();
            String header = tr("slot", selectedSlot, stack != null ? stack.getDisplayName() : tr("empty"));
            fontRenderer.drawString(fontRenderer.trimStringToWidth(header, centerW - 16), centerX + 8, panelY, GuiTheme.ACCENT);
            fontRenderer.drawString(tr("label.min"), centerX + 8, panelY + 24, GuiTheme.TEXT_DIM);
            fontRenderer.drawString(tr("label.max"), centerX + 52, panelY + 24, GuiTheme.TEXT_DIM);
            fontRenderer.drawString(tr("label.chance01"), centerX + 96, panelY + 24, GuiTheme.TEXT_DIM);
        } else {
            fontRenderer.drawString(tr("hint.loot.1"), centerX + 8, panelY, GuiTheme.TEXT_DIM);
            fontRenderer.drawString(tr("hint.loot.2"), centerX + 8, panelY + 11, GuiTheme.TEXT_DIM);
        }
    }

    private void drawRecipeSlots() {
        fontRenderer.drawString(tr("header.outputs"), centerX + 8, slotsY, GuiTheme.ACCENT);
        for (int i = 0; i < 9; ++i) {
            drawSlotBox(centerX + 8 + i * 18, slotsY + 10, GuiTheme.OUTPUT_BG, i == selectedSlot);
        }
        fontRenderer.drawString(tr("header.inputs"), centerX + 8, slotsY + 40 - 10, GuiTheme.ACCENT);
        for (int i = 9; i < 27; ++i) {
            drawSlotBox(centerX + 8 + (i % 9) * 18, slotsY + 50 + (i / 9 - 1) * 18, GuiTheme.SLOT_BG, i == selectedSlot);
        }

        int panelY = slotsY + 106;
        ItemStack stack = getSelectedStack();
        if (selectedSlot != -1 && stack != null) {
            String kind = selectedSlot < 9 ? tr("output") : tr("input");
            String header = kind + ": " + stack.getDisplayName() + " x" + stack.getCount();
            fontRenderer.drawString(fontRenderer.trimStringToWidth(header, centerW - 16), centerX + 8, panelY - 12, GuiTheme.ACCENT);
            if (selectedSlot < 9) {
                fontRenderer.drawString(tr("label.chance_pct"), centerX + 8, panelY, GuiTheme.TEXT_DIM);
            } else {
                fontRenderer.drawString(isToolInputSelected() && !recipeSettings[selectedSlot].consumeWhole
                                ? tr("label.durability") : tr("label.consume"),
                        centerX + 8, panelY, GuiTheme.TEXT_DIM);
            }
            fontRenderer.drawString(tr("label.nbt"), centerX + 62, panelY, GuiTheme.TEXT_DIM);
        } else {
            fontRenderer.drawString(tr("hint.recipe.1"), centerX + 8, panelY, GuiTheme.TEXT_DIM);
            fontRenderer.drawString(tr("hint.recipe.2"), centerX + 8, panelY + 11, GuiTheme.TEXT_DIM);
        }
    }

    private void drawNpcSlots() {
        fontRenderer.drawString(tr("header.equipment"), centerX + 8, slotsY, GuiTheme.ACCENT);
        String[] names = {"Hand", "Off", "Head", "Chest", "Legs", "Feet"};
        for (int i = 0; i < 6; ++i) {
            drawSlotBox(centerX + 8 + i * 20, slotsY + 10, GuiTheme.SLOT_BG, false);
            fontRenderer.drawString(names[i], centerX + 8 + i * 20, slotsY + 30, GuiTheme.TEXT_DIM);
        }
    }

    private static final int THEME_ROW_H = 14;
    private static final int THEME_LIST_W = 116;
    private static final int SLIDER_H = 6;
    private static final int SLIDER_GAP = 22;
    private static final String[] SLIDER_LABELS = {"Hue", "Saturation", "Brightness", "Alpha"};

    private int themeSliderX() {
        return centerX + 16 + THEME_LIST_W;
    }

    private int themeSliderW() {
        return Math.max(60, centerW - THEME_LIST_W - 40 - SCROLL_W);
    }

    private int themeTop() {
        return slotsY + 16;
    }

    private int themeListBottom() {
        return contentBottom() - 2;
    }

    private int maxThemeListScroll() {
        int visible = themeListBottom() - 2 - themeTop();
        return Math.max(0, GuiTheme.snapshot().size() * THEME_ROW_H - visible);
    }

    private int themeSliderY(int index) {
        return themeTop() + 24 + index * SLIDER_GAP;
    }

    private void drawThemeTab() {
        List<String> keys = new ArrayList<>(GuiTheme.snapshot().keySet());
        Map<String, Integer> colors = GuiTheme.snapshot();

        int top = themeTop();
        int listX = centerX + 6;
        int listRight = listX + THEME_LIST_W;
        int listBottom = themeListBottom();
        drawRect(listX, slotsY, listRight, listBottom, GuiTheme.BORDER);
        drawRect(listX + 1, slotsY + 1, listRight - 1, listBottom - 1, GuiTheme.PANEL);
        fontRenderer.drawString(tr("theme.elements"), listX + 4, slotsY + 3, GuiTheme.TEXT_DIM);

        beginClip(listX + 1, top, THEME_LIST_W - 2, listBottom - 2 - top);
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            int rowY = top + i * THEME_ROW_H - Math.round(themeListScroll);
            if (rowY + THEME_ROW_H < top || rowY > listBottom) continue;
            boolean selected = key.equals(themeKey);
            if (selected) {
                drawRect(listX + 2, rowY, listRight - 2, rowY + THEME_ROW_H - 1, GuiTheme.SELECTED);
            }
            drawRect(listX + 4, rowY + 1, listX + 16, rowY + 12, GuiTheme.BORDER);
            drawRect(listX + 5, rowY + 2, listX + 15, rowY + 11, colors.get(key));
            fontRenderer.drawString(key, listX + 20, rowY + 3, selected ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
        }
        endClip();
        beginClip(centerX + 1, contentTop(), centerW - 2, contentBottom() - contentTop());

        int maxListScroll = maxThemeListScroll();
        if (maxListScroll > 0) {
            int trackX = listRight - 2 - SCROLL_W;
            int trackTop = top;
            int trackH = listBottom - 2 - top;
            drawRect(trackX, trackTop, trackX + SCROLL_W, trackTop + trackH, GuiTheme.SCROLL_TRACK);
            int thumbH = Math.max(12, trackH * trackH / (trackH + maxListScroll));
            int thumbY = trackTop + Math.round((trackH - thumbH) * (themeListScroll / maxListScroll));
            drawRect(trackX, thumbY, trackX + SCROLL_W, thumbY + thumbH, GuiTheme.SCROLL_THUMB);
        }

        int sx = themeSliderX();
        int sw = themeSliderW();

        int editorLeft = sx - 8;
        int editorRight = Math.min(centerX + centerW - SCROLL_W - 4, sx + sw + 34);
        int editorBottom = themeSliderY(4) + 22;
        drawRect(editorLeft, slotsY, editorRight, editorBottom, GuiTheme.BORDER);
        drawRect(editorLeft + 1, slotsY + 1, editorRight - 1, editorBottom - 1, GuiTheme.PANEL);
        fontRenderer.drawString(tr("theme.color"), editorLeft + 4, slotsY + 3, GuiTheme.TEXT_DIM);

        fontRenderer.drawString(themeKey, sx, top, GuiTheme.ACCENT);
        drawRect(sx + sw - 18, top - 2, sx + sw, top + 10, GuiTheme.BORDER);
        drawCheckerboard(sx + sw - 17, top - 1, sx + sw - 1, top + 9);
        drawRect(sx + sw - 17, top - 1, sx + sw - 1, top + 9, currentThemeColor());

        for (int i = 0; i < 4; i++) {
            int y = themeSliderY(i);
            fontRenderer.drawString(SLIDER_LABELS[i], sx, y - 10, GuiTheme.TEXT_DIM);
            drawSliderTrack(i, sx, y, sw);

            float fraction = sliderFraction(i);
            int knobX = sx + Math.round(fraction * (sw - 3));
            drawRect(knobX, y - 3, knobX + 3, y + SLIDER_H + 3, GuiTheme.ACCENT);

            String value = i == 0 ? String.valueOf(Math.round(themeH))
                    : i == 3 ? String.valueOf(themeA)
                    : String.valueOf(Math.round((i == 1 ? themeS : themeV) * 100.0f));
            fontRenderer.drawString(value, sx + sw + 6, y - 1, GuiTheme.TEXT);
        }

        int hexY = themeSliderY(4);
        fontRenderer.drawString("Hex (AARRGGBB)", sx, hexY - 10, GuiTheme.TEXT_DIM);
        txtThemeHex.x = sx;
        txtThemeHex.y = hexY;
        txtThemeHex.setVisible(true);
        txtThemeHex.drawTextBox();
    }

    private float sliderFraction(int index) {
        switch (index) {
            case 0: return themeH / 360.0f;
            case 1: return themeS;
            case 2: return themeV;
            default: return themeA / 255.0f;
        }
    }

    private void drawSliderTrack(int index, int x, int y, int w) {
        drawRect(x - 1, y - 1, x + w + 1, y + SLIDER_H + 1, GuiTheme.BORDER);

        if (index == 0) {
            for (int seg = 0; seg < 6; seg++) {
                int x1 = x + seg * w / 6;
                int x2 = x + (seg + 1) * w / 6;
                drawHGradient(x1, y, x2, y + SLIDER_H,
                        hsvToRgb(seg * 60.0f, 1.0f, 1.0f),
                        hsvToRgb((seg + 1) * 60.0f % 360.0f, 1.0f, 1.0f));
            }
            return;
        }

        if (index == 1) {
            float v = themeV <= 0.0f ? 1.0f : themeV;
            drawHGradient(x, y, x + w, y + SLIDER_H, hsvToRgb(themeH, 0.0f, v), hsvToRgb(themeH, 1.0f, v));
            return;
        }

        if (index == 2) {
            drawHGradient(x, y, x + w, y + SLIDER_H, hsvToRgb(themeH, themeS, 0.0f), hsvToRgb(themeH, themeS, 1.0f));
            return;
        }

        int rgb = hsvToRgb(themeH, themeS, themeV) & 0xFFFFFF;
        drawCheckerboard(x, y, x + w, y + SLIDER_H);
        drawHGradient(x, y, x + w, y + SLIDER_H, rgb, 0xFF000000 | rgb);
    }

    private void drawCheckerboard(int x1, int y1, int x2, int y2) {
        drawRect(x1, y1, x2, y2, 0xFFBBBBBB);
        int cell = 3;
        for (int y = y1, row = 0; y < y2; y += cell, row++) {
            for (int x = x1 + (row % 2) * cell; x < x2; x += cell * 2) {
                drawRect(x, y, Math.min(x + cell, x2), Math.min(y + cell, y2), 0xFF777777);
            }
        }
    }

    private void drawHGradient(int x1, int y1, int x2, int y2, int leftColor, int rightColor) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        putColorVertex(buffer, x2, y1, rightColor);
        putColorVertex(buffer, x1, y1, leftColor);
        putColorVertex(buffer, x1, y2, leftColor);
        putColorVertex(buffer, x2, y2, rightColor);
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void putColorVertex(BufferBuilder buffer, int x, int y, int color) {
        buffer.pos(x, y, this.zLevel)
                .color((color >> 16 & 0xFF) / 255.0f, (color >> 8 & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, (color >> 24 & 0xFF) / 255.0f)
                .endVertex();
    }

    private boolean clickThemeTab(int mouseX, int mouseY) {
        List<String> keys = new ArrayList<>(GuiTheme.snapshot().keySet());
        if (mouseX >= centerX + 6 && mouseX < centerX + 6 + THEME_LIST_W
                && mouseY >= themeTop() && mouseY < themeListBottom()) {
            int trackX = centerX + 6 + THEME_LIST_W - 2 - SCROLL_W;
            if (maxThemeListScroll() > 0 && mouseX >= trackX) {
                draggingThemeList = true;
                dragThemeList(mouseY);
                return true;
            }
            int index = (mouseY + Math.round(themeListScroll) - themeTop()) / THEME_ROW_H;
            if (index >= 0 && index < keys.size()) {
                themeKey = keys.get(index);
                loadThemeSelection();
                return true;
            }
        }

        int slider = themeSliderAt(mouseX, mouseY);
        if (slider >= 0) {
            draggingThemeSlider = slider;
            dragThemeSlider(mouseX);
            return true;
        }
        return false;
    }

    private int themeSliderAt(int mouseX, int mouseY) {
        int sx = themeSliderX();
        int sw = themeSliderW();
        for (int i = 0; i < 4; i++) {
            int y = themeSliderY(i);
            if (mouseX >= sx - 3 && mouseX <= sx + sw + 3 && mouseY >= y - 6 && mouseY <= y + SLIDER_H + 6) {
                return i;
            }
        }
        return -1;
    }

    private void nudgeThemeSlider(int index, int notches) {
        switch (index) {
            case 0:
                themeH = (themeH + notches * 5.0f) % 360.0f;
                if (themeH < 0.0f) themeH += 360.0f;
                break;
            case 1:
                themeS = Math.max(0.0f, Math.min(1.0f, themeS + notches * 0.02f));
                break;
            case 2:
                themeV = Math.max(0.0f, Math.min(1.0f, themeV + notches * 0.02f));
                break;
            default:
                themeA = Math.max(0, Math.min(255, themeA + notches * 5));
                break;
        }
        pushThemeColor();
    }

    private void dragThemeList(int mouseY) {
        int max = maxThemeListScroll();
        if (max <= 0) return;
        float fraction = (mouseY - themeTop()) / (float) Math.max(1, themeListBottom() - 2 - themeTop());
        themeListScrollTarget = Math.max(0.0f, Math.min(max, fraction * max));
        themeListScroll = themeListScrollTarget;
    }

    private void dragThemeSlider(int mouseX) {
        if (draggingThemeSlider < 0) return;
        int sx = themeSliderX();
        int sw = themeSliderW();
        float f = Math.max(0.0f, Math.min(1.0f, (mouseX - sx) / (float) Math.max(1, sw - 3)));
        switch (draggingThemeSlider) {
            case 0: themeH = f * 360.0f; break;
            case 1: themeS = f; break;
            case 2: themeV = f; break;
            default: themeA = Math.round(f * 255.0f); break;
        }
        pushThemeColor();
    }

    private void drawZoneInfo() {
        fontRenderer.drawString(tr("header.zone"), centerX + 8, topY + 26, GuiTheme.ACCENT);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {}

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        int notches = wheel / 120;
        if (notches == 0) notches = wheel > 0 ? 1 : -1;

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        if (jsonEditor.isInside(mouseX, mouseY)) {
            jsonEditor.scroll(notches);
        } else if (container.activeTab == TAB_THEME && insideContent(mouseX, mouseY)) {
            int slider = themeSliderAt(mouseX, mouseY + Math.round(fieldScroll));
            if (slider >= 0) {
                nudgeThemeSlider(slider, notches);
            } else if (mouseX < centerX + 6 + THEME_LIST_W) {
                themeListScrollTarget = Math.max(0.0f, Math.min(maxThemeListScroll(), themeListScrollTarget - notches * THEME_ROW_H * 2));
            }
        } else if (insideContent(mouseX, mouseY)) {
            fieldScrollTarget = Math.max(0.0f, Math.min(maxFieldScroll(), fieldScrollTarget - notches * 26));
        } else if (mouseX >= leftX && mouseX < leftX + leftW && mouseY >= browserY && mouseY < browserY + browserH) {
            int visible = Math.max(1, browserH / ROW_H);
            int max = Math.max(0, browserRows.size() - visible);
            browserScroll = Math.max(0, Math.min(max, browserScroll - notches));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        jsonEditor.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && maxFieldScroll() > 0
                && mouseX >= centerX + centerW - SCROLL_W - 2 && mouseX < centerX + centerW - 2
                && mouseY >= contentTop() && mouseY < contentBottom()) {
            draggingFieldBar = true;
            dragFieldBar(mouseY);
            return;
        }

        txtPackName.mouseClicked(mouseX, mouseY, mouseButton);
        if (insideContent(mouseX, mouseY)) {
            int localY = mouseY + Math.round(fieldScroll);
            if (container.activeTab == TAB_THEME && clickThemeTab(mouseX, localY)) return;
            for (GuiTextField field : activeFields()) {
                if (field.getVisible()) field.mouseClicked(mouseX, localY, mouseButton);
            }
        } else {
            for (GuiTextField field : activeFields()) {
                if (field.getVisible()) field.setFocused(false);
            }
        }

        if (clickBrowser(mouseX, mouseY)) return;

        int tab = container.activeTab;
        if (tab == TAB_LOOT || tab == TAB_RECIPES) {
            for (int i = 0; i < 27; i++) {
                Slot slot = container.inventorySlots.get(i);
                if (slot.yPos < 0) continue;
                if (mouseX >= slot.xPos - 1 && mouseX < slot.xPos + 17 && mouseY >= slot.yPos - 1 && mouseY < slot.yPos + 17) {
                    if (selectedSlot != i) {
                        selectedSlot = i;
                        updateSidePanel();
                    }
                    break;
                }
            }
        }
    }

    private boolean clickBrowser(int mouseX, int mouseY) {
        if (mouseX < leftX + 4 || mouseX >= leftX + leftW - 4) return false;
        if (mouseY < browserY || mouseY >= browserY + browserH) return false;

        int index = browserScroll + (mouseY - browserY) / ROW_H;
        if (index < 0 || index >= browserRows.size()) return false;

        BrowserRow row = browserRows.get(index);
        if (row.isPack && mouseX < leftX + 4 + fontRenderer.getStringWidth("[x] ")) {
            boolean enable = disabledPacks.contains(row.pack);
            if (enable) {
                disabledPacks.remove(row.pack);
            } else {
                disabledPacks.add(row.pack);
            }
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketTogglePack(row.pack, enable));
            buildBrowserRows();
            return true;
        }
        if (row.isPack) {
            selectedPack = row.pack.equals(selectedPack) ? "" : row.pack;
            txtPackName.setText(selectedPack.isEmpty() ? txtPackName.getText() : selectedPack);
            packMeta = null;
            if (!selectedPack.isEmpty()) {
                ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketRequestFileContent(selectedPack, "pack.json"));
            }
            buildBrowserRows();
        } else {
            openedPath = row.path;
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketRequestFileContent(row.pack, row.path));
        }
        return true;
    }

    private void dragFieldBar(int mouseY) {
        int max = maxFieldScroll();
        if (max <= 0) return;
        float fraction = (float) (mouseY - contentTop()) / Math.max(1, contentBottom() - contentTop());
        fieldScrollTarget = Math.max(0.0f, Math.min(max, fraction * max));
        fieldScroll = fieldScrollTarget;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        jsonEditor.mouseReleased();
        draggingFieldBar = false;
        draggingThemeSlider = -1;
        draggingThemeList = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        jsonEditor.mouseDragged(mouseY);
        if (draggingFieldBar) dragFieldBar(mouseY);
        if (draggingThemeSlider >= 0) dragThemeSlider(mouseX);
        if (draggingThemeList) dragThemeList(mouseY + Math.round(fieldScroll));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode != Keyboard.KEY_ESCAPE && jsonEditor.keyTyped(typedChar, keyCode)) return;

        if (txtPackName.textboxKeyTyped(typedChar, keyCode)) return;

        if (container.activeTab == TAB_THEME && txtThemeHex.textboxKeyTyped(typedChar, keyCode)) {
            Integer color = GuiTheme.parseColor(txtThemeHex.getText());
            if (color != null) {
                GuiTheme.apply(themeKey, color);
                themeA = (color >> 24) & 0xFF;
                float[] hsv = rgbToHsv((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
                themeH = hsv[0];
                themeS = hsv[1];
                themeV = hsv[2];
                if (!jsonDirty) refreshJsonText();
            }
            return;
        }

        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields != null) {
            for (EditorField ef : fields) {
                if (ef.field.textboxKeyTyped(typedChar, keyCode)) return;
            }
        }

        if (selectedSlot != -1 && container.activeTab == TAB_LOOT) {
            boolean changed = txtItemMin.textboxKeyTyped(typedChar, keyCode)
                    || txtItemMax.textboxKeyTyped(typedChar, keyCode)
                    || txtItemChance.textboxKeyTyped(typedChar, keyCode);
            if (changed) {
                ItemSettings set = slotSettings[selectedSlot];
                set.customized = true;
                try { set.min = Integer.parseInt(txtItemMin.getText()); } catch (Exception ignored) {}
                try { set.max = Integer.parseInt(txtItemMax.getText()); } catch (Exception ignored) {}
                try { set.chance = Double.parseDouble(txtItemChance.getText()); } catch (Exception ignored) {}
                if (!jsonDirty) refreshJsonText();
                return;
            }
        }

        if (selectedSlot != -1 && container.activeTab == TAB_RECIPES) {
            boolean changed = (txtRecipeChance.getVisible() && txtRecipeChance.textboxKeyTyped(typedChar, keyCode))
                    || (txtRecipeCost.getVisible() && txtRecipeCost.textboxKeyTyped(typedChar, keyCode))
                    || (txtRecipeNbt.getVisible() && txtRecipeNbt.textboxKeyTyped(typedChar, keyCode));
            if (changed) {
                RecipeSlotSettings set = recipeSettings[selectedSlot];
                set.touched = true;
                try { set.chance = Float.parseFloat(txtRecipeChance.getText()); } catch (Exception ignored) {}
                String costText = txtRecipeCost.getText().trim();
                if (costText.isEmpty()) {
                    set.cost = -1;
                } else {
                    try { set.cost = Integer.parseInt(costText); } catch (Exception ignored) {}
                }
                set.nbt = txtRecipeNbt.getText();
                if (!jsonDirty) refreshJsonText();
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id >= BTN_TAB_BASE) {
            container.activeTab = TAB_ORDER[button.id - BTN_TAB_BASE];
            openedPath = null;
            baseJson = null;
            jsonDirty = false;
            browserScroll = 0;
            updateTabState();
            return;
        }

        switch (button.id) {
            case BTN_SAVE:
                saveCurrent();
                break;
            case BTN_APPLY:
                applyJsonText();
                break;
            case BTN_MAP:
                this.mc.displayGuiScreen(new GuiZoneEquipment());
                break;
            case BTN_NBT:
                if (selectedSlot != -1) {
                    RecipeSlotSettings set = recipeSettings[selectedSlot];
                    set.useNbt = !set.useNbt;
                    set.touched = true;
                    ItemStack stack = getSelectedStack();
                    if (set.useNbt && set.nbt.isEmpty() && stack != null && stack.hasTagCompound()) {
                        set.nbt = stack.getTagCompound().toString();
                        txtRecipeNbt.setText(set.nbt);
                    }
                    btnNbtToggle.displayString = set.useNbt ? tr("nbt.on") : tr("nbt.off");
                    if (!jsonDirty) refreshJsonText();
                }
                break;
            case BTN_WHOLE:
                if (selectedSlot != -1) {
                    RecipeSlotSettings set = recipeSettings[selectedSlot];
                    set.consumeWhole = !set.consumeWhole;
                    set.touched = true;
                    btnWholeToggle.displayString = set.consumeWhole ? tr("whole.on") : tr("whole.off");
                    if (!jsonDirty) refreshJsonText();
                }
                break;
            default:
                break;
        }
    }

    private JsonObject buildJson() {
        if (container.activeTab == TAB_THEME) {
            return new JsonParser().parse(GuiTheme.toJson()).getAsJsonObject();
        }
        JsonObject root = baseJson == null
                ? new JsonObject()
                : new JsonParser().parse(baseJson.toString()).getAsJsonObject();
        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields != null) {
            for (EditorField ef : fields) {
                if (ef.jsonKey == null) continue;
                String text = ef.field.getText().trim();
                if (text.isEmpty()) continue;
                try {
                    switch (ef.type) {
                        case INT:
                            root.addProperty(ef.jsonKey, Integer.parseInt(text));
                            break;
                        case FLOAT:
                            root.addProperty(ef.jsonKey, Float.parseFloat(text));
                            break;
                        case BOOL:
                            root.addProperty(ef.jsonKey, Boolean.parseBoolean(text));
                            break;
                        default:
                            root.addProperty(ef.jsonKey, text);
                            break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int tab = container.activeTab;
        if (tab == TAB_LOOT) {
            putIfFilled(root, "items", buildLootItems());
        } else if (tab == TAB_RECIPES) {
            putIfFilled(root, "outputs", buildRecipeStacks(0, 9, true));
            putIfFilled(root, "inputs", buildRecipeStacks(9, 27, false));
        } else if (tab == TAB_NPC) {
            JsonObject equipment = buildEquipment();
            if (equipment.entrySet().size() > 0 || !root.has("equipment")) root.add("equipment", equipment);
        }
        return root;
    }

    private void putIfFilled(JsonObject root, String key, JsonArray array) {
        if (array.size() > 0 || !root.has(key)) root.add(key, array);
    }

    private JsonArray buildLootItems() {
        JsonArray items = new JsonArray();
        for (int i = 0; i < 27; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            ItemSettings set = slotSettings[i];

            int max = set.customized ? set.max : stack.getCount();
            int min = set.min;
            if (max < min) max = min;

            JsonObject obj = new JsonObject();
            obj.addProperty("item", stack.getItem().getRegistryName().toString());
            if (stack.getMetadata() > 0) obj.addProperty("meta", stack.getMetadata());
            obj.addProperty("min", min);
            obj.addProperty("max", max);
            obj.addProperty("chance", set.chance);
            items.add(obj);
        }
        return items;
    }

    private JsonArray buildRecipeStacks(int from, int to, boolean isOutput) {
        JsonArray array = new JsonArray();
        for (int i = from; i < to; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot == null || !slot.getHasStack()) continue;
            ItemStack stack = slot.getStack();
            RecipeSlotSettings set = recipeSettings[i];

            JsonObject obj = new JsonObject();
            obj.addProperty("item", stack.getItem().getRegistryName().toString());
            obj.addProperty("count", stack.getCount());
            if (stack.getMetadata() > 0) obj.addProperty("meta", stack.getMetadata());
            if (isOutput) obj.addProperty("chance", set.chance);
            if (!isOutput) {
                boolean wear = stack.isItemStackDamageable() && !set.consumeWhole;
                if (stack.isItemStackDamageable() && set.consumeWhole) obj.addProperty("consume_whole", true);
                if (set.cost >= 0) obj.addProperty(wear ? "durability" : "consume", set.cost);
            }
            if (set.useNbt && !set.nbt.trim().isEmpty()) obj.addProperty("nbt", set.nbt.trim());
            array.add(obj);
        }
        return array;
    }

    private JsonObject buildEquipment() {
        JsonObject equipment = new JsonObject();
        String[] slots = {"mainhand", "offhand", "head", "chest", "legs", "feet"};
        for (int i = 0; i < 6; i++) {
            Slot slot = container.inventorySlots.get(i);
            if (slot != null && slot.getHasStack()) {
                equipment.addProperty(slots[i], slot.getStack().getItem().getRegistryName().toString());
            }
        }
        return equipment;
    }

    private void refreshJsonText() {
        jsonEditor.setText(new GsonBuilder().setPrettyPrinting().create().toJson(buildJson()));
        jsonDirty = false;
    }

    private void applyJsonText() {
        JsonObject root = parseEditor();
        if (root == null) return;
        baseJson = root;
        applyJson(root);
        if (container.activeTab == TAB_THEME) {
            GuiTheme.applyJson(root);
            loadThemeSelection();
        }
        jsonDirty = false;
        formSignature = formSignature();
    }

    private JsonObject parseEditor() {
        try {
            JsonElement element = new JsonParser().parse(jsonEditor.getText());
            if (!element.isJsonObject()) throw new IllegalArgumentException("not an object");
            return element.getAsJsonObject();
        } catch (Exception e) {
            message(TextFormatting.RED + tr("msg.bad_json") + " " + e.getMessage());
            return null;
        }
    }

    private void applyJson(JsonObject root) {
        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields == null) return;
        for (EditorField ef : fields) {
            if (ef.jsonKey == null) continue;
            JsonElement element = root.get(ef.jsonKey);
            if (element != null && element.isJsonPrimitive()) {
                ef.field.setText(element.getAsString());
            }
        }
    }

    private String tabSubDir(int tab) {
        switch (tab) {
            case TAB_ITEMS: return "items";
            case TAB_BLOCK: return "blocks";
            case TAB_FOOD: return "food";
            case TAB_FUEL: return "fuels";
            case TAB_TABS: return "tabs";
            case TAB_NPC: return "npcs";
            case TAB_RECIPES: return "recipes";
            case TAB_LOOT: return "loot_tables/airdrops";
            default: return null;
        }
    }

    private String fileNameField() {
        List<EditorField> fields = tabFields.get(container.activeTab);
        if (fields == null || fields.isEmpty()) return "";
        return fields.get(0).field.getText().trim();
    }

    private void saveCurrent() {
        if (container.activeTab == TAB_THEME) {
            JsonObject root = parseEditor();
            if (root == null) return;
            GuiTheme.applyJson(root);
            loadThemeSelection();
            boolean ok = GuiTheme.save(this.mc.mcDataDir);
            message(ok
                    ? TextFormatting.GREEN + tr("msg.theme_saved")
                    : TextFormatting.RED + tr("msg.theme_failed"));
            return;
        }

        String subDir = tabSubDir(container.activeTab);
        if (subDir == null) return;

        if (parseEditor() == null) return;

        String path;
        if (openedPath != null && openedPath.startsWith(subDir + "/")) {
            path = openedPath;
        } else {
            String name = fileNameField();
            if (name.isEmpty()) name = "custom";
            if (!name.endsWith(".json")) name += ".json";
            path = subDir + "/" + name;
        }

        String packName = txtPackName.getText().trim();
        if (packName.isEmpty()) packName = "example_pack";

        ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketSaveContent(packName, path, jsonEditor.getText()));
        openedPath = path;
        ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketRequestPackList());
    }

    private void message(String text) {
        if (mc.player != null) mc.player.sendMessage(new TextComponentString(text));
    }

    private void buildBrowserRows() {
        browserRows.clear();
        String subDir = tabSubDir(container.activeTab);

        for (Map.Entry<String, List<String>> entry : packFiles.entrySet()) {
            String pack = entry.getKey();
            boolean open = pack.equals(selectedPack);
            String mark = disabledPacks.contains(pack) ? "[ ] " : "[x] ";
            browserRows.add(new BrowserRow(true, pack, null, mark + (open ? "- " : "+ ") + pack));
            if (!open || subDir == null) continue;

            for (String path : entry.getValue()) {
                if (!path.startsWith(subDir + "/")) continue;
                browserRows.add(new BrowserRow(false, pack, path, "  " + path.substring(subDir.length() + 1)));
            }
        }
        int visible = Math.max(1, browserH / ROW_H);
        browserScroll = Math.max(0, Math.min(Math.max(0, browserRows.size() - visible), browserScroll));
    }

    public void receivePackList(String json) {
        packFiles.clear();
        disabledPacks.clear();
        try {
            JsonObject root = new JsonParser().parse(json).getAsJsonObject();
            JsonObject packs = root.has("packs") ? root.getAsJsonObject("packs") : root;
            for (Map.Entry<String, JsonElement> entry : packs.entrySet()) {
                List<String> files = new ArrayList<>();
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    files.add(element.getAsString());
                }
                files.sort(String::compareToIgnoreCase);
                packFiles.put(entry.getKey(), files);
            }
            if (root.has("disabled")) {
                for (JsonElement element : root.getAsJsonArray("disabled")) {
                    disabledPacks.add(element.getAsString());
                }
            }
        } catch (Exception e) {
            message(TextFormatting.RED + "Failed to read pack list: " + e.getMessage());
        }
        buildBrowserRows();
    }

    public void receiveFileContent(String packName, String filePath, String json) {
        if ("pack.json".equals(filePath)) {
            try {
                packMeta = new Gson().fromJson(json, PackMeta.class);
            } catch (Exception e) {
                packMeta = null;
            }
            return;
        }

        openedPath = filePath;
        selectedPack = packName;
        txtPackName.setText(packName);
        jsonEditor.setText(json);
        JsonObject root = parseEditor();
        baseJson = root;
        if (root != null) applyJson(root);
        jsonDirty = false;
        formSignature = formSignature();
    }
}
