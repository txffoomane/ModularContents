package modularcontents.custom.tab;

import com.google.gson.Gson;
import modularcontents.custom.item.CustomItemInfo;
import modularcontents.custom.item.CustomContentManager;
import modularcontents.custom.pack.PackZipUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class CustomTabManager {

    private static final Gson GSON = new Gson();
    public static final Map<String, CreativeTabs> CUSTOM_TABS = new HashMap<>();
    private static final Map<String, CustomTabInfo> TAB_INFOS = new HashMap<>();

    public static void loadTabs(File gameDir) {
        CUSTOM_TABS.clear();
        TAB_INFOS.clear();

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
                        registerTab(GSON.fromJson(reader, CustomTabInfo.class));
                    } catch (Exception e) {
                        System.err.println("[ModularContents] Failed to load custom tab from: " + file.getAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }

        PackZipUtils.loadJsonEntries(rootPacksDir, "tabs", (fileName, reader, packName) -> registerTab(GSON.fromJson(reader, CustomTabInfo.class)));
    }

    private static void registerTab(CustomTabInfo info) {
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

            @Override
            @SideOnly(Side.CLIENT)
            public void displayAllRelevantItems(NonNullList<ItemStack> list) {
                super.displayAllRelevantItems(list);
                if (info.items != null) {
                    for (String itemId : info.items) {
                        Item item = Item.REGISTRY.getObject(new ResourceLocation(itemId));
                        if (item != null && item.getCreativeTab() != this) {
                            item.getSubItems(CreativeTabs.SEARCH, list);
                        }
                    }
                }
            }
        };

        CUSTOM_TABS.put(info.id, newTab);
        TAB_INFOS.put(info.id, info);
        System.out.println("[ModularContents] Loaded custom creative tab: " + info.id);
    }

    public static String toSyncJson() {
        return GSON.toJson(TAB_INFOS.values());
    }

    public static void applySyncedTabs(String json) {
        try {
            CustomTabInfo[] infos = GSON.fromJson(json, CustomTabInfo[].class);
            if (infos != null) {
                for (CustomTabInfo info : infos) {
                    if (info != null && info.id != null && !info.id.isEmpty() && !CUSTOM_TABS.containsKey(info.id)) {
                        registerTab(info);
                    }
                }
            }

            for (CustomItemInfo itemInfo : CustomContentManager.CUSTOM_ITEMS.values()) {
                if (itemInfo.creativeTab == null || !CUSTOM_TABS.containsKey(itemInfo.creativeTab)) continue;
                Item item = Item.getByNameOrId("modularcontents:" + itemInfo.id);
                if (item != null && item.getCreativeTab() != CUSTOM_TABS.get(itemInfo.creativeTab)) {
                    item.setCreativeTab(CUSTOM_TABS.get(itemInfo.creativeTab));
                }
            }
            for (modularcontents.custom.item.CustomFoodInfo itemInfo : CustomContentManager.CUSTOM_FOODS.values()) {
                if (itemInfo.creativeTab == null || !CUSTOM_TABS.containsKey(itemInfo.creativeTab)) continue;
                Item item = Item.getByNameOrId("modularcontents:" + itemInfo.id);
                if (item != null && item.getCreativeTab() != CUSTOM_TABS.get(itemInfo.creativeTab)) {
                    item.setCreativeTab(CUSTOM_TABS.get(itemInfo.creativeTab));
                }
            }
        } catch (Exception e) {
            System.err.println("[ModularContents] Failed to apply synced creative tabs");
            e.printStackTrace();
        }
    }
}
