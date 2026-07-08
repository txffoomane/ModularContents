package modularcontents.custom.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Universal content manager that replaces CustomItemManager.
 * It scans subdirectories to load specialized POJOs for blocks, food, and items.
 */
public class CustomContentManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Map<String, CustomItemInfo> CUSTOM_ITEMS = new HashMap<>();
    public static final Map<String, CustomBlockInfo> CUSTOM_BLOCKS = new HashMap<>();
    public static final Map<String, CustomFoodInfo> CUSTOM_FOODS = new HashMap<>();
    public static final Map<String, CustomWeaponInfo> CUSTOM_WEAPONS = new HashMap<>();
    public static final Map<String, CustomToolInfo> CUSTOM_TOOLS = new HashMap<>();
    public static final Map<String, CustomArmorInfo> CUSTOM_ARMORS = new HashMap<>();

    public static void loadContent(File gameDir) {
        CUSTOM_ITEMS.clear();
        CUSTOM_BLOCKS.clear();
        CUSTOM_FOODS.clear();
        CUSTOM_WEAPONS.clear();
        CUSTOM_TOOLS.clear();
        CUSTOM_ARMORS.clear();

        File rootDir = new File(gameDir, "ModularContents");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        File[] packs = rootDir.listFiles(File::isDirectory);
        if (packs != null) {
            for (File packDir : packs) {
                if (packDir.getName().equals("generated")) continue;

                // Load basic items
                loadJsonFiles(new File(packDir, "items"), CustomItemInfo.class, CUSTOM_ITEMS);

                // Load blocks
                loadJsonFiles(new File(packDir, "blocks"), CustomBlockInfo.class, CUSTOM_BLOCKS);

                // Load food
                loadJsonFiles(new File(packDir, "food"), CustomFoodInfo.class, CUSTOM_FOODS);
                // Load weapons
                loadJsonFiles(new File(packDir, "weapons"), CustomWeaponInfo.class, CUSTOM_WEAPONS);

                // Load tools
                loadJsonFiles(new File(packDir, "tools"), CustomToolInfo.class, CUSTOM_TOOLS);

                // Load armor
                loadJsonFiles(new File(packDir, "armor"), CustomArmorInfo.class, CUSTOM_ARMORS);
            }
        }
        System.out.println("[ModularContents] Loaded " + CUSTOM_ITEMS.size() + " items, " + CUSTOM_BLOCKS.size() + " blocks, " + CUSTOM_FOODS.size() + " foods, " + CUSTOM_WEAPONS.size() + " weapons, " + CUSTOM_TOOLS.size() + " tools, " + CUSTOM_ARMORS.size() + " armors.");
    }

    private static <T> void loadJsonFiles(File dir, Class<T> clazz, Map<String, T> map) {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    T info = GSON.fromJson(reader, clazz);
                    // Extract ID via reflection or assumption
                    String id = null;
                    if (info instanceof CustomItemInfo) id = ((CustomItemInfo) info).id;
                    else if (info instanceof CustomBlockInfo) id = ((CustomBlockInfo) info).id;
                    else if (info instanceof CustomFoodInfo) id = ((CustomFoodInfo) info).id;
                    else if (info instanceof CustomWeaponInfo) id = ((CustomWeaponInfo) info).id;
                    else if (info instanceof CustomToolInfo) id = ((CustomToolInfo) info).id;
                    else if (info instanceof CustomArmorInfo) id = ((CustomArmorInfo) info).id;

                    if (id != null && !id.isEmpty()) {
                        map.put(id, info);
                    }
                } catch (Exception e) {
                    System.out.println("[ModularContents] Failed to load JSON: " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}