package modularcontents.custom.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import modularcontents.custom.pack.PackZipUtils;
import modularcontents.custom.recipe.IngredientStack;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EquipmentManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Map<String, EquipmentFile> EQUIPMENT = new LinkedHashMap<>();

    public static void loadEquipment(File gameDir) {
        EQUIPMENT.clear();

        File rootPacksDir = new File(gameDir, "ModularContents");
        if (!rootPacksDir.exists()) {
            return;
        }

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File equipmentDir = new File(new File(packDir, "loot_tables"), "equipment");
                if (equipmentDir.exists() && equipmentDir.isDirectory()) {
                    File[] jsonFiles = equipmentDir.listFiles((dir, name) -> name.endsWith(".json"));
                    if (jsonFiles != null) {
                        for (File jsonFile : jsonFiles) {
                            String name = jsonFile.getName().substring(0, jsonFile.getName().length() - ".json".length());
                            try (Reader reader = new FileReader(jsonFile)) {
                                registerEquipment(name, reader, packDir.getName());
                            } catch (Exception e) {
                                System.err.println("[ModularContents] Failed to load equipment preset from: " + jsonFile.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        PackZipUtils.loadJsonEntries(rootPacksDir, "loot_tables/equipment", (fileName, reader, packName) -> {
            String name = fileName.substring(0, fileName.length() - ".json".length());
            registerEquipment(name, reader, packName);
        });
    }

    private static void registerEquipment(String name, Reader reader, String packName) {
        EquipmentFile file = GSON.fromJson(reader, EquipmentFile.class);
        if (file != null && file.sets != null && !file.sets.isEmpty()) {
            EQUIPMENT.put(name, file);
            System.out.println("[ModularContents] Loaded equipment preset: " + name + " (" + file.sets.size() + " sets) from " + packName);
        }
    }

    public static List<String> getPresetNames() {
        return new ArrayList<>(EQUIPMENT.keySet());
    }

    public static EquipmentSet pickRandomSet(EquipmentFile file, Random random) {
        if (file == null || file.sets == null || file.sets.isEmpty()) {
            return null;
        }

        float totalWeight = 0.0f;
        for (EquipmentSet set : file.sets) {
            totalWeight += Math.max(0.0f, set.chance);
        }

        if (totalWeight <= 0.0f) {
            return file.sets.get(random.nextInt(file.sets.size()));
        }

        float roll = random.nextFloat() * totalWeight;
        float current = 0.0f;
        for (EquipmentSet set : file.sets) {
            current += Math.max(0.0f, set.chance);
            if (roll < current) {
                return set;
            }
        }
        return file.sets.get(file.sets.size() - 1);
    }

    public static List<ItemStack> rollContainerLoot(List<PresetSelection> selections, Random random) {
        List<ItemStack> generated = new ArrayList<>();
        if (selections == null) {
            return generated;
        }

        for (PresetSelection selection : selections) {
            EquipmentFile file = EQUIPMENT.get(selection.name);
            if (file == null) {
                continue;
            }
            if (random.nextFloat() * 100.0f >= selection.chance) {
                continue;
            }
            EquipmentSet set = pickRandomSet(file, random);
            if (set == null || set.items == null) {
                continue;
            }
            for (IngredientStack ingredient : set.items) {
                if (ingredient == null) {
                    continue;
                }
                ItemStack stack = ingredient.toItemStack();
                if (!stack.isEmpty()) {
                    generated.add(stack);
                }
            }
        }

        return generated;
    }

    public static String toSyncJson() {
        return GSON.toJson(EQUIPMENT);
    }

    public static void applySynced(String json) {
        try {
            Type type = new TypeToken<LinkedHashMap<String, EquipmentFile>>() {}.getType();
            Map<String, EquipmentFile> parsed = GSON.fromJson(json, type);
            EQUIPMENT.clear();
            if (parsed != null) {
                for (Map.Entry<String, EquipmentFile> entry : parsed.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().sets != null && !entry.getValue().sets.isEmpty()) {
                        EQUIPMENT.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            System.out.println("[ModularContents] Synced " + EQUIPMENT.size() + " equipment presets from server");
        } catch (Exception e) {
            System.err.println("[ModularContents] Failed to apply synced equipment presets");
            e.printStackTrace();
        }
    }

    public static class EquipmentFile {
        public List<EquipmentSet> sets;
    }

    public static class EquipmentSet {
        public String name;
        public float chance = 100.0f;
        public List<IngredientStack> items;
    }

    public static class PresetSelection {
        public final String name;
        public final float chance;

        public PresetSelection(String name, float chance) {
            this.name = name;
            this.chance = chance;
        }
    }
}
