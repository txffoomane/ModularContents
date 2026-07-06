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
        File npcDir = new File(gameDir, "ModularContents/example_pack/npcs");
        if (!npcDir.exists()) {
            npcDir.mkdirs();
            // Generate example NPC
            CustomNPCInfo example = new CustomNPCInfo();
            example.id = "example_bandit";
            example.name = "Bandit";
            example.maxHealth = 30.0;
            example.shootRange = 24.0;
            example.equipment.put("mainhand", "minecraft:bow");
            example.equipment.put("head", "minecraft:leather_helmet");
            example.dropChances.put("mainhand", 0.05f);

            try {
                java.nio.file.Files.write(new File(npcDir, "example_bandit.json").toPath(), GSON.toJson(example).getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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