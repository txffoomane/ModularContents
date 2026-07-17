package modularcontents.custom.pack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import modularcontents.custom.block.TileEntityCustomWorkbench;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class CustomWorkbenchManager {
    private static final Logger LOGGER = LogManager.getLogger("ModularContents");
    private static final Gson GSON = new GsonBuilder().create();
    
    public static final List<WorkbenchConfig> WORKBENCHES = new ArrayList<>();
    public static final List<Block> CUSTOM_WORKBENCH_BLOCKS = new ArrayList<>();

    public static WorkbenchConfig getWorkbench(String id) {
        for (WorkbenchConfig config : WORKBENCHES) {
            if (config.id.equals(id)) return config;
        }
        return null;
    }

    public static void loadWorkbenches(File gameDir) {
        WORKBENCHES.clear();
        File rootPacksDir = new File(gameDir, "ModularContents");
        if (!rootPacksDir.exists()) return;

        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File workbenchDir = new File(packDir, "workbenches");
                if (workbenchDir.exists() && workbenchDir.isDirectory()) {
                    File[] files = workbenchDir.listFiles((d, name) -> name.endsWith(".json"));
                    if (files != null) {
                        for (File file : files) {
                            try (FileReader reader = new FileReader(file)) {
                                WorkbenchConfig config = GSON.fromJson(reader, WorkbenchConfig.class);
                                if (config != null && config.id != null) {
                                    WORKBENCHES.add(config);
                                }
                            } catch (Exception e) {
                                LOGGER.error("Failed to load workbench config: " + file.getName(), e);
                            }
                        }
                    }
                }
            }
        }

        PackZipUtils.loadJsonEntries(rootPacksDir, "workbenches", (fileName, reader, packName) -> {
            WorkbenchConfig config = GSON.fromJson(reader, WorkbenchConfig.class);
            if (config != null && config.id != null) {
                WORKBENCHES.add(config);
            }
        });
    }
}
