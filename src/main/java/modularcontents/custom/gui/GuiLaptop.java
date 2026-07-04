package modularcontents.custom.gui;

import com.google.common.base.Predicate;
import modularcontents.ModularcontentsMod;
import modularcontents.custom.network.PacketLaptopAirdrop;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiLaptop extends GuiScreen {

    // Style colors
    private static final int COL_ACCENT = 0xFFFFAA00;
    private static final int COL_BORDER = 0xFF4A4A4A;
    private static final int COL_PANEL = 0xFF151515;
    private static final int COL_TEXT = 0xFFDDDDDD;
    private static final int COL_TEXT_DIM = 0xFF888888;

    private final BlockPos laptopPos;
    private final World world;
    private DynamicTexture minimapTexture;
    private int textureId = -1;

    private static final int MIN_RANGE = 25;
    private static final int MAX_RANGE = 200;

    private int selectedX = 0;
    private int selectedZ = 0;
    private boolean hasSelection = false;
    private FlatButton callButton;
    private FlatButton goButton;
    private GuiTextField xField;
    private GuiTextField zField;

    // Minimap specs
    private final int TEX_SIZE = 400;
    private final int mapRadius = TEX_SIZE / 2; // 200 blocks
    private final int DISPLAY_SIZE = 160; // smaller map display

    private boolean[] waterMask;

    private float currentZoom = 1.0f;
    private float targetZoom = 1.0f;

    private float panX = 0.0f;
    private float panZ = 0.0f;
    private boolean dragging = false;
    private boolean dragMoved = false;
    private int lastDragRawX = 0;
    private int lastDragRawY = 0;
    private int dragStartRawX = 0;
    private int dragStartRawY = 0;

    public GuiLaptop(World world, BlockPos pos) {
        this.world = world;
        this.laptopPos = pos;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        int startX = (this.width - panelWidth()) / 2;
        int startY = (this.height - panelHeight()) / 2;

        int inputY = startY + DISPLAY_SIZE + 26;

        this.xField = new GuiTextField(1, this.fontRenderer, startX + 18, inputY, 48, 14);
        this.zField = new GuiTextField(2, this.fontRenderer, startX + 78, inputY, 48, 14);
        this.xField.setMaxStringLength(12);
        this.zField.setMaxStringLength(12);
        Predicate<String> intOnly = s -> s != null && s.matches("-?\\d*");
        this.xField.setValidator(intOnly);
        this.zField.setValidator(intOnly);

        this.goButton = new FlatButton(1, startX + 130, inputY, 40, 14, "Go");

        this.callButton = new FlatButton(0, startX + 10, startY + DISPLAY_SIZE + 46, DISPLAY_SIZE, 24, "Call Airdrop");
        this.callButton.enabled = false;

        this.buttonList.add(this.goButton);
        this.buttonList.add(this.callButton);

        // Generate minimap texture
        if (this.textureId == -1) {
            generateMinimap();
        }
    }

    private int panelWidth() {
        return DISPLAY_SIZE + 20;
    }

    private int panelHeight() {
        return DISPLAY_SIZE + 78;
    }

    private void generateMinimap() {
        minimapTexture = new DynamicTexture(TEX_SIZE, TEX_SIZE);
        int[] pixels = minimapTexture.getTextureData();
        waterMask = new boolean[TEX_SIZE * TEX_SIZE];

        for (int i = 0; i < TEX_SIZE; i++) {
            for (int j = 0; j < TEX_SIZE; j++) {
                int wx = laptopPos.getX() - mapRadius + i;
                int wz = laptopPos.getZ() - mapRadius + j;

                int y = world.getHeight(wx, wz);
                BlockPos p = new BlockPos(wx, y > 0 ? y - 1 : 0, wz);
                IBlockState state = world.getBlockState(p);
                int color = state.getMapColor(world, p).colorValue;

                boolean water = state.getMaterial() == Material.WATER;
                waterMask[j * TEX_SIZE + i] = water;

                if (wx % 32 == 0 || wz % 32 == 0) {
                    color = 0x00FF00;
                }

                if (water) {
                    int or = (color >> 16) & 0xFF;
                    int og = (color >> 8) & 0xFF;
                    int ob = color & 0xFF;
                    color = (Math.min(255, or / 2 + 130) << 16) | ((og / 3) << 8) | (ob / 3);
                }

                double dx = i - mapRadius;
                double dz = j - mapRadius;
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist < MIN_RANGE || dist > MAX_RANGE) {
                    int or = (color >> 16) & 0xFF;
                    int og = (color >> 8) & 0xFF;
                    int ob = color & 0xFF;
                    color = ((or * 35 / 100) << 16) | ((og * 35 / 100) << 8) | (ob * 35 / 100);
                }

                if (Math.abs(dist - MIN_RANGE) < 0.9 || Math.abs(dist - MAX_RANGE) < 0.9) {
                    color = 0xFFAA00;
                }

                pixels[j * TEX_SIZE + i] = 0xFF000000 | color;
            }
        }
        minimapTexture.updateDynamicTexture();
        this.textureId = minimapTexture.getGlTextureId();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            if (scroll > 0) {
                targetZoom = Math.min(4.0f, targetZoom + 0.5f);
            } else {
                targetZoom = Math.max(1.0f, targetZoom - 0.5f);
            }
        }
    }

    private int getMapX() {
        return (this.width - panelWidth()) / 2 + 10;
    }

    private int getMapY() {
        return (this.height - panelHeight()) / 2 + 20;
    }

    private boolean isWaterAt(int x, int z) {
        if (waterMask == null) return false;
        int ti = x - (laptopPos.getX() - mapRadius);
        int tj = z - (laptopPos.getZ() - mapRadius);
        if (ti < 0 || ti >= TEX_SIZE || tj < 0 || tj >= TEX_SIZE) return false;
        return waterMask[tj * TEX_SIZE + ti];
    }

    private double distanceFromLaptop(int x, int z) {
        double dx = x - laptopPos.getX();
        double dz = z - laptopPos.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private boolean isValidTarget(int x, int z) {
        double dist = distanceFromLaptop(x, z);
        return dist >= MIN_RANGE && dist <= MAX_RANGE && !isWaterAt(x, z);
    }

    private void updateSelection(int x, int z) {
        this.selectedX = x;
        this.selectedZ = z;
        this.hasSelection = true;

        if (this.xField != null) this.xField.setText(String.valueOf(x));
        if (this.zField != null) this.zField.setText(String.valueOf(z));

        if (isValidTarget(x, z)) {
            this.callButton.enabled = true;
            this.callButton.displayString = "Call: X=" + x + " Z=" + z;
        } else {
            this.callButton.enabled = false;
            double dist = distanceFromLaptop(x, z);
            if (isWaterAt(x, z)) {
                this.callButton.displayString = "Blocked: water";
            } else if (dist < MIN_RANGE) {
                this.callButton.displayString = "Too close (min " + MIN_RANGE + ")";
            } else {
                this.callButton.displayString = "Too far (max " + MAX_RANGE + ")";
            }
        }
    }

    private void applyManualCoords() {
        try {
            int x = Integer.parseInt(xField.getText().trim());
            int z = Integer.parseInt(zField.getText().trim());
            updateSelection(x, z);
        } catch (NumberFormatException ignored) {
        }
    }

    private float visibleTexSize() {
        return TEX_SIZE / currentZoom;
    }

    private float texOffset(float pan) {
        float vis = visibleTexSize();
        float max = TEX_SIZE - vis;
        float off = max / 2.0f + pan;
        if (off < 0.0f) off = 0.0f;
        if (off > max) off = max;
        return off;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Smooth zoom interpolation
        currentZoom += (targetZoom - currentZoom) * 0.2f;

        if (this.dragging) {
            int rawX = Mouse.getX();
            int rawY = Mouse.getY();
            int rdx = rawX - this.lastDragRawX;
            int rdy = rawY - this.lastDragRawY;
            if (rdx != 0 || rdy != 0) {
                float scaleFactor = new ScaledResolution(this.mc).getScaleFactor();
                float perRawPixel = (visibleTexSize() / (float) DISPLAY_SIZE) / scaleFactor;
                this.panX -= rdx * perRawPixel;
                this.panZ += rdy * perRawPixel;
            }
            if (Math.abs(rawX - this.dragStartRawX) + Math.abs(rawY - this.dragStartRawY) > 3) {
                this.dragMoved = true;
            }
            this.lastDragRawX = rawX;
            this.lastDragRawY = rawY;
        }

        float maxOff = Math.max(0.0f, TEX_SIZE - visibleTexSize());
        float halfOff = maxOff / 2.0f;
        panX = Math.max(-halfOff, Math.min(panX, halfOff));
        panZ = Math.max(-halfOff, Math.min(panZ, halfOff));

        int panelWidth = panelWidth();
        int panelHeight = panelHeight();
        int startX = (this.width - panelWidth) / 2;
        int startY = (this.height - panelHeight) / 2;

        // Draw Panel Background
        drawRect(startX, startY, startX + panelWidth, startY + panelHeight, COL_BORDER);
        drawRect(startX + 1, startY + 1, startX + panelWidth - 1, startY + panelHeight - 1, COL_PANEL);

        this.drawCenteredString(this.fontRenderer, "Airdrop Tactical Map", this.width / 2, startY + 6, COL_ACCENT);

        int mapX = startX + 10;
        int mapY = startY + 20;

        // Draw Map Border
        drawRect(mapX - 1, mapY - 1, mapX + DISPLAY_SIZE + 1, mapY + DISPLAY_SIZE + 1, COL_BORDER);
        drawRect(mapX, mapY, mapX + DISPLAY_SIZE, mapY + DISPLAY_SIZE, 0xFF111111);

        float visibleTexSize = visibleTexSize();
        float texU = texOffset(panX);
        float texV = texOffset(panZ);

        // Draw Map Texture with Zoom
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.textureId != -1) {
            GlStateManager.bindTexture(this.textureId);

            float minU = texU / (float) TEX_SIZE;
            float minV = texV / (float) TEX_SIZE;
            float maxU = (texU + visibleTexSize) / (float) TEX_SIZE;
            float maxV = (texV + visibleTexSize) / (float) TEX_SIZE;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(mapX, mapY + DISPLAY_SIZE, this.zLevel).tex(minU, maxV).endVertex();
            buffer.pos(mapX + DISPLAY_SIZE, mapY + DISPLAY_SIZE, this.zLevel).tex(maxU, maxV).endVertex();
            buffer.pos(mapX + DISPLAY_SIZE, mapY, this.zLevel).tex(maxU, minV).endVertex();
            buffer.pos(mapX, mapY, this.zLevel).tex(minU, minV).endVertex();
            tessellator.draw();
        }

        // Draw Player Location using skin face
        if (this.mc.player != null) {
            float pTexX = (float) this.mc.player.posX - (laptopPos.getX() - mapRadius);
            float pTexZ = (float) this.mc.player.posZ - (laptopPos.getZ() - mapRadius);
            float pRelX = (pTexX - texU) / visibleTexSize;
            float pRelZ = (pTexZ - texV) / visibleTexSize;

            if (pRelX >= 0 && pRelX <= 1.0f && pRelZ >= 0 && pRelZ <= 1.0f) {
                int px = mapX + (int) (pRelX * DISPLAY_SIZE);
                int py = mapY + (int) (pRelZ * DISPLAY_SIZE);
                drawPlayerFace(px, py);
            }
        }

        // Draw Selection
        if (hasSelection) {
            float selRelX = (selectedX - (laptopPos.getX() - mapRadius) - texU) / visibleTexSize;
            float selRelY = (selectedZ - (laptopPos.getZ() - mapRadius) - texV) / visibleTexSize;

            if (selRelX >= 0 && selRelX <= 1.0f && selRelY >= 0 && selRelY <= 1.0f) {
                int px = mapX + (int)(selRelX * DISPLAY_SIZE);
                int py = mapY + (int)(selRelY * DISPLAY_SIZE);
                int markColor = isValidTarget(selectedX, selectedZ) ? COL_ACCENT : 0xFFFF3030;
                drawRect(px - 3, py - 1, px + 4, py + 2, markColor);
                drawRect(px - 1, py - 3, px + 2, py + 4, markColor);
            }
        }

        this.fontRenderer.drawString("X", startX + 10, mapY + DISPLAY_SIZE + 9, COL_TEXT_DIM);
        this.fontRenderer.drawString("Z", startX + 70, mapY + DISPLAY_SIZE + 9, COL_TEXT_DIM);
        this.xField.drawTextBox();
        this.zField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw Hover Coordinates Preview
        if (mouseX >= mapX && mouseX < mapX + DISPLAY_SIZE && mouseY >= mapY && mouseY < mapY + DISPLAY_SIZE) {
            float relX = (mouseX - mapX) / (float)DISPLAY_SIZE;
            float relY = (mouseY - mapY) / (float)DISPLAY_SIZE;

            float hoverTexX = texU + relX * visibleTexSize;
            float hoverTexY = texV + relY * visibleTexSize;

            int hoverBlockX = laptopPos.getX() - mapRadius + (int)hoverTexX;
            int hoverBlockZ = laptopPos.getZ() - mapRadius + (int)hoverTexY;

            String hoverStr = "X: " + hoverBlockX + " Z: " + hoverBlockZ;
            int strW = this.fontRenderer.getStringWidth(hoverStr);

            // Draw a small background for the tooltip
            drawRect(mouseX + 8, mouseY - 14, mouseX + 12 + strW, mouseY - 2, 0xCC000000);
            this.fontRenderer.drawString(hoverStr, mouseX + 10, mouseY - 12, COL_ACCENT);
        }
    }

    private void drawPlayerFace(int cx, int cy) {
        int size = 12;
        int x = cx - size / 2;
        int y = cy - size / 2;

        drawRect(x - 2, y - 2, x + size + 2, y + size + 2, 0xFF000000);
        drawRect(x - 1, y - 1, x + size + 1, y + size + 1, COL_ACCENT);

        net.minecraft.util.ResourceLocation skin = this.mc.player.getLocationSkin();
        this.mc.getTextureManager().bindTexture(skin);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawScaledCustomSizeModalRect(x, y, 8.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        drawScaledCustomSizeModalRect(x, y, 40.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
        GlStateManager.disableBlend();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.xField.mouseClicked(mouseX, mouseY, mouseButton);
        this.zField.mouseClicked(mouseX, mouseY, mouseButton);

        int mapX = getMapX();
        int mapY = getMapY();

        if (mouseButton == 0 && mouseX >= mapX && mouseX < mapX + DISPLAY_SIZE && mouseY >= mapY && mouseY < mapY + DISPLAY_SIZE) {
            this.dragging = true;
            this.dragMoved = false;
            this.lastDragRawX = Mouse.getX();
            this.lastDragRawY = Mouse.getY();
            this.dragStartRawX = this.lastDragRawX;
            this.dragStartRawY = this.lastDragRawY;
        }
    }

    @Override
    public void updateScreen() {
        this.xField.updateCursorCounter();
        this.zField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.xField.textboxKeyTyped(typedChar, keyCode)) return;
        if (this.zField.textboxKeyTyped(typedChar, keyCode)) return;
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            applyManualCoords();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        if (state == 0 && this.dragging) {
            this.dragging = false;

            int mapX = getMapX();
            int mapY = getMapY();
            boolean insideMap = mouseX >= mapX && mouseX < mapX + DISPLAY_SIZE && mouseY >= mapY && mouseY < mapY + DISPLAY_SIZE;

            if (!this.dragMoved && insideMap) {
                float visibleTexSize = visibleTexSize();
                float texU = texOffset(panX);
                float texV = texOffset(panZ);

                float relX = (mouseX - mapX) / (float) DISPLAY_SIZE;
                float relY = (mouseY - mapY) / (float) DISPLAY_SIZE;

                float clickTexX = texU + relX * visibleTexSize;
                float clickTexY = texV + relY * visibleTexSize;

                int tx = laptopPos.getX() - mapRadius + (int) clickTexX;
                int tz = laptopPos.getZ() - mapRadius + (int) clickTexY;
                updateSelection(tx, tz);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 && hasSelection && isValidTarget(selectedX, selectedZ)) {
            ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketLaptopAirdrop(laptopPos, selectedX, selectedZ));
            this.mc.displayGuiScreen(null);
        } else if (button.id == 1) {
            applyManualCoords();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static class FlatButton extends GuiButton {
        public FlatButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (this.visible) {
                this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

                int borderColor = this.enabled ? (this.hovered ? COL_ACCENT : COL_BORDER) : 0xFF222222;
                int bgColor = this.enabled ? (this.hovered ? 0xFF2A2A11 : 0xFF111111) : 0xFF111111;
                int textColor = this.enabled ? (this.hovered ? COL_ACCENT : COL_TEXT) : 0xFF555555;

                drawRect(this.x, this.y, this.x + this.width, this.y + this.height, borderColor);
                drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, bgColor);

                this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, textColor);
            }
        }
    }
}