package modularcontents.custom.gui;

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
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiListWorkbench extends GuiContainer {

    public static class FlatButton extends GuiButton {
        public FlatButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                // Crisper, EFT-like colors
                int borderColor = this.enabled ? (this.hovered ? 0xFFFFAA00 : 0xFF4A4A4A) : 0xFF222222;
                int bgColor = this.enabled ? (this.hovered ? 0xFF2A2A11 : 0xFF151515) : 0xFF111111;
                int textColor = this.enabled ? (this.hovered ? 0xFFFFAA00 : 0xFFDDDDDD) : 0xFF555555;

                // Shadow effect
                if (this.enabled && !this.hovered) {
                    drawRect(this.x + 1, this.y + 1, this.x + this.width + 1, this.y + this.height + 1, 0x55000000);
                }

                // Border
                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, borderColor);
                // Background
                drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, bgColor);

                this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
            }
        }
    }

    private final TileEntityListWorkbench te;
    private final ContainerListWorkbench container;
    private final InventoryPlayer playerInv;

    private List<String> categories = new ArrayList<>();
    private int currentCategoryIndex = 0;
    private List<ListWorkbenchRecipe> currentRecipes = new ArrayList<>();

    // Favorites I/O
    private static final Set<String> FAVORITES = new HashSet<>();
    private static boolean favoritesLoaded = false;

    private int scrollOffset = 0;
    private float currentScroll = 0.0f;
    private boolean isScrolling = false;
    private static final int MAX_VISIBLE_RECIPES = 5;

    // Ingredients Scroll
    private int reqScrollOffset = 0;
    private float currentReqScroll = 0.0f;
    private boolean isReqScrolling = false;
    private static final int MAX_VISIBLE_REQS = 3;

    private GuiButton btnCraft;
    private GuiButton btnCancel;
    private GuiButton btnCatPrev;
    private GuiButton btnCatNext;
    private GuiButton btnFavorite;
    private GuiButton btnMinus;
    private GuiButton btnPlus;
    private GuiTextField searchField;

    private int selectedRecipeIndex = -1;
    private int craftAmount = 1;

    public GuiListWorkbench(InventoryPlayer playerInv, TileEntityListWorkbench te) {
        super(new ContainerListWorkbench(playerInv, te));

        loadFavorites();

        this.te = te;
        this.playerInv = playerInv;
        this.container = (ContainerListWorkbench) this.inventorySlots;
        this.xSize = 256;
        this.ySize = 256;

        this.categories.add("Favorites");
        this.categories.addAll(ListWorkbenchRecipeManager.getCategories());

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
            currentRecipes = ListWorkbenchRecipeManager.getAllRecipes().stream()
                    .filter(r -> FAVORITES.contains(r.id))
                    .collect(Collectors.toList());
        } else {
            currentRecipes = ListWorkbenchRecipeManager.getRecipesInCategory(currentCat);
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

        scrollOffset = 0;
        currentScroll = 0.0f;
        reqScrollOffset = 0;
        currentReqScroll = 0.0f;
        selectedRecipeIndex = -1;
        craftAmount = 1;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        // Header Category buttons (Left Panel)
        btnCatPrev = new FlatButton(4, guiLeft + 8, guiTop + 5, 12, 12, "<");
        btnCatNext = new FlatButton(5, guiLeft + 106, guiTop + 5, 12, 12, ">");

        // Favorite button (Top-left of right block)
        int rightBlockLeft = guiLeft + 125;
        btnFavorite = new FlatButton(6, rightBlockLeft + 5, guiTop + 8, 14, 14, "*");

        // Bottom-Right Controls
        int ctrlX = guiLeft + 130;

        // Plus / Minus row
        btnMinus = new FlatButton(8, ctrlX, guiTop + 106, 14, 14, "-");
        btnPlus = new FlatButton(9, ctrlX + 42, guiTop + 106, 14, 14, "+");

        // Craft / Cancel row
        btnCraft = new FlatButton(0, ctrlX, guiTop + 122, 28, 14, "Craft");
        btnCancel = new FlatButton(1, ctrlX + 30, guiTop + 122, 28, 14, "Cancel");

        this.buttonList.add(btnCraft);
        this.buttonList.add(btnCancel);
        this.buttonList.add(btnCatPrev);
        this.buttonList.add(btnCatNext);
        this.buttonList.add(btnFavorite);
        this.buttonList.add(btnMinus);
        this.buttonList.add(btnPlus);

        // Search field (Left Panel)
        searchField = new GuiTextField(7, this.fontRenderer, guiLeft + 8, guiTop + 20, 110, 14);
        searchField.setMaxStringLength(30);
        searchField.setEnableBackgroundDrawing(true);
        searchField.setTextColor(16777215);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    private ListWorkbenchRecipe getRecipeToShow() {
        boolean isCrafting = container.clientProgress > 0 || te.isCrafting();
        if (isCrafting) {
            String activeId = te.getActiveRecipeId();
            if (activeId != null && !activeId.isEmpty()) {
                return ListWorkbenchRecipeManager.getRecipe(activeId);
            }
        }
        if (selectedRecipeIndex >= 0 && selectedRecipeIndex < currentRecipes.size()) {
            return currentRecipes.get(selectedRecipeIndex);
        }
        return null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;

            if (mouseX >= guiLeft + 125 && mouseX <= guiLeft + 251) {
                // Scroll requirements list
                ListWorkbenchRecipe recipeToShow = getRecipeToShow();
                if (recipeToShow != null) {
                    int totalHidden = recipeToShow.inputs.size() - MAX_VISIBLE_REQS;
                    if (totalHidden > 0) {
                        if (scroll > 0) reqScrollOffset--;
                        else if (scroll < 0) reqScrollOffset++;

                        reqScrollOffset = Math.max(0, Math.min(reqScrollOffset, totalHidden));
                        currentReqScroll = (float) reqScrollOffset / totalHidden;
                    }
                }
            } else {
                // Scroll recipes list
                int totalHidden = currentRecipes.size() - MAX_VISIBLE_RECIPES;
                if (totalHidden > 0) {
                    if (scroll > 0) {
                        scrollOffset--;
                    } else if (scroll < 0) {
                        scrollOffset++;
                    }
                    scrollOffset = Math.max(0, Math.min(scrollOffset, totalHidden));
                    currentScroll = (float) scrollOffset / totalHidden;
                }
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();

        boolean isCrafting = container.clientProgress > 0 || te.isCrafting();
        ListWorkbenchRecipe recipeToShow = getRecipeToShow();

        btnCancel.enabled = isCrafting;

        btnCatPrev.enabled = currentCategoryIndex > 0;
        btnCatNext.enabled = currentCategoryIndex < categories.size() - 1;

        if (recipeToShow != null) {
            btnFavorite.visible = true;
            boolean isFav = FAVORITES.contains(recipeToShow.id);
            btnFavorite.displayString = isFav ? "\u2605" : "*";

            btnMinus.visible = !isCrafting;
            btnPlus.visible = !isCrafting;
            btnMinus.enabled = craftAmount > 1;

            int maxAffordable = getMaxAffordable(recipeToShow);
            btnPlus.enabled = craftAmount < maxAffordable && craftAmount < 64;

            if (craftAmount > maxAffordable && maxAffordable > 0) {
                craftAmount = maxAffordable;
            } else if (maxAffordable == 0) {
                craftAmount = 1;
            }

            // Fix: Only enable Craft button if player can actually afford the selected amount
            btnCraft.enabled = !isCrafting && maxAffordable > 0 && craftAmount <= maxAffordable;
        } else {
            btnCraft.enabled = false;
            btnFavorite.visible = false;
            btnMinus.visible = false;
            btnPlus.visible = false;
        }
    }

    private int getMaxAffordable(ListWorkbenchRecipe recipe) {
        int max = 64;

        // Group ingredients by their display name/type to properly calculate max for duplicate ingredients
        java.util.Map<String, Integer> requiredTotals = new java.util.HashMap<>();
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

        if (button.id == 0 && recipeToShow != null) { // Craft
            modularcontents.ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketCraftStart(recipeToShow.id, te.getPos(), craftAmount));
            // Removed client-side te.startCrafting() to fix ghost queue desync
        } else if (button.id == 1) { // Cancel
            modularcontents.ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketCraftCancel(te.getPos()));
            // Removed client-side te.resetCrafting() to rely on server sync
        } else if (button.id == 4 && currentCategoryIndex > 0) { // Prev Category
            currentCategoryIndex--;
            updateCategoryRecipes();
        } else if (button.id == 5 && currentCategoryIndex < categories.size() - 1) { // Next Category
            currentCategoryIndex++;
            updateCategoryRecipes();
        } else if (button.id == 6 && recipeToShow != null) { // Toggle Favorite
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
        }
    }

    private int countItemInInventory(IngredientStack requiredIng) {
        ItemStack required = requiredIng.toItemStack();
        if (required.isEmpty()) return 0;
        int count = 0;
        for (int i = 0; i < playerInv.mainInventory.size(); i++) {
            ItemStack stack = playerInv.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == required.getItem()) {
                if (requiredIng.meta == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE || requiredIng.meta == stack.getMetadata()) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Overall dark border wrap around top section
        drawRect(guiLeft + 3, guiTop + 3, guiLeft + 253, guiTop + 142, 0xFF4A4A4A);

        // --- LEFT BLOCK (List) ---
        drawRect(guiLeft + 4, guiTop + 4, guiLeft + 123, guiTop + 141, 0xFF151515);

        // --- RIGHT BLOCK (Details) ---
        drawRect(guiLeft + 124, guiTop + 4, guiLeft + 252, guiTop + 141, 0xFF18181A);

        // Left Panel Header Gradient (Subtle 3D pop)
        this.drawGradientRect(guiLeft + 4, guiTop + 4, guiLeft + 123, guiTop + 18, 0xFF2A2A2A, 0xFF151515);
        drawRect(guiLeft + 4, guiTop + 18, guiLeft + 123, guiTop + 19, 0xFF333333);

        // --- BOTTOM BLOCK (Player Inventory) ---
        // Add a nice dark frame for the vanilla texture block
        drawRect(guiLeft + 38, guiTop + 141, guiLeft + 218, guiTop + 241, 0xFF4A4A4A);
        drawRect(guiLeft + 39, guiTop + 142, guiLeft + 217, guiTop + 240, 0xFF18181A);

        // Draw custom dark background for player inventory slots instead of vanilla texture
        int invX = guiLeft + 48;
        int invY = guiTop + 157;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                int slotX = invX + j * 18;
                int slotY = invY + i * 18;
                drawRect(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF3A3A3A); // border
                drawRect(slotX, slotY, slotX + 16, slotY + 16, 0xFF111111); // bg
                // Inner drop shadow
                drawRect(slotX, slotY, slotX + 16, slotY + 1, 0x88000000);
                drawRect(slotX, slotY, slotX + 1, slotY + 16, 0x88000000);
            }
        }

        // Draw custom dark background for hotbar slots
        int hotbarY = guiTop + 215;
        for (int k = 0; k < 9; ++k) {
            int slotX = invX + k * 18;
            drawRect(slotX - 1, hotbarY - 1, slotX + 17, hotbarY + 17, 0xFF3A3A3A); // border
            drawRect(slotX, hotbarY, slotX + 16, hotbarY + 16, 0xFF111111); // bg
            // Inner drop shadow
            drawRect(slotX, hotbarY, slotX + 16, hotbarY + 1, 0x88000000);
            drawRect(slotX, hotbarY, slotX + 1, hotbarY + 16, 0x88000000);
        }

        // --- DRAW LEFT BLOCK CONTENT ---
        // Style the search field manually with a border
        drawRect(guiLeft + 7, guiTop + 19, guiLeft + 119, guiTop + 35, 0xFF4A4A4A);
        drawRect(guiLeft + 8, guiTop + 20, guiLeft + 118, guiTop + 34, 0xFF151515);
        drawRect(guiLeft + 8, guiTop + 20, guiLeft + 118, guiTop + 21, 0x55000000); // inner shadow
        searchField.setEnableBackgroundDrawing(false);
        searchField.drawTextBox();

        if (!categories.isEmpty()) {
            String catName = categories.get(currentCategoryIndex).toUpperCase();
            int strWidth = this.fontRenderer.getStringWidth(catName);
            int centerCatX = guiLeft + 4 + (119 - strWidth) / 2;
            this.fontRenderer.drawStringWithShadow(catName, centerCatX, guiTop + 8, 0xFFFFAA00);
        }

        // Scrollbar List
        int scrollX = guiLeft + 117;
        int scrollYStart = guiTop + 38;
        int scrollHeight = 98;
        // Track
        drawRect(scrollX, scrollYStart, scrollX + 3, scrollYStart + scrollHeight, 0xFF111111);

        // Thumb
        int thumbHeight = 15;
        int thumbY = scrollYStart + (int)(currentScroll * (scrollHeight - thumbHeight));
        drawRect(scrollX, thumbY, scrollX + 3, thumbY + thumbHeight, isScrolling ? 0xFFFFAA00 : 0xFF555555);

        int startY = guiTop + 38;
        for (int i = 0; i < MAX_VISIBLE_RECIPES && i + scrollOffset < currentRecipes.size(); i++) {
            int index = i + scrollOffset;
            ListWorkbenchRecipe recipe = currentRecipes.get(index);
            int rowY = startY + i * 20;

            boolean isHovered = mouseX >= guiLeft + 8 && mouseX <= guiLeft + 112 && mouseY >= rowY && mouseY <= rowY + 18;
            int bgBorder = (index == selectedRecipeIndex) ? 0xFFFFAA00 : (isHovered ? 0xFF4A4A4A : 0xFF2A2A2A);
            int bgFill = (index == selectedRecipeIndex) ? 0xFF2A2A11 : (isHovered ? 0xFF222222 : 0xFF181818);

            drawRect(guiLeft + 6, rowY, guiLeft + 114, rowY + 19, bgBorder);
            drawRect(guiLeft + 7, rowY + 1, guiLeft + 113, rowY + 18, bgFill);

            ItemStack result = recipe.getPrimaryResult();
            if (!result.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.75f, 0.75f, 1.0f);
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(result, (int)((guiLeft + 9) / 0.75f), (int)((rowY + 2) / 0.75f));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                String name = result.getDisplayName();
                if (name.length() > 14) name = name.substring(0, 13) + "...";
                int tColor = (index == selectedRecipeIndex) ? 0xFFFFAA00 : 0xFFDDDDDD;
                this.fontRenderer.drawString(name, guiLeft + 25, rowY + 5, tColor);
            }
        }

        // --- DRAW RIGHT BLOCK CONTENT ---
        boolean isCrafting = container.clientProgress > 0 || te.isCrafting();
        ListWorkbenchRecipe recipeToShow = getRecipeToShow();

        if (recipeToShow != null) {
            int rightX = guiLeft + 130;
            ItemStack primaryResult = recipeToShow.getPrimaryResult();

            // Header Section Gradient
            this.drawGradientRect(guiLeft + 124, guiTop + 4, guiLeft + 252, guiTop + 40, 0xFF2A2A2A, 0xFF18181A);
            drawRect(guiLeft + 124, guiTop + 40, guiLeft + 252, guiTop + 41, 0xFF333333);

            if (!primaryResult.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0f, 2.0f, 1.0f);
                RenderHelper.enableGUIStandardItemLighting();
                this.itemRender.renderItemAndEffectIntoGUI(primaryResult, (int)((rightX + 22) / 2.0f), (int)((guiTop + 6) / 2.0f));
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();

                String displayName = primaryResult.getDisplayName();
                if (displayName.length() > 13) displayName = displayName.substring(0, 12) + ".";
                this.fontRenderer.drawStringWithShadow(displayName, rightX + 60, guiTop + 10, 0xFFFFAA00);
            }

            int progress = Math.max(container.clientProgress, te.getProgress());
            int total = Math.max(container.clientTotalTime, te.getTotalTime());
            int displayTime = isCrafting ? (total - progress) : recipeToShow.craftingTime;

            int totalSeconds = displayTime / 20;
            String timeString = String.format("Time: %d:%02d", totalSeconds / 60, totalSeconds % 60);

            int timerColor = isCrafting ? 0xFF55FF55 : 0xFFBBBBBB;
            this.fontRenderer.drawString(timeString, rightX + 60, guiTop + 22, timerColor);

            // Requirements Table Header
            int reqY = guiTop + 46;
            this.fontRenderer.drawString("Item", rightX, reqY, 0xFF888888);
            this.fontRenderer.drawString("Have / Need", rightX + 75, reqY, 0xFF888888);
            reqY += 10;
            drawRect(guiLeft + 124, reqY, guiLeft + 252, reqY + 1, 0xFF333333);
            reqY += 4;

            // Requirements List
            int totalReqs = recipeToShow.inputs.size();

            // Draw requirement scrollbar if needed
            if (totalReqs > MAX_VISIBLE_REQS) {
                int reqScrollX = rightX + 116;
                int reqScrollYStart = guiTop + 60;
                int reqScrollHeight = 35;
                drawRect(reqScrollX, reqScrollYStart, reqScrollX + 3, reqScrollYStart + reqScrollHeight, 0xFF111111);

                int reqThumbHeight = 8;
                int reqThumbY = reqScrollYStart + (int)(currentReqScroll * (reqScrollHeight - reqThumbHeight));
                drawRect(reqScrollX, reqThumbY, reqScrollX + 3, reqThumbY + reqThumbHeight, isReqScrolling ? 0xFFFFAA00 : 0xFF555555);
            }

            for (int i = 0; i < MAX_VISIBLE_REQS && i + reqScrollOffset < totalReqs; i++) {
                IngredientStack ing = recipeToShow.inputs.get(i + reqScrollOffset);
                ItemStack reqStack = ing.toItemStack();
                if (!reqStack.isEmpty()) {
                    // Row alternating background
                    if (i % 2 == 0) drawRect(rightX - 3, reqY - 1, rightX + 114, reqY + 13, 0x11FFFFFF);

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.75f, 0.75f, 1.0f);
                    RenderHelper.enableGUIStandardItemLighting();
                    this.itemRender.renderItemAndEffectIntoGUI(reqStack, (int)((rightX + 2) / 0.75f), (int)(reqY / 0.75f));
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();

                    String ingName = reqStack.getDisplayName();
                    if (ingName.length() > 10) ingName = ingName.substring(0, 9) + ".";
                    this.fontRenderer.drawString(ingName, rightX + 18, reqY + 2, 0xFFDDDDDD);

                    int have = countItemInInventory(ing);
                    int need = ing.count;
                    int amountMultiplier = Math.max(1, isCrafting ? 1 : craftAmount);
                    int totalNeed = need * amountMultiplier;
                    int color = have >= totalNeed ? 0xFF55FF55 : 0xFFFF5555;

                    this.fontRenderer.drawString(have + " / " + totalNeed, rightX + 85, reqY + 2, color);

                    reqY += 14;
                }
            }

            // Yields Panel (if applicable)
            List<ItemStack> allResults = recipeToShow.getResults();
            if (allResults.size() > 1) {
                this.fontRenderer.drawString("Yields:", rightX, reqY + 2, 0xFFFFAA00);
                int yieldX = rightX + 40;
                for (ItemStack res : allResults) {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(0.75f, 0.75f, 1.0f);
                    RenderHelper.enableGUIStandardItemLighting();
                    this.itemRender.renderItemAndEffectIntoGUI(res, (int)(yieldX / 0.75f), (int)((reqY + 2) / 0.75f));
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.popMatrix();
                    this.fontRenderer.drawString("x" + res.getCount(), yieldX + 13, reqY + 2, 0xFFFFFFFF);
                    yieldX += 30;
                }
            }

            // Bottom Right controls (Quantity text)
            if (!isCrafting) {
                String amountStr = String.valueOf(craftAmount);
                int strW = this.fontRenderer.getStringWidth(amountStr);
                this.fontRenderer.drawString(amountStr, guiLeft + 151 - (strW/2), guiTop + 109, 0xFFFFFFFF);
            } else {
                int queued = Math.max(container.clientQueuedCrafts, te.getQueuedCrafts());
                if (queued > 0) {
                    this.fontRenderer.drawString("Q: " + queued, guiLeft + 143, guiTop + 109, 0xFFFFAA00);
                }
            }
        } else {
            this.fontRenderer.drawString("Select a recipe", guiLeft + 155, guiTop + 50, 0xFF555555);
        }

        // --- 3 OUTPUT SLOTS ---
        int outX = guiLeft + 195;
        for (int i = 0; i < 3; i++) {
            drawRect(outX + i * 18 - 1, guiTop + 106, outX + i * 18 + 17, guiTop + 124, 0xFF4A4A4A); // lighter border for outputs
            drawRect(outX + i * 18, guiTop + 107, outX + i * 18 + 16, guiTop + 123, 0xFF151515); // slightly lighter bg
            // inner shadow
            drawRect(outX + i * 18, guiTop + 107, outX + i * 18 + 16, guiTop + 108, 0x55000000);
            drawRect(outX + i * 18, guiTop + 107, outX + i * 18 + 1, guiTop + 123, 0x55000000);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int scrollX = guiLeft + 117;
            int scrollYStart = guiTop + 38;
            int scrollHeight = 98;

            if (mouseX >= scrollX && mouseX <= scrollX + 4 && mouseY >= scrollYStart && mouseY <= scrollYStart + scrollHeight) {
                isScrolling = true;
                return;
            }

            // Req scroll click check
            ListWorkbenchRecipe recipeToShow = getRecipeToShow();
            if (recipeToShow != null && recipeToShow.inputs.size() > MAX_VISIBLE_REQS) {
                int reqScrollX = guiLeft + 130 + 115;
                int reqScrollYStart = guiTop + 54;
                int reqScrollHeight = 42;
                if (mouseX >= reqScrollX && mouseX <= reqScrollX + 4 && mouseY >= reqScrollYStart && mouseY <= reqScrollYStart + reqScrollHeight) {
                    isReqScrolling = true;
                    return;
                }
            }

            int startY = guiTop + 38;
            for (int i = 0; i < MAX_VISIBLE_RECIPES && i + scrollOffset < currentRecipes.size(); i++) {
                int rowY = startY + i * 20;
                if (mouseX >= guiLeft + 8 && mouseX <= guiLeft + 112 && mouseY >= rowY && mouseY <= rowY + 18) {
                    selectedRecipeIndex = i + scrollOffset;
                    craftAmount = 1;
                    reqScrollOffset = 0;
                    currentReqScroll = 0.0f;
                    break;
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (isScrolling) {
            int scrollYStart = guiTop + 38;
            int scrollHeight = 98;
            currentScroll = ((float)(mouseY - scrollYStart)) / scrollHeight;
            currentScroll = Math.max(0.0f, Math.min(currentScroll, 1.0f));

            int totalHidden = currentRecipes.size() - MAX_VISIBLE_RECIPES;
            if (totalHidden > 0) {
                scrollOffset = Math.round(currentScroll * totalHidden);
            }
        } else if (isReqScrolling) {
            int reqScrollYStart = guiTop + 54;
            int reqScrollHeight = 42;
            currentReqScroll = ((float)(mouseY - reqScrollYStart)) / reqScrollHeight;
            currentReqScroll = Math.max(0.0f, Math.min(currentReqScroll, 1.0f));

            ListWorkbenchRecipe recipeToShow = getRecipeToShow();
            if (recipeToShow != null) {
                int totalHidden = recipeToShow.inputs.size() - MAX_VISIBLE_REQS;
                if (totalHidden > 0) {
                    reqScrollOffset = Math.round(currentReqScroll * totalHidden);
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
