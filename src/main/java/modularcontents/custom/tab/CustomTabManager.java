package modularcontents.custom.tab;

import com.google.gson.Gson;
import modularcontents.custom.pack.PackZipUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class CustomTabManager {

    private static final Gson GSON = new Gson();
    public static final Map<String, CreativeTabs> CUSTOM_TABS = new HashMap<>();

    public static void loadTabs(File gameDir) {
        CUSTOM_TABS.clear();

        File rootPacksDir = new File(gameDir, "ModularContents");
        if (!rootPacksDir.exists()) return;

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File tabsDir = new File(packDir, "tabs");
                if (!tabsDir.exists() || !tabsDir.isDirectory()) continue;

                File[] jsonFiles = tabsDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (jsonFiles == null) continue;

                for (File file : jsonFiles) {
                    try (FileReader reader = new FileReader(file)) {
                        registerTab(reader);
                    } catch (Exception e) {
                        System.err.println("[ModularContents] Failed to load custom tab from: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }

        PackZipUtils.loadJsonEntries(rootPacksDir, "tabs", (fileName, reader, packName) -> registerTab(reader));
    }

    private static void registerTab(Reader reader) {
        CustomTabInfo info = GSON.fromJson(reader, CustomTabInfo.class);
        if (info == null || info.id == null || info.id.isEmpty()) return;

        CreativeTabs newTab = new CreativeTabs(info.id) {
            @Override
            public ItemStack getTabIconItem() {
                if (info.icon != null && !info.icon.isEmpty()) {
                    Item iconItem = Item.REGISTRY.getObject(new ResourceLocation(info.icon));
                    if (iconItem != null) {
                        return new ItemStack(iconItem);
                    }
                }
                return new ItemStack(Blocks.DIRT);
            }

            @Override
            public String getTranslatedTabLabel() {
                return info.displayName != null ? info.displayName : info.id;
            }
        };

        CUSTOM_TABS.put(info.id, newTab);
        System.out.println("[ModularContents] Loaded custom creative tab: " + info.id);
    }
}
