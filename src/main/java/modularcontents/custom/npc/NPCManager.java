package modularcontents.custom.npc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class NPCManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<String, CustomNPCInfo> NPCS = new HashMap<>();

    public static void loadNPCs(File gameDir) {
        NPCS.clear();
        File rootDir = new File(gameDir, "ModularContents");
        if (!rootDir.exists()) {
            return;
        }

        File[] packs = rootDir.listFiles(File::isDirectory);
        if (packs == null) return;

        for (File packDir : packs) {
            if (packDir.getName().equals("generated")) continue;

            File npcDir = new File(packDir, "npcs");
            if (!npcDir.exists() || !npcDir.isDirectory()) continue;

            File[] files = npcDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        CustomNPCInfo info = GSON.fromJson(reader, CustomNPCInfo.class);
                        if (info != null && info.id != null && !info.id.isEmpty()) {
                            NPCS.put(info.id, info);
                        }
                    } catch (Exception e) {
                        System.out.println("[ModularContents] Failed to load NPC JSON: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("[ModularContents] Loaded " + NPCS.size() + " custom NPCs.");
    }

    public static ItemStack getItemFromString(String itemName) {
        if (itemName == null || itemName.isEmpty()) return ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item != null) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
}