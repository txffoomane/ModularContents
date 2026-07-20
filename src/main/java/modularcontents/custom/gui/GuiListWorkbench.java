package modularcontents.custom.gui;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.block.TileEntityListWorkbench;
import modularcontents.custom.inventory.ContainerListWorkbench;
import modularcontents.custom.network.PacketCraftCancel;
import modularcontents.custom.network.PacketCraftStart;
import modularcontents.custom.recipe.IngredientStack;
import modularcontents.custom.recipe.ListWorkbenchRecipe;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiListWorkbench extends GuiContainer {

    private static final int COL_ACCENT = 0xFFFFAA00;
    private static final int COL_BORDER = 0xFF4A4A4A;
    private static final int COL_BORDER_DARK = 0xFF2A2A2A;
    private static final int COL_PANEL_L = 0xFF151515;
    private static final int COL_PANEL_R = 0xFF18181A;
    private static final int COL_SLOT_BG = 0xFF111111;
    private static final int COL_TEXT = 0xFFDDDDDD;
    private static final int COL_TEXT_DIM = 0xFF888888;
    private static final int COL_LINE = 0xFF333333;
    private static final int COL_CRAFTABLE = 0xFF55DD55;
    private static final int COL_CRAFTABLE_DIM = 0xFF2A4A2A;

    public static class FlatButton extends GuiButton {
        public FlatButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                int borderColor = this.enabled ? (this.hovered ? COL_ACCENT : COL_BORDER) : 0xFF222222;
                int bgColor = this.enabled ? (this.hovered ? 0xFF2A2A11 : COL_PANEL_L) : COL_SLOT_BG;
                int textColor = this.enabled ? (this.hovered ? COL_ACCENT : COL_TEXT) : 0xFF555555;

                if (this.enabled && !this.hovered) {
                    drawRect(this.x + 1, this.y + 1, this.x + this.width + 1, this.y + this.height + 1, 0x55000000);
                }

                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, borderColor);
                drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, bgColor);

                String text = this.displayString;
                int textWidth = mc.fontRenderer.getStringWidth(text);
                float scale = 1.0f;
                int maxTextWidth = this.width - 6;
                if (textWidth > maxTextWidth) {
                    scale = (float) maxTextWidth / (float) textWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate(this.x + this.width / 2.0f, this.y + (this.height - 8.0f * scale) / 2.0f + 0.5f, 0.0f);
                GlStateManager.scale(scale, scale, 1.0f);
                mc.fontRenderer.drawStringWithShadow(text, -textWidth / 2.0f, 0, textColor);
                GlStateManager.popMatrix();
            }
        }
    }

    private static final int PANEL_TOP = 4;
    private static final int PANEL_BOTTOM = 154;
    private static final int LEFT_X0 = 4;
    private static final int LEFT_X1 = 132;
    private static final int RIGHT_X0 = 134;
    private static final int RIGHT_X1 = 334;

    private static final int LIST_TOP = 38;
    private static final int LIST_HEIGHT = 110;
    private static final int RECIPE_ROW_HEIGHT = 20;

    private static final int GRID_COLS = 5;
    private static final int GRID_CELL = 22;

    private static final int REQ_TOP = 62;
    private static final int REQ_HEIGHT = 56;
    private static final int REQ_ROW_HEIGHT = 14;

    private static final int QUEUE_Y = 122;

    private final TileEntityListWorkbench te;
    private final ContainerListWorkbench container;
    private final InventoryPlayer playerInv;

    private List<String> categories = new ArrayList<>();
    private int currentCategoryIndex = 0;
    private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<>();

    private static final Set<String> FAVORITES = new HashSet<>();
    private static boolean favoritesLoaded = false;

    private static final int THUMB_HEIGHT = 15;
    private static final int REQ_THUMB_HEIGHT = 10;

    private float scrollPos = 0.0f;
    private float scrollTarget = 0.0f;
    private boolean isScrolling = false;
    private float scrollGrabOffset = 0.0f;

    private float reqScrollPos = 0.0f;
    private float reqScrollTarget = 0.0f;
    private boolean isReqScrolling = false;
    private float reqScrollGrabOffset = 0.0f;

    private long lastFrameTime = 0L;

    private GuiButton btnCraft;
    private GuiButton btnCatPrev;
    private GuiButton btnCatNext;
    private GuiButton btnFavorite;
    private GuiButton btnMinus;
    private GuiButton btnPlus;
    private GuiButton btnViewMode;
    private GuiTextField searchField;

    private boolean gridMode = false;

    private int selectedRecipeIndex = -1;
    private int craftAmount = 1;
    private long openTime;

    public GuiListWorkbench(InventoryPlayer playerInv, TileEntityListWorkbench te) {
        super(new ContainerListWorkbench(playerInv, te));

        loadFavorites();

        this.te = te;
        this.playerInv = playerInv;
        this.container = (ContainerListWorkbench) this.inventorySlots;
        this.xSize = 338;
        this.ySize = 240;

        this.categories.add("Favorites");
        this.categories.addAll(ListWorkbenchRecipeManager.getCategories("workbench", te.getWorkbenchId()));

        if (categories.size() > 1) {
            currentCategoryIndex = 1;
        }
        updateCategoryRecipes();
    }

    private static File getFavoritesFile() {
        return new File(Minecraft.getMinecraft().mcDataDir, "ModularContents/modularcontents_favorites.txt");
    }

    private static void loadFavorites() {
        if (favoritesLoaded) return;
        favoritesLoaded = true;

        File file = getFavoritesFile();
        if (file.exists()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                FAVORITES.clear();
                FAVORITES.addAll(lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveFavorites() {
        File file = getFavoritesFile();
        try {
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), FAVORITES, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCategoryRecipes() {
        String currentCat = categories.get(currentCategoryIndex);

        if (currentCat.equals("Favorites")) {
            currentRecipes = ListWorkbenchRecipeManager.getAllRecipes().stream().filter(r -> r.type.equalsIgnoreCase("workbench") || r.type.equalsIgnoreCase("both"))
                    .filter(r -> r.workbench != null && r.workbench.equalsIgnoreCase(te.getWorkbenchId()))
                    .filter(r -> FAVORITES.contains(r.id))
                    .collect(Collectors.toList());
        } else {
            currentRecipes = ListWorkbenchRecipeManager.getRecipesInCategory(currentCat, "workbench", te.getWorkbenchId());
        }

        if (searchField != null && !searchField.getText().trim().isEmpty()) {
            String search = searchField.getText().trim().toLowerCase();
            currentRecipes = currentRecipes.stream()
                    .filter(r -> {
                        ItemStack res = r.getPrimaryResult();
                        return !res.isEmpty() && res.getDisplayName().toLowerCase().contains(search);
                    })
                    .collect(Collectors.toList());
        }

        currentRecipes = currentRecipes.stream()
                .sorted((a, b) -> {
                    boolean ca = isCraftable(a);
                    boolean cb = isCraftable(b);
                    if (ca != cb) return ca ? -1 : 1;
                    ItemStack ra = a.getPrimaryResult();
                    ItemStack rb = b.getPrimaryResult();
                    String na = ra.isEmpty() ? "" : ra.getDisplayName();
                    String nb = rb.isEmpty() ? "" : rb.getDisplayName();
                    return na.compareToIgnoreCase(nb);
                })
                .collect(Collectors.toList());

        scrollPos = 0.0f;
        scrollTarget = 0.0f;
        reqScrollPos = 0.0f;
        reqScrollTarget = 0.0f;
        selectedRecipeIndex = -1;
        craftAmount = 1;
    }

    @Override
    public void initGui() {
        super.initGui();
        openTime = System.currentTimeMillis();
        Keyboard.enableRepeatEvents(true);

        btnCatPrev = new FlatButton(4, guiLeft + 8, guiTop + 5, 12, 12, "<");
        btnCatNext = new FlatButton(5, guiLeft + 112, guiTop + 5, 12, 12, ">");

        btnFavorite = new FlatButton(6, guiLeft + 314, guiTop + 8, 14, 14, "*");

        btnMinus = new FlatButton(8, guiLeft + 140, guiTop + 122, 14, 14, "-");
        btnPlus = new FlatButton(9, guiLeft + 184, guiTop + 122, 14, 14, "+");

        btnCraft = new FlatButton(0, guiLeft + 140, guiTop + 138, 58, 14, "Craft");

        btnViewMode = new FlatButton(10, guiLeft + 109, guiTop + 21, 16, 12, gridMode ? "G" : "L");

        this.buttonList.add(btnCraft);
        this.buttonList.add(btnCatPrev);
        this.buttonList.add(btnCatNext);
        this.buttonList.add(btnFavorite);
        this.buttonList.add(btnMinus);
        this.buttonList.add(btnPlus);
        this.buttonList.add(btnViewMode);

        searchField = new GuiTextField(7, this.fontRenderer, guiLeft + 10, guiTop + 23, 92, 10);
        searchField.setMaxStringLength(30);
        searchField.setEnableBackgroundDrawing(false);
        searchField.setTextColor(16777215);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    private ListWorkbenchRecipe getRecipeToShow() {
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < currentRecipes.size()) {
            return currentRecipes.get(selectedRecipeIndex);
        }
        if (te.isCrafting()) {
            String activeId = te.getActiveRecipeId();
            if (activeId != null && !activeId.isEmpty()) {
                return ListWorkbenchRecipeManager.getRecipe(activeId);
            }
        }
        return null;
    }

    private int getMaxScroll() {
        int contentHeight = gridMode
                ? ((currentRecipes.size() + GRID_COLS - 1) / GRID_COLS) * GRID_CELL
                : currentRecipes.size() * RECIPE_ROW_HEIGHT;
        return Math.max(0, contentHeight - LIST_HEIGHT);
    }

    private int getMaxReqScroll() {
        ListWorkbenchRecipe recipe = getRecipeToShow();
        if (recipe == null) return 0;
        return Math.max(0, recipe.inputs.size() * REQ_ROW_HEIGHT - REQ_HEIGHT);
    }

    private void enableScissor(int x, int y, int w, int h) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        int f = sr.getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * f, this.mc.displayHeight - (y + h) * f, w * f, h * f);
    }

    private float getMouseYPrecise() {
        return this.height - (float) Mouse.getY() * this.height / (float) this.mc.displayHeight - 1.0f;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            int notches = scroll / 120;
            if (notches == 0) notches = scroll > 0 ? 1 : -1;

            boolean inPanelY = mouseY >= guiTop + PANEL_TOP && mouseY <= guiTop + PANEL_BOTTOM;
            if (inPanelY && mouseX >= guiLeft + RIGHT_X0 && mouseX <= guiLeft + RIGHT_X1) {
                int max = getMaxReqScroll();
                if (max > 0) {
                    reqScrollTarget = Math.max(0.0f, Math.min(reqScrollTarget - notches * REQ_ROW_HEIGHT, max));
                }
            } else if (inPanelY && mouseX >= guiLeft + LEFT_X0 && mouseX <= guiLeft + LEFT_X1) {
                int max = getMaxScroll();
                int step = gridMode ? GRID_CELL : RECIPE_ROW_HEIGHT;
                if (max > 0) {
                    scrollTarget = Math.max(0.0f, Math.min(scrollTarget - notches * step, max));
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();

        ListWorkbenchRecipe recipeToShow = getRecipeToShow();

        btnCatPrev.enabled = currentCategoryIndex > 0;
        btnCatNext.enabled = currentCategoryIndex < categories.size() - 1;

        if (recipeToShow != null) {
            btnFavorite.visible = true;
            boolean isFav = FAVORITES.contains(recipeToShow.id);
            btnFavorite.displayString = isFav ? "★" : "*";

            btnMinus.visible = true;
            btnPlus.visible = true;
            btnMinus.enabled = craftAmount > 1;

            int maxAffordable = getMaxAffordable(recipeToShow);
            btnPlus.enabled = craftAmount < maxAffordable && craftAmount < 64;

            if (craftAmount > maxAffordable && maxAffordable > 0) {
                craftAmount = maxAffordable;
            } else if (maxAffordable == 0) {
                craftAmount = 1;
            }

            btnCraft.enabled = maxAffordable > 0 && craftAmount <= maxAffordable && te.hasFreeQueueSlot();
        } else {
            btnCraft.enabled = false;
            btnFavorite.visible = false;
            btnMinus.visible = false;
            btnPlus.visible = false;
        }
    }

    private boolean isCraftable(ListWorkbenchRecipe recipe) {
        return getMaxAffordable(recipe) > 0;
    }

    private int getMaxAffordable(ListWorkbenchRecipe recipe) {
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
        return max;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            updateCategoryRecipes();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        ListWorkbenchRecipe recipeToShow = getRecipeToShow();

        if (button.id == 0 && recipeToShow != null) {
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketCraftStart(recipeToShow.id, te.getPos(), craftAmount));
        } else if (button.id == 4 && currentCategoryIndex > 0) {
            currentCategoryIndex--;
            updateCategoryRecipes();
        } else if (button.id == 5 && currentCategoryIndex < categories.size() - 1) {
            currentCategoryIndex++;
            updateCategoryRecipes();
        } else if (button.id == 6 && recipeToShow != null) {
            String id = recipeToShow.id;
            if (FAVORITES.contains(id)) FAVORITES.remove(id);
            else FAVORITES.add(id);

            saveFavorites();

            if (categories.get(currentCategoryIndex).equals("Favorites")) {
                updateCategoryRecipes();
            }
        } else if (button.id == 8 && craftAmount > 1) {
            craftAmount--;
        } else if (button.id == 9) {
            craftAmount++;
        } else if (button.id == 10) {
            gridMode = !gridMode;
            btnViewMode.displayString = gridMode ? "G" : "L";
            scrollTarget = Math.min(scrollTarget, getMaxScroll());
            scrollPos = scrollTarget;
        }
    }

    private int countItemInInventory(IngredientStack requiredIng) {
        int count = 0;
        for (int i = 0; i < playerInv.mainInventory.size(); i++) {
            ItemStack stack = playerInv.mainInventory.get(i);
            if (requiredIng.matches(stack)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        float animProgress = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 200.0f);
        // ease-out cubic
        animProgress = 1.0f - (float) Math.pow(1.0f - animProgress, 3);

        GlStateManager.pushMatrix();
        if (animProgress < 1.0f) {
            float scale = 0.8f + 0.2f * animProgress;
            float cx = this.width / 2.0f;
            float cy = this.height / 2.0f;
            GlStateManager.translate(cx, cy, 0);
            GlStateManager.scale(scale, scale, 1.0f);
            GlStateManager.translate(-cx, -cy, 0);

            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, animProgress);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        if (gridMode) {
            int hoverIndex = getGridIndexAt(mouseX, mouseY);
            if (hoverIndex >= 0) {
                ItemStack hoverResult = currentRecipes.get(hoverIndex).getPrimaryResult();
                if (!hoverResult.isEmpty()) {
                    this.renderToolTip(hoverResult, mouseX, mouseY);
                }
            }
        }

        int qy = guiTop + QUEUE_Y;
        for (int i = 0; i < TileEntityListWorkbench.QUEUE_SIZE; i++) {
            int qx = getQueueSlotX(i);
            if (mouseX >= qx && mouseX < qx + 16 && mouseY >= qy && mouseY < qy + 16) {
                String queuedId = te.getQueueRecipeId(i);
                if (!queuedId.isEmpty()) {
                    ListWorkbenchRecipe queued = ListWorkbenchRecipeManager.getRecipe(queuedId);
                    List<String> lines = new ArrayList<>();
                    ItemStack icon = queued != null ? queued.getPrimaryResult() : ItemStack.EMPTY;
                    lines.add(!icon.isEmpty() ? icon.getDisplayName() : queuedId);
                    lines.add("§7Queued: " + te.getQueueCount(i));
                    lines.add(i == 0 && te.isCrafting() ? "§aCrafting..." : "§7Waiting");
                    lines.add("§cClick to cancel");
                    this.drawHoveringText(lines, mouseX, mouseY);
                }
                break;
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawSlotBox(int x, int y) {
        drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF3A3A3A);
        drawRect(x, y, x + 16, y + 16, COL_SLOT_BG);
        drawRect(x, y, x + 16, y + 1, 0x88000000);
        drawRect(x, y, x + 1, y + 16, 0x88000000);
    }

    private void drawListRecipes(int listTop, int mouseX, int mouseY) {
        int firstRow = Math.max(0, (int) (scrollPos / RECIPE_ROW_HEIGHT));
        int lastRow = Math.min(currentRecipes.size() - 1, (int) ((scrollPos + LIST_HEIGHT) / RECIPE_ROW_HEIGHT));

        for (int index = firstRow; index <= lastRow; index++) {
            ListWorkbenchRecipe recipe = currentRecipes.get(index);
            int rowY = listTop + index * RECIPE_ROW_HEIGHT;
            float visualY = rowY - scrollPos;

            boolean isHovered = mouseX >= guiLeft + 7 && mouseX <= guiLeft + 121
                    && mouseY >= visualY && mouseY <= visualY + 19
                    && mouseY >= listTop && mouseY <= listTop + LIST_HEIGHT;
            boolean craftable = isCraftable(recipe);
            int bgBorder = (index == selectedRecipeIndex) ? COL_ACCENT : (isHovered ? COL_BORDER : (craftable ? COL_CRAFTABLE_DIM : COL_BORDER_DARK));
            int bgFill = (index == selectedRecipeIndex) ? 0xFF2A2A11 : (isHovered ? 0xFF222222 : (craftable ? 0xFF16221A : 0xFF181818));

            drawRect(guiLeft + 6, rowY, guiLeft + 122, rowY + 19, bgBorder);
            drawRect(guiLeft + 7, rowY + 1, guiLeft + 121, rowY + 18, bgFill);

            if (craftable && index != selectedRecipeIndex) {
                drawRect(guiLeft + 7, rowY + 1, guiLeft + 8, rowY + 18, COL_CRAFTABLE);
            }

            ItemStack result = recipe.getPrimaryResult();
            if (!result.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.75f, 0.75f, 1.0f);
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(result, (int) ((guiLeft + 10) / 0.75f), (int) ((rowY + 3) / 0.75f));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                String name = this.fontRenderer.trimStringToWidth(result.getDisplayName(), 92);
                int tColor = (index == selectedRecipeIndex) ? COL_ACCENT : (craftable ? COL_CRAFTABLE : COL_TEXT);
                this.fontRenderer.drawString(name, guiLeft + 26, rowY + 6, tColor);
            }
        }
    }

    private void drawGridRecipes(int listTop, int mouseX, int mouseY) {
        int gridLeft = guiLeft + 8;

        int firstRow = Math.max(0, (int) (scrollPos / GRID_CELL));
        int lastRow = (int) ((scrollPos + LIST_HEIGHT) / GRID_CELL);
        int firstIndex = firstRow * GRID_COLS;
        int lastIndex = Math.min(currentRecipes.size() - 1, (lastRow + 1) * GRID_COLS - 1);

        for (int index = firstIndex; index <= lastIndex; index++) {
            ListWorkbenchRecipe recipe = currentRecipes.get(index);
            ItemStack result = recipe.getPrimaryResult();
            if (result.isEmpty()) continue;

            int col = index % GRID_COLS;
            int row = index / GRID_COLS;
            int cx = gridLeft + col * GRID_CELL;
            int cy = listTop + row * GRID_CELL;
            float visualY = cy - scrollPos;

            boolean isHovered = mouseX >= cx && mouseX < cx + 20
                    && mouseY >= visualY && mouseY < visualY + 20
                    && mouseY >= listTop && mouseY <= listTop + LIST_HEIGHT;
            boolean craftable = isCraftable(recipe);
            int bgBorder = (index == selectedRecipeIndex) ? COL_ACCENT : (isHovered ? COL_BORDER : (craftable ? COL_CRAFTABLE : COL_BORDER_DARK));
            int bgFill = (index == selectedRecipeIndex) ? 0xFF2A2A11 : (isHovered ? 0xFF222222 : (craftable ? 0xFF16221A : 0xFF181818));

            drawRect(cx, cy, cx + 20, cy + 20, bgBorder);
            drawRect(cx + 1, cy + 1, cx + 19, cy + 19, bgFill);

            RenderHelper.enableGUIStandardItemLighting();
            this.itemRender.renderItemAndEffectIntoGUI(result, cx + 2, cy + 2);
            RenderHelper.disableStandardItemLighting();
        }
    }

    private int getGridIndexAt(int mouseX, int mouseY) {
        int listTop = guiTop + LIST_TOP;
        if (mouseY < listTop || mouseY >= listTop + LIST_HEIGHT) return -1;

        int gridLeft = guiLeft + 8;
        int localX = mouseX - gridLeft;
        if (localX < 0) return -1;

        int col = localX / GRID_CELL;
        if (col < 0 || col >= GRID_COLS || localX - col * GRID_CELL >= 20) return -1;

        int row = (int) ((mouseY - listTop + scrollPos) / GRID_CELL);
        float cellTop = listTop + row * GRID_CELL - scrollPos;
        if (mouseY >= cellTop + 20) return -1;

        int index = row * GRID_COLS + col;
        if (index < 0 || index >= currentRecipes.size()) return -1;
        return index;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        long now = System.nanoTime();
        float dt = lastFrameTime == 0L ? 0.016f : Math.min(0.1f, (now - lastFrameTime) / 1.0e9f);
        lastFrameTime = now;
        float blend = 1.0f - (float) Math.exp(-16.0f * dt);

        if (isScrolling) {
            int maxScroll = getMaxScroll();
            if (maxScroll > 0) {
                float thumbTop = getMouseYPrecise() - scrollGrabOffset;
                float frac = (thumbTop - (guiTop + LIST_TOP)) / (float) (LIST_HEIGHT - THUMB_HEIGHT);
                frac = Math.max(0.0f, Math.min(frac, 1.0f));
                scrollTarget = frac * maxScroll;
                scrollPos = scrollTarget;
            }
        }
        if (isReqScrolling) {
            int maxReqScroll = getMaxReqScroll();
            if (maxReqScroll > 0) {
                float thumbTop = getMouseYPrecise() - reqScrollGrabOffset;
                float frac = (thumbTop - (guiTop + REQ_TOP)) / (float) (REQ_HEIGHT - REQ_THUMB_HEIGHT);
                frac = Math.max(0.0f, Math.min(frac, 1.0f));
                reqScrollTarget = frac * maxReqScroll;
                reqScrollPos = reqScrollTarget;
            }
        }

        scrollTarget = Math.max(0.0f, Math.min(scrollTarget, getMaxScroll()));
        reqScrollTarget = Math.max(0.0f, Math.min(reqScrollTarget, getMaxReqScroll()));

        scrollPos += (scrollTarget - scrollPos) * blend;
        if (Math.abs(scrollTarget - scrollPos) < 0.1f) scrollPos = scrollTarget;

        reqScrollPos += (reqScrollTarget - reqScrollPos) * blend;
        if (Math.abs(reqScrollTarget - reqScrollPos) < 0.1f) reqScrollPos = reqScrollTarget;

        drawRect(guiLeft + 3, guiTop + 3, guiLeft + 335, guiTop + 155, COL_BORDER);
        drawRect(guiLeft + LEFT_X0, guiTop + PANEL_TOP, guiLeft + LEFT_X1, guiTop + PANEL_BOTTOM, COL_PANEL_L);
        drawRect(guiLeft + RIGHT_X0, guiTop + PANEL_TOP, guiLeft + RIGHT_X1, guiTop + PANEL_BOTTOM, COL_PANEL_R);

        this.drawGradientRect(guiLeft + LEFT_X0, guiTop + 4, guiLeft + LEFT_X1, guiTop + 18, COL_BORDER_DARK, COL_PANEL_L);
        drawRect(guiLeft + LEFT_X0, guiTop + 18, guiLeft + LEFT_X1, guiTop + 19, COL_LINE);

        drawRect(guiLeft + 79, guiTop + 154, guiLeft + 259, guiTop + 238, COL_BORDER);
        drawRect(guiLeft + 80, guiTop + 155, guiLeft + 258, guiTop + 237, COL_PANEL_R);

        int invX = guiLeft + 88;
        int invY = guiTop + 158;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                drawSlotBox(invX + j * 18, invY + i * 18);
            }
        }
        for (int k = 0; k < 9; ++k) {
            drawSlotBox(invX + k * 18, guiTop + 216);
        }

        drawRect(guiLeft + 7, guiTop + 21, guiLeft + 106, guiTop + 33, COL_BORDER);
        drawRect(guiLeft + 8, guiTop + 22, guiLeft + 105, guiTop + 32, COL_PANEL_L);
        drawRect(guiLeft + 8, guiTop + 22, guiLeft + 105, guiTop + 23, 0x55000000);
        searchField.drawTextBox();

        if (!categories.isEmpty()) {
            String catName = categories.get(currentCategoryIndex).toUpperCase();
            int strWidth = this.fontRenderer.getStringWidth(catName);
            int centerCatX = guiLeft + LEFT_X0 + (LEFT_X1 - LEFT_X0 - strWidth) / 2;
            this.fontRenderer.drawStringWithShadow(catName, centerCatX, guiTop + 8, COL_ACCENT);
        }

        int listTop = guiTop + LIST_TOP;
        int scrollX = guiLeft + 124;
        drawRect(scrollX, listTop, scrollX + 3, listTop + LIST_HEIGHT, COL_SLOT_BG);

        int maxScroll = getMaxScroll();
        float scrollFrac = maxScroll > 0 ? scrollPos / maxScroll : 0.0f;
        int thumbY = listTop + (int) (scrollFrac * (LIST_HEIGHT - THUMB_HEIGHT));
        drawRect(scrollX, thumbY, scrollX + 3, thumbY + THUMB_HEIGHT, isScrolling ? COL_ACCENT : 0xFF555555);

        if (currentRecipes.isEmpty()) {
            String empty = "No recipes";
            int w = this.fontRenderer.getStringWidth(empty);
            this.fontRenderer.drawString(empty, guiLeft + LEFT_X0 + (LEFT_X1 - LEFT_X0 - w) / 2, listTop + 46, 0xFF555555);
        } else {
            enableScissor(guiLeft + LEFT_X0, listTop, LEFT_X1 - LEFT_X0, LIST_HEIGHT);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0f, -scrollPos, 0.0f);

            if (gridMode) {
                drawGridRecipes(listTop, mouseX, mouseY);
            } else {
                drawListRecipes(listTop, mouseX, mouseY);
            }

            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        boolean isCrafting = container.clientProgress > 0 || te.isCrafting();
        ListWorkbenchRecipe recipeToShow = getRecipeToShow();

        if (recipeToShow != null) {
            int rightX = guiLeft + 140;
            ItemStack primaryResult = recipeToShow.getPrimaryResult();

            this.drawGradientRect(guiLeft + RIGHT_X0, guiTop + 4, guiLeft + RIGHT_X1, guiTop + 44, COL_BORDER_DARK, COL_PANEL_R);
            drawRect(guiLeft + RIGHT_X0, guiTop + 44, guiLeft + RIGHT_X1, guiTop + 45, COL_LINE);

            if (!primaryResult.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0f, 2.0f, 1.0f);
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(primaryResult, (int) (rightX / 2.0f), (int) ((guiTop + 8) / 2.0f));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                String displayName = this.fontRenderer.trimStringToWidth(primaryResult.getDisplayName(), 130);
                this.fontRenderer.drawStringWithShadow(displayName, rightX + 38, guiTop + 10, COL_ACCENT);
            }

            int progress = Math.max(container.clientProgress, te.getProgress());
            int total = Math.max(container.clientTotalTime, te.getTotalTime());
            boolean showingActive = isCrafting && recipeToShow.id != null && recipeToShow.id.equals(te.getActiveRecipeId());
            int displayTime = showingActive ? (total - progress) : recipeToShow.craftingTime;

            int totalSeconds = displayTime / 20;
            String timeString = String.format("Time: %d:%02d", totalSeconds / 60, totalSeconds % 60);

            int timerColor = showingActive ? 0xFF55FF55 : 0xFFBBBBBB;
            this.fontRenderer.drawString(timeString, rightX + 38, guiTop + 22, timerColor);

            if (showingActive && total > 0) {
                int barX0 = rightX + 38;
                int barX1 = guiLeft + 326;
                drawRect(barX0, guiTop + 33, barX1, guiTop + 38, COL_SLOT_BG);
                float frac = Math.max(0.0f, Math.min(1.0f, (float) progress / (float) total));
                drawRect(barX0 + 1, guiTop + 34, barX0 + 1 + (int) ((barX1 - barX0 - 2) * frac), guiTop + 37, COL_ACCENT);
            } else if (recipeToShow.outputs != null && (recipeToShow.outputs.size() > 1 || recipeToShow.hasChanceOutputs())) {
                int yieldX = rightX + 38;
                int shown = 0;
                for (IngredientStack out : recipeToShow.outputs) {
                    if (out == null) continue;
                    ItemStack res = out.toItemStack();
                    if (res.isEmpty()) continue;
                    if (shown >= 3) break;

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.75f, 0.75f, 1.0f);
                    RenderHelper.enableGUIStandardItemLighting();
                    this.itemRender.renderItemAndEffectIntoGUI(res, (int) (yieldX / 0.75f), (int) ((guiTop + 31) / 0.75f));
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();

                    String label = out.chance < 100.0f ? (int) out.chance + "%" : "x" + res.getCount();
                    int labelColor = out.chance < 100.0f ? COL_ACCENT : 0xFFBBBBBB;
                    this.fontRenderer.drawString(label, yieldX + 13, guiTop + 33, labelColor);
                    yieldX += 15 + this.fontRenderer.getStringWidth(label) + 6;
                    shown++;
                }
            }

            this.fontRenderer.drawString("Item", rightX, guiTop + 48, COL_TEXT_DIM);
            String haveNeed = "Have / Need";
            this.fontRenderer.drawString(haveNeed, guiLeft + 328 - this.fontRenderer.getStringWidth(haveNeed), guiTop + 48, COL_TEXT_DIM);
            drawRect(guiLeft + RIGHT_X0, guiTop + 58, guiLeft + RIGHT_X1, guiTop + 59, COL_LINE);

            int totalReqs = recipeToShow.inputs.size();
            int reqListTop = guiTop + REQ_TOP;

            int maxReqScroll = getMaxReqScroll();
            if (maxReqScroll > 0) {
                int reqScrollX = guiLeft + 328;
                float reqFrac = reqScrollPos / maxReqScroll;
                drawRect(reqScrollX, reqListTop, reqScrollX + 3, reqListTop + REQ_HEIGHT, COL_SLOT_BG);

                int reqThumbY = reqListTop + (int) (reqFrac * (REQ_HEIGHT - REQ_THUMB_HEIGHT));
                drawRect(reqScrollX, reqThumbY, reqScrollX + 3, reqThumbY + REQ_THUMB_HEIGHT, isReqScrolling ? COL_ACCENT : 0xFF555555);
            }

            enableScissor(guiLeft + RIGHT_X0, reqListTop, RIGHT_X1 - RIGHT_X0, REQ_HEIGHT);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0f, -reqScrollPos, 0.0f);

            int firstReq = Math.max(0, (int) (reqScrollPos / REQ_ROW_HEIGHT));
            int lastReq = Math.min(totalReqs - 1, (int) ((reqScrollPos + REQ_HEIGHT) / REQ_ROW_HEIGHT));

            for (int i = firstReq; i <= lastReq; i++) {
                IngredientStack ing = recipeToShow.inputs.get(i);
                ItemStack reqStack = ing.toItemStack();
                if (reqStack.isEmpty()) continue;

                int rowY = reqListTop + i * REQ_ROW_HEIGHT;

                if (i % 2 == 0) drawRect(guiLeft + 137, rowY - 1, guiLeft + 324, rowY + 13, 0x11FFFFFF);

                GlStateManager.pushMatrix();
                GlStateManager.scale(0.75f, 0.75f, 1.0f);
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(reqStack, (int) ((rightX + 1) / 0.75f), (int) (rowY / 0.75f));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                String ingName = this.fontRenderer.trimStringToWidth(reqStack.getDisplayName(), 110);
                this.fontRenderer.drawString(ingName, rightX + 18, rowY + 3, COL_TEXT);

                int have = countItemInInventory(ing);
                int need = ing.count;
                int totalNeed = need * Math.max(1, craftAmount);
                int color = have >= totalNeed ? 0xFF55FF55 : 0xFFFF5555;

                String counts = have + " / " + totalNeed;
                this.fontRenderer.drawString(counts, guiLeft + 320 - this.fontRenderer.getStringWidth(counts), rowY + 3, color);
            }

            GlStateManager.popMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            drawRect(guiLeft + RIGHT_X0, guiTop + 119, guiLeft + RIGHT_X1, guiTop + 120, COL_LINE);

            String amountStr = String.valueOf(craftAmount);
            int strW = this.fontRenderer.getStringWidth(amountStr);
            this.fontRenderer.drawString(amountStr, guiLeft + 169 - strW / 2, guiTop + 125, 0xFFFFFFFF);
        } else {
            String hint = "Select a recipe";
            int w = this.fontRenderer.getStringWidth(hint);
            this.fontRenderer.drawString(hint, guiLeft + (RIGHT_X0 + RIGHT_X1) / 2 - w / 2, guiTop + 72, 0xFF555555);

            drawRect(guiLeft + RIGHT_X0, guiTop + 119, guiLeft + RIGHT_X1, guiTop + 120, COL_LINE);
        }

        drawQueueSlots(mouseX, mouseY);

        int outX = guiLeft + 260;
        int outY = guiTop + 122;
        for (int i = 0; i < TileEntityListWorkbench.OUTPUT_SLOTS; i++) {
            drawRect(outX + i * 18 - 1, outY - 1, outX + i * 18 + 17, outY + 17, COL_BORDER);
            drawRect(outX + i * 18, outY, outX + i * 18 + 16, outY + 16, COL_PANEL_L);
            drawRect(outX + i * 18, outY, outX + i * 18 + 16, outY + 1, 0x55000000);
            drawRect(outX + i * 18, outY, outX + i * 18 + 1, outY + 16, 0x55000000);
        }
        String outLabel = "Output";
        int outWidth = TileEntityListWorkbench.OUTPUT_SLOTS * 18 - 2;
        this.fontRenderer.drawString(outLabel, outX + outWidth / 2 - this.fontRenderer.getStringWidth(outLabel) / 2, outY + 20, COL_TEXT_DIM);
    }

    private int getQueueSlotX(int index) {
        return guiLeft + 202 + index * 18;
    }

    private void drawQueueSlots(int mouseX, int mouseY) {
        int qy = guiTop + QUEUE_Y;

        for (int i = 0; i < TileEntityListWorkbench.QUEUE_SIZE; i++) {
            int qx = getQueueSlotX(i);
            String queuedId = te.getQueueRecipeId(i);
            boolean occupied = !queuedId.isEmpty();
            boolean hovered = occupied && mouseX >= qx && mouseX < qx + 16 && mouseY >= qy && mouseY < qy + 16;

            int border = hovered ? 0xFFFF5555 : (occupied ? COL_ACCENT : COL_BORDER);
            drawRect(qx - 1, qy - 1, qx + 17, qy + 17, border);
            drawRect(qx, qy, qx + 16, qy + 16, COL_SLOT_BG);
            drawRect(qx, qy, qx + 16, qy + 1, 0x88000000);
            drawRect(qx, qy, qx + 1, qy + 16, 0x88000000);

            if (occupied) {
                ListWorkbenchRecipe queued = ListWorkbenchRecipeManager.getRecipe(queuedId);
                if (queued != null) {
                    ItemStack icon = queued.getPrimaryResult();
                    if (!icon.isEmpty()) {
                        RenderHelper.enableGUIStandardItemLighting();
                        this.itemRender.renderItemAndEffectIntoGUI(icon, qx, qy);
                        RenderHelper.disableStandardItemLighting();
                    }
                }

                GlStateManager.disableDepth();

                if (i == 0) {
                    int progress = Math.max(container.clientProgress, te.getProgress());
                    int total = Math.max(container.clientTotalTime, te.getTotalTime());
                    if (total > 0) {
                        float frac = Math.max(0.0f, Math.min(1.0f, (float) progress / (float) total));
                        drawRect(qx, qy + 14, qx + (int) (16 * frac), qy + 16, 0xFF55FF55);
                    }
                }

                int count = te.getQueueCount(i);
                if (count > 1) {
                    String countStr = String.valueOf(count);
                    this.fontRenderer.drawStringWithShadow(countStr, qx + 17 - this.fontRenderer.getStringWidth(countStr), qy - 2, 0xFFFFFFFF);
                }

                if (hovered) {
                    drawRect(qx, qy, qx + 16, qy + 16, 0x60FF3333);
                }

                GlStateManager.enableDepth();
            }
        }

        String queueLabel = "Queue";
        this.fontRenderer.drawString(queueLabel, getQueueSlotX(0) + 26 - this.fontRenderer.getStringWidth(queueLabel) / 2, qy + 20, COL_TEXT_DIM);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int qy = guiTop + QUEUE_Y;
            for (int i = 0; i < TileEntityListWorkbench.QUEUE_SIZE; i++) {
                int qx = getQueueSlotX(i);
                if (mouseX >= qx && mouseX < qx + 16 && mouseY >= qy && mouseY < qy + 16) {
                    if (!te.getQueueRecipeId(i).isEmpty()) {
                        ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketCraftCancel(te.getPos(), i));
                    }
                    return;
                }
            }

            int listTop = guiTop + LIST_TOP;
            int scrollX = guiLeft + 124;

            if (getMaxScroll() > 0 && mouseX >= scrollX - 2 && mouseX <= scrollX + 5 && mouseY >= listTop && mouseY <= listTop + LIST_HEIGHT) {
                float frac = scrollPos / getMaxScroll();
                float thumbY = listTop + frac * (LIST_HEIGHT - THUMB_HEIGHT);
                if (mouseY >= thumbY && mouseY <= thumbY + THUMB_HEIGHT) {
                    scrollGrabOffset = mouseY - thumbY;
                } else {
                    scrollGrabOffset = THUMB_HEIGHT / 2.0f;
                }
                isScrolling = true;
                return;
            }

            if (getMaxReqScroll() > 0) {
                int reqScrollX = guiLeft + 328;
                int reqListTop = guiTop + REQ_TOP;
                if (mouseX >= reqScrollX - 2 && mouseX <= reqScrollX + 5 && mouseY >= reqListTop && mouseY <= reqListTop + REQ_HEIGHT) {
                    float frac = reqScrollPos / getMaxReqScroll();
                    float thumbY = reqListTop + frac * (REQ_HEIGHT - REQ_THUMB_HEIGHT);
                    if (mouseY >= thumbY && mouseY <= thumbY + REQ_THUMB_HEIGHT) {
                        reqScrollGrabOffset = mouseY - thumbY;
                    } else {
                        reqScrollGrabOffset = REQ_THUMB_HEIGHT / 2.0f;
                    }
                    isReqScrolling = true;
                    return;
                }
            }

            if (gridMode) {
                int index = getGridIndexAt(mouseX, mouseY);
                if (index >= 0) {
                    selectedRecipeIndex = index;
                    craftAmount = 1;
                    reqScrollPos = 0.0f;
                    reqScrollTarget = 0.0f;
                }
            } else if (mouseX >= guiLeft + 7 && mouseX <= guiLeft + 121 && mouseY >= listTop && mouseY < listTop + LIST_HEIGHT) {
                int index = (int) ((mouseY - listTop + scrollPos) / RECIPE_ROW_HEIGHT);
                if (index >= 0 && index < currentRecipes.size()) {
                    float rowY = listTop + index * RECIPE_ROW_HEIGHT - scrollPos;
                    if (mouseY >= rowY && mouseY <= rowY + 19) {
                        selectedRecipeIndex = index;
                        craftAmount = 1;
                        reqScrollPos = 0.0f;
                        reqScrollTarget = 0.0f;
                    }
                }
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0) {
            isScrolling = false;
            isReqScrolling = false;
        }
    }
}
