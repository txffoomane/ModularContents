package modularcontents.custom.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListWorkbenchRecipeManager {
    private static final Logger LOGGER = LogManager.getLogger("ModularContents");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, ListWorkbenchRecipe> RECIPES = new HashMap<>();

    public static void setupDirectories(File configDir) {
        File rootPacksDir = new File(configDir.getParentFile(), "ModularContents");
        if (!rootPacksDir.exists()) {
            rootPacksDir.mkdirs();
        }

        if (modularcontents.custom.config.ModularContentsConfig.generateExamplePack) {
            File examplePackDir = new File(rootPacksDir, "example_pack");
            if (!examplePackDir.exists()) {
                createExamplePack(rootPacksDir);
            }
        }
    }

    public static void loadRecipes(File gameDir) {
        RECIPES.clear();

        File rootPacksDir = new File(gameDir, "ModularContents");
        if (!rootPacksDir.exists()) {
            rootPacksDir.mkdirs();
        }

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File recipeDir = new File(packDir, "recipes");
                if (recipeDir.exists() && recipeDir.isDirectory()) {
                    loadRecipesFromDir(recipeDir, packDir.getName());
                }
            }
        }
    }

    private static void loadRecipesFromDir(File recipeDir, String packName) {
        File[] files = recipeDir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    ListWorkbenchRecipe recipe = GSON.fromJson(reader, ListWorkbenchRecipe.class);
                    if (recipe != null && recipe.id != null) {
                        RECIPES.put(recipe.id, recipe);
                        LOGGER.info("Loaded custom workbench recipe '" + recipe.id + "' from pack '" + packName + "'");
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to load recipe: " + file.getName() + " in pack: " + packName, e);
                }
            }
        }
    }

    private static void createExamplePack(File rootPacksDir) {
        try {
            File examplePackDir = new File(rootPacksDir, "example_pack");
            File exampleRecipesDir = new File(examplePackDir, "recipes");
            exampleRecipesDir.mkdirs();

            // --- Рецепт 1: Раздел Basics ---
            File example1 = new File(exampleRecipesDir, "example_diamond.json");
            ListWorkbenchRecipe recipe1 = new ListWorkbenchRecipe();
            recipe1.id = "example_diamond";
            recipe1.category = "Basics";
            recipe1.craftingTime = 100;

            IngredientStack out1 = new IngredientStack();
            out1.item = "minecraft:diamond";
            out1.count = 1;
            recipe1.output = out1; // single output format

            IngredientStack in1 = new IngredientStack();
            in1.item = "minecraft:dirt";
            in1.count = 10;
            IngredientStack in2 = new IngredientStack();
            in2.item = "minecraft:cobblestone";
            in2.count = 5;
            recipe1.inputs = java.util.Arrays.asList(in1, in2);

            java.io.FileWriter writer1 = new java.io.FileWriter(example1);
            GSON.toJson(recipe1, writer1);
            writer1.close();

            // --- Рецепт 2: Раздел Weapons ---
            File example2 = new File(exampleRecipesDir, "example_sword.json");
            ListWorkbenchRecipe recipe2 = new ListWorkbenchRecipe();
            recipe2.id = "example_sword";
            recipe2.category = "Weapons";
            recipe2.craftingTime = 200;

            IngredientStack out2 = new IngredientStack();
            out2.item = "minecraft:diamond_sword";
            out2.count = 1;
            recipe2.output = out2;

            IngredientStack in3 = new IngredientStack();
            in3.item = "minecraft:diamond";
            in3.count = 2;
            IngredientStack in4 = new IngredientStack();
            in4.item = "minecraft:stick";
            in4.count = 1;
            recipe2.inputs = java.util.Arrays.asList(in3, in4);

            java.io.FileWriter writer2 = new java.io.FileWriter(example2);
            GSON.toJson(recipe2, writer2);
            writer2.close();

            // --- Рецепт 3: Раздел Ammo ---
            File example3 = new File(exampleRecipesDir, "example_arrows.json");
            ListWorkbenchRecipe recipe3 = new ListWorkbenchRecipe();
            recipe3.id = "example_arrows";
            recipe3.category = "Ammo";
            recipe3.craftingTime = 60;

            IngredientStack out3 = new IngredientStack();
            out3.item = "minecraft:arrow";
            out3.count = 4;
            recipe3.output = out3;

            IngredientStack in5 = new IngredientStack();
            in5.item = "minecraft:flint";
            in5.count = 1;
            IngredientStack in6 = new IngredientStack();
            in6.item = "minecraft:stick";
            in6.count = 1;
            IngredientStack in7 = new IngredientStack();
            in7.item = "minecraft:feather";
            in7.count = 1;
            recipe3.inputs = java.util.Arrays.asList(in5, in6, in7);

            java.io.FileWriter writer3 = new java.io.FileWriter(example3);
            GSON.toJson(recipe3, writer3);
            writer3.close();

            // --- Рецепт 4: Раздел Food ---
            File example4 = new File(exampleRecipesDir, "example_golden_apple.json");
            ListWorkbenchRecipe recipe4 = new ListWorkbenchRecipe();
            recipe4.id = "example_golden_apple";
            recipe4.category = "Food";
            recipe4.craftingTime = 300;

            IngredientStack out4 = new IngredientStack();
            out4.item = "minecraft:golden_apple";
            out4.count = 1;
            recipe4.output = out4;

            IngredientStack in8 = new IngredientStack();
            in8.item = "minecraft:apple";
            in8.count = 1;
            IngredientStack in9 = new IngredientStack();
            in9.item = "minecraft:gold_ingot";
            in9.count = 8;
            recipe4.inputs = java.util.Arrays.asList(in8, in9);

            java.io.FileWriter writer4 = new java.io.FileWriter(example4);
            GSON.toJson(recipe4, writer4);
            writer4.close();

            // --- Рецепт 5: Разборка (Disassembly) ---
            File example5 = new File(exampleRecipesDir, "example_disassembly.json");
            ListWorkbenchRecipe recipe5 = new ListWorkbenchRecipe();
            recipe5.id = "example_disassembly";
            recipe5.category = "Recycling";
            recipe5.craftingTime = 100;

            // Multiple outputs
            IngredientStack out5a = new IngredientStack();
            out5a.item = "minecraft:diamond";
            out5a.count = 2;
            IngredientStack out5b = new IngredientStack();
            out5b.item = "minecraft:stick";
            out5b.count = 1;
            recipe5.outputs = java.util.Arrays.asList(out5a, out5b);

            IngredientStack in10 = new IngredientStack();
            in10.item = "minecraft:diamond_sword";
            in10.count = 1;
            recipe5.inputs = java.util.Arrays.asList(in10);

            java.io.FileWriter writer5 = new java.io.FileWriter(example5);
            GSON.toJson(recipe5, writer5);
            writer5.close();

            LOGGER.info("Created example content pack with categories in ModularContents/example_pack");
        } catch (Exception e) {
            LOGGER.error("Failed to create example content pack", e);
        }
    }

    public static ListWorkbenchRecipe getRecipe(String id) {
        return RECIPES.get(id);
    }

    public static Collection<ListWorkbenchRecipe> getAllRecipes() {
        return RECIPES.values();
    }

    public static List<String> getCategories() {
        return RECIPES.values().stream()
                .map(r -> r.category != null && !r.category.isEmpty() ? r.category : "general")
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<ListWorkbenchRecipe> getRecipesInCategory(String category) {
        return RECIPES.values().stream()
                .filter(r -> {
                    String cat = r.category != null && !r.category.isEmpty() ? r.category : "general";
                    return cat.equalsIgnoreCase(category);
                })
                .collect(Collectors.toList());
    }
}
