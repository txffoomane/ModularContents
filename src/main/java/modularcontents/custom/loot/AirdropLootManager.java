package modularcontents.custom.loot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import modularcontents.custom.pack.PackZipUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AirdropLootManager {

    private static final Gson GSON = new Gson();
    public static final Map<String, AirdropLootTable> LOOT_TABLES = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static void loadLootTables(File gameDir) {
        LOOT_TABLES.clear();
        File rootPacksDir = new File(gameDir, "ModularContents");

        if (!rootPacksDir.exists()) {
            return;
        }

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File lootTablesDir = new File(packDir, "loot_tables");
                File lootDir = new File(lootTablesDir, "airdrops");

                if (lootDir.exists() && lootDir.isDirectory()) {
                    File[] jsonFiles = lootDir.listFiles((dir, name) -> name.endsWith(".json"));
                    if (jsonFiles != null) {
                        for (File jsonFile : jsonFiles) {
                            String name = jsonFile.getName().replace(".json", "");
                            try (Reader reader = new FileReader(jsonFile)) {
                                loadLootTable(name, reader, packDir.getName());
                            } catch (Exception e) {
                                System.err.println("[ModularContents] Failed to load airdrop loot table from: " + jsonFile.getAbsolutePath());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        PackZipUtils.loadJsonEntries(rootPacksDir, "loot_tables/airdrops", (fileName, reader, packName) -> {
            String name = fileName.substring(0, fileName.length() - ".json".length());
            loadLootTable(name, reader, packName);
        });
    }

    private static void loadLootTable(String name, Reader reader, String packName) {
        JsonObject root = GSON.fromJson(reader, JsonObject.class);

        int weight = root.has("weight") ? root.get("weight").getAsInt() : 10;
        List<LootEntry> entries = new ArrayList<>();

        if (root.has("items")) {
            JsonArray itemsArray = root.getAsJsonArray("items");
            for (JsonElement el : itemsArray) {
                JsonObject itemObj = el.getAsJsonObject();
                String itemName = itemObj.get("item").getAsString();
                int meta = itemObj.has("meta") ? itemObj.get("meta").getAsInt() : 0;
                int min = itemObj.has("min") ? itemObj.get("min").getAsInt() : 1;
                int max = itemObj.has("max") ? itemObj.get("max").getAsInt() : 1;
                double chance = itemObj.has("chance") ? itemObj.get("chance").getAsDouble() : 1.0;

                entries.add(new LootEntry(itemName, meta, min, max, chance));
            }
        }

        LOOT_TABLES.put(name, new AirdropLootTable(name, weight, entries));
        System.out.println("[ModularContents] Loaded airdrop loot table: " + name + " from " + packName);
    }

    public static String getRandomLootTable() {
        if (LOOT_TABLES.isEmpty()) {
            return "";
        }

        int totalWeight = 0;
        for (AirdropLootTable table : LOOT_TABLES.values()) {
            totalWeight += table.weight;
        }

        int randomValue = RANDOM.nextInt(totalWeight);
        int currentWeight = 0;

        for (AirdropLootTable table : LOOT_TABLES.values()) {
            currentWeight += table.weight;
            if (randomValue < currentWeight) {
                return table.name;
            }
        }

        // Fallback
        return LOOT_TABLES.keySet().iterator().next();
    }

    public static List<ItemStack> generateLoot(String tableName) {
        List<ItemStack> generated = new ArrayList<>();
        if (tableName == null || tableName.isEmpty() || !LOOT_TABLES.containsKey(tableName)) {
            return generated;
        }

        AirdropLootTable table = LOOT_TABLES.get(tableName);
        for (LootEntry entry : table.entries) {
            if (RANDOM.nextDouble() <= entry.chance) {
                Item item = Item.REGISTRY.getObject(new ResourceLocation(entry.itemName));
                if (item != null) {
                    int amount = entry.min;
                    if (entry.max > entry.min) {
                        amount += RANDOM.nextInt(entry.max - entry.min + 1);
                    }
                    if (amount > 0) {
                        generated.add(new ItemStack(item, amount, entry.meta));
                    }
                }
            }
        }

        return generated;
    }

    public static class AirdropLootTable {
        public final String name;
        public final int weight;
        public final List<LootEntry> entries;

        public AirdropLootTable(String name, int weight, List<LootEntry> entries) {
            this.name = name;
            this.weight = weight;
            this.entries = entries;
        }
    }

    public static class LootEntry {
        public final String itemName;
        public final int meta;
        public final int min;
        public final int max;
        public final double chance;

        public LootEntry(String itemName, int meta, int min, int max, double chance) {
            this.itemName = itemName;
            this.meta = meta;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }
    }
}