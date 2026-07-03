package modularcontents.custom.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ModularContentsConfig {

    public static boolean generateExamplePack = true;

    public static void load(File gameDir) {
        File configDir = new File(gameDir, "ModularContents");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "modularcontents_settings.cfg");
        Configuration config = new Configuration(configFile);

        config.load();
        generateExamplePack = config.getBoolean("Generate Example Pack", Configuration.CATEGORY_GENERAL, true, "Should the mod generate an example content pack with recipes on first launch?");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
