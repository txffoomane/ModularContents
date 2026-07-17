package modularcontents.custom.item;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LangGenerator {

    public static void generateLangFiles(File gameDir) {
        File langDir = new File(gameDir, "ModularContents/lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
        }

        File enUs = new File(langDir, "en_us.lang");
        File ruRu = new File(langDir, "ru_ru.lang");

        updateLangFile(enUs, "en");
        updateLangFile(ruRu, "ru");
    }

    private static void updateLangFile(File langFile, String lang) {
        Set<String> existingKeys = new HashSet<>();
        if (langFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(langFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("=")) {
                        existingKeys.add(line.split("=")[0].trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(langFile, true))) {
            boolean wroteAnything = false;

            // Items
            for (String id : CustomContentManager.CUSTOM_ITEMS.keySet()) {
                String key = "item.custom." + id + ".name";
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            // Foods
            for (String id : CustomContentManager.CUSTOM_FOODS.keySet()) {
                String key = "item." + id + ".name"; // Custom food uses simple unlocalized name
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            // Weapons
            for (String id : CustomContentManager.CUSTOM_WEAPONS.keySet()) {
                String key = "item.custom." + id + ".name";
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            // Tools
            for (String id : CustomContentManager.CUSTOM_TOOLS.keySet()) {
                String key = "item.custom." + id + ".name";
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            // Armor
            for (String id : CustomContentManager.CUSTOM_ARMORS.keySet()) {
                String key = "item.custom." + id + ".name";
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            // Blocks
            for (String id : CustomContentManager.CUSTOM_BLOCKS.keySet()) {
                String key = "tile." + id + ".name";
                if (!existingKeys.contains(key)) {
                    writer.write(key + "=" + generateFriendlyName(id) + "\n");
                    wroteAnything = true;
                    existingKeys.add(key);
                }
            }

            if (wroteAnything) {
                System.out.println("[ModularContents] Updated language file: " + langFile.getName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateFriendlyName(String id) {
        if (id == null || id.isEmpty()) return "Unknown";
        String[] words = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1));
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}
