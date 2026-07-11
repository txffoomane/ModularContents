import java.nio.file.Files;
import java.nio.file.Paths;

public class FixGui {
    public static void main(String[] args) throws Exception {
        String code = new String(Files.readAllBytes(Paths.get("src/main/java/modularcontents/custom/gui/GuiListWorkbench.java")), "UTF-8");
        code = code.replace("package modularcontents.custom.gui;", "package modularcontents.custom.client.gui;");
        code = code.replace("class GuiListWorkbench", "class GuiHandcraft");
        code = code.replace("GuiListWorkbench(", "GuiHandcraft(");
        code = code.replace("import modularcontents.custom.block.TileEntityListWorkbench;", "");
        code = code.replace("import modularcontents.custom.inventory.ContainerListWorkbench;", "import modularcontents.custom.inventory.ContainerHandcraft;\nimport modularcontents.custom.network.PacketHandcraftAction;");
        code = code.replace("ContainerListWorkbench", "ContainerHandcraft");
        code = code.replaceAll("private final TileEntityListWorkbench te;\s*", "");
        code = code.replaceAll(", TileEntityListWorkbench te", "");
        code = code.replaceAll("this\.te = te;\s*", "");
        
        code = code.replace("te.isCrafting()", "(container.activeRecipeId != null && !container.activeRecipeId.isEmpty())");
        code = code.replace("te.getActiveRecipeId()", "container.activeRecipeId");
        
        code = code.replaceAll("Math\.max\(container\.clientProgress, te\.getProgress\(\)\)", "container.clientProgress");
        code = code.replaceAll("Math\.max\(container\.clientTotalTime, te\.getTotalTime\(\)\)", "container.clientTotalTime");
        
        code = code.replace("ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketCraftStart(recipeToShow.id, te.getPos(), craftAmount));", 
            "ModularcontentsMod.PACKET_HANDLER.sendToServer(new PacketHandcraftAction(recipeToShow.id, craftAmount));");
        code = code.replace("te.hasFreeQueueSlot()", "true");
        
        code = code.replaceAll("(?s)private void drawQueueSlots.*?^    }", "");
        code = code.replaceAll("(?s)private int getQueueSlotX.*?^    }", "");
        code = code.replaceAll("drawQueueSlots\(mouseX, mouseY\);", "");
        
        code = code.replaceAll("(?s)int qy = guiTop \+ QUEUE_Y;.*?return;\n\s*}", "");
        code = code.replaceAll("(?s)int qy = guiTop \+ QUEUE_Y;.*?\n\s*}", "");
        code = code.replace("TileEntityListWorkbench.OUTPUT_SLOTS", "3");
        
        Files.write(Paths.get("src/main/java/modularcontents/custom/client/gui/GuiHandcraft.java"), code.getBytes("UTF-8"));
    }
}
