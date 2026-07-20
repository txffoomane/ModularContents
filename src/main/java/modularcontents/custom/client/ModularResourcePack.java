package modularcontents.custom.client;

import com.google.common.collect.ImmutableSet;
import modularcontents.custom.item.CustomContentManager;
import modularcontents.custom.pack.PackZipUtils;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ModularResourcePack implements IResourcePack {

    private final File rootPacksDir;
    private static final Set<String> DOMAINS = ImmutableSet.of("modularcontents");

    public ModularResourcePack(File gameDir) {
        this.rootPacksDir = new File(gameDir, "ModularContents");
    }

    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException {
        String path = location.getResourcePath();

        // Try to load any requested resource directly from content packs first
        InputStream packStream = findPackResource(path);
        if (packStream != null) {
            return packStream;
        }

        // 0. Serve generated lang files
        if (path.startsWith("lang/") && path.endsWith(".lang")) {
            File langFile = new File(rootPacksDir, path);
            if (langFile.exists()) {
                return new FileInputStream(langFile);
            }
        }

        // 1. Intercept Model generation (So users don't have to write .json models for items)
        if (path.startsWith("models/item/") && path.endsWith(".json")) {
            String itemId = path.substring("models/item/".length(), path.length() - 5);
            if (CustomContentManager.CUSTOM_ITEMS.containsKey(itemId) || CustomContentManager.CUSTOM_FOODS.containsKey(itemId) || itemId.equals("custom_workbench") || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(itemId) != null) {
                if (!itemId.equals("custom_workbench")) {
                    String generatedJson = "{\n  \"parent\": \"item/generated\",\n  \"textures\": {\n    \"layer0\": \"modularcontents:items/" + itemId + "\"\n  }\n}";
                    if (modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(itemId) != null) {
                         generatedJson = "{\n  \"parent\": \"modularcontents:block/" + itemId + "\"\n}";
                    }
                    System.out.println("[ModularContents] Generated model for item: " + itemId);
                    return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
                }
            } else if (CustomContentManager.CUSTOM_BLOCKS.containsKey(itemId)) {
                String generatedJson = "{\n  \"parent\": \"modularcontents:block/" + itemId + "\"\n}";
                return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
            }
        }

        // Blockstates generation
        if (path.startsWith("blockstates/") && path.endsWith(".json")) {
            String blockId = path.substring("blockstates/".length(), path.length() - 5);
            boolean isDoubleSlab = blockId.endsWith("_double");
            String searchId = isDoubleSlab ? blockId.substring(0, blockId.length() - 7) : blockId;
            if (CustomContentManager.CUSTOM_BLOCKS.containsKey(searchId)) {
                modularcontents.custom.item.CustomBlockInfo info = CustomContentManager.CUSTOM_BLOCKS.get(searchId);
                String type = info.blockType != null ? info.blockType.toLowerCase() : "block";
                String generatedJson = "";
                if (isDoubleSlab) {
                    generatedJson = "{\n  \"variants\": {\n    \"normal\": { \"model\": \"modularcontents:" + searchId + "_double\" },\n    \"variant=default\": { \"model\": \"modularcontents:" + searchId + "_double\" }\n  }\n}";
                } else if (type.equals("stair")) {
                    generatedJson = "{\n  \"variants\": {\n    \"facing=east,half=bottom,shape=straight\":  { \"model\": \"modularcontents:" + blockId + "\" },\n    \"facing=west,half=bottom,shape=straight\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 180 },\n    \"facing=south,half=bottom,shape=straight\": { \"model\": \"modularcontents:" + blockId + "\", \"y\": 90 },\n    \"facing=north,half=bottom,shape=straight\": { \"model\": \"modularcontents:" + blockId + "\", \"y\": 270 },\n    \"facing=east,half=bottom,shape=outer_right\":  { \"model\": \"modularcontents:" + blockId + "_outer\" },\n    \"facing=west,half=bottom,shape=outer_right\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 180 },\n    \"facing=south,half=bottom,shape=outer_right\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 90 },\n    \"facing=north,half=bottom,shape=outer_right\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 270 },\n    \"facing=east,half=bottom,shape=outer_left\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 270 },\n    \"facing=west,half=bottom,shape=outer_left\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 90 },\n    \"facing=south,half=bottom,shape=outer_left\": { \"model\": \"modularcontents:" + blockId + "_outer\" },\n    \"facing=north,half=bottom,shape=outer_left\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"y\": 180 },\n    \"facing=east,half=bottom,shape=inner_right\":  { \"model\": \"modularcontents:" + blockId + "_inner\" },\n    \"facing=west,half=bottom,shape=inner_right\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 180 },\n    \"facing=south,half=bottom,shape=inner_right\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 90 },\n    \"facing=north,half=bottom,shape=inner_right\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 270 },\n    \"facing=east,half=bottom,shape=inner_left\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 270 },\n    \"facing=west,half=bottom,shape=inner_left\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 90 },\n    \"facing=south,half=bottom,shape=inner_left\": { \"model\": \"modularcontents:" + blockId + "_inner\" },\n    \"facing=north,half=bottom,shape=inner_left\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"y\": 180 },\n    \"facing=east,half=top,shape=straight\":  { \"model\": \"modularcontents:" + blockId + "\", \"x\": 180 },\n    \"facing=west,half=top,shape=straight\":  { \"model\": \"modularcontents:" + blockId + "\", \"x\": 180, \"y\": 180 },\n    \"facing=south,half=top,shape=straight\": { \"model\": \"modularcontents:" + blockId + "\", \"x\": 180, \"y\": 90 },\n    \"facing=north,half=top,shape=straight\": { \"model\": \"modularcontents:" + blockId + "\", \"x\": 180, \"y\": 270 },\n    \"facing=east,half=top,shape=outer_right\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 90 },\n    \"facing=west,half=top,shape=outer_right\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 270 },\n    \"facing=south,half=top,shape=outer_right\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 180 },\n    \"facing=north,half=top,shape=outer_right\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180 },\n    \"facing=east,half=top,shape=outer_left\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180 },\n    \"facing=west,half=top,shape=outer_left\":  { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 180 },\n    \"facing=south,half=top,shape=outer_left\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 90 },\n    \"facing=north,half=top,shape=outer_left\": { \"model\": \"modularcontents:" + blockId + "_outer\", \"x\": 180, \"y\": 270 },\n    \"facing=east,half=top,shape=inner_right\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 90 },\n    \"facing=west,half=top,shape=inner_right\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 270 },\n    \"facing=south,half=top,shape=inner_right\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 180 },\n    \"facing=north,half=top,shape=inner_right\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180 },\n    \"facing=east,half=top,shape=inner_left\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180 },\n    \"facing=west,half=top,shape=inner_left\":  { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 180 },\n    \"facing=south,half=top,shape=inner_left\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 90 },\n    \"facing=north,half=top,shape=inner_left\": { \"model\": \"modularcontents:" + blockId + "_inner\", \"x\": 180, \"y\": 270 }\n  }\n}";
                } else if (type.equals("slab")) {
                    generatedJson = "{\n  \"variants\": {\n    \"half=bottom,variant=default\": { \"model\": \"modularcontents:" + blockId + "\" },\n    \"half=top,variant=default\": { \"model\": \"modularcontents:" + blockId + "_top\" }\n  }\n}";
                } else {
                    if ("horizontal".equalsIgnoreCase(info.rotationType)) {
                        generatedJson = "{\n  \"variants\": {\n    \"facing=north\": { \"model\": \"modularcontents:" + blockId + "\" },\n    \"facing=south\": { \"model\": \"modularcontents:" + blockId + "\", \"y\": 180 },\n    \"facing=west\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 270 },\n    \"facing=east\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 90 }\n  }\n}";
                    } else if ("log".equalsIgnoreCase(info.rotationType)) {
                        generatedJson = "{\n  \"variants\": {\n    \"axis=y\": { \"model\": \"modularcontents:" + blockId + "\" },\n    \"axis=z\": { \"model\": \"modularcontents:" + blockId + "\", \"x\": 90 },\n    \"axis=x\": { \"model\": \"modularcontents:" + blockId + "\", \"x\": 90, \"y\": 90 },\n    \"axis=none\": { \"model\": \"modularcontents:" + blockId + "\" }\n  }\n}";
                    } else {
                        generatedJson = "{\n  \"variants\": {\n    \"normal\": { \"model\": \"modularcontents:" + blockId + "\" }\n  }\n}";
                    }
                }
                return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
            } else if (modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(blockId) != null) {
                String generatedJson = "{\n  \"variants\": {\n    \"crafting=false,facing=north\": { \"model\": \"modularcontents:" + blockId + "\" },\n    \"crafting=false,facing=south\": { \"model\": \"modularcontents:" + blockId + "\", \"y\": 180 },\n    \"crafting=false,facing=west\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 270 },\n    \"crafting=false,facing=east\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 90 },\n    \"crafting=true,facing=north\": { \"model\": \"modularcontents:" + blockId + "\" },\n    \"crafting=true,facing=south\": { \"model\": \"modularcontents:" + blockId + "\", \"y\": 180 },\n    \"crafting=true,facing=west\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 270 },\n    \"crafting=true,facing=east\":  { \"model\": \"modularcontents:" + blockId + "\", \"y\": 90 }\n  }\n}";
                return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
            }
        }

        // Block models generation
        if (path.startsWith("models/block/") && path.endsWith(".json")) {
            String blockId = path.substring("models/block/".length(), path.length() - 5);
            boolean isDoubleSlabModel = blockId.endsWith("_double");
            String baseId = blockId.replace("_top", "").replace("_inner", "").replace("_outer", "").replace("_double", "");
            if (CustomContentManager.CUSTOM_BLOCKS.containsKey(baseId)) {
                modularcontents.custom.item.CustomBlockInfo info = CustomContentManager.CUSTOM_BLOCKS.get(baseId);
                String tex = info.texture != null && !info.texture.isEmpty() ? info.texture : baseId;
                if (baseId.endsWith("_slab") && tex.equals(baseId)) tex = baseId.substring(0, baseId.length() - 5);

                boolean hasTop = info.textureTop != null && !info.textureTop.isEmpty();
                boolean hasBottom = info.textureBottom != null && !info.textureBottom.isEmpty();
                boolean hasFront = info.textureFront != null && !info.textureFront.isEmpty();
                boolean hasSide = info.textureSide != null && !info.textureSide.isEmpty();

                String tTop = hasTop ? "modularcontents:blocks/" + info.textureTop : "modularcontents:blocks/" + tex;
                String tBottom = hasBottom ? "modularcontents:blocks/" + info.textureBottom : "modularcontents:blocks/" + tex;
                String tSide = hasSide ? "modularcontents:blocks/" + info.textureSide : "modularcontents:blocks/" + tex;
                String tFront = hasFront ? "modularcontents:blocks/" + info.textureFront : tSide;

                String texPath = "modularcontents:blocks/" + tex;
                String generatedJson = "";

                if (isDoubleSlabModel) {
                    if (hasTop || hasBottom || hasFront || hasSide) {
                         generatedJson = "{\n  \"parent\": \"block/orientable\",\n  \"textures\": {\n" +
                             "    \"top\": \"" + tTop + "\",\n" +
                             "    \"bottom\": \"" + tBottom + "\",\n" +
                             "    \"front\": \"" + tFront + "\",\n" +
                             "    \"side\": \"" + tSide + "\"\n" +
                             "  }\n}";
                    } else {
                        generatedJson = "{\n  \"parent\": \"block/cube_all\",\n  \"textures\": {\n    \"all\": \"" + texPath + "\"\n  }\n}";
                    }
                } else if (blockId.endsWith("_inner")) {
                    generatedJson = "{\n  \"parent\": \"block/inner_stairs\",\n  \"textures\": {\n    \"bottom\": \"" + tBottom + "\",\n    \"top\": \"" + tTop + "\",\n    \"side\": \"" + tSide + "\"\n  }\n}";
                } else if (blockId.endsWith("_outer")) {
                    generatedJson = "{\n  \"parent\": \"block/outer_stairs\",\n  \"textures\": {\n    \"bottom\": \"" + tBottom + "\",\n    \"top\": \"" + tTop + "\",\n    \"side\": \"" + tSide + "\"\n  }\n}";
                } else if (blockId.endsWith("_top")) {
                    generatedJson = "{\n  \"parent\": \"block/upper_slab\",\n  \"textures\": {\n    \"bottom\": \"" + tBottom + "\",\n    \"top\": \"" + tTop + "\",\n    \"side\": \"" + tSide + "\"\n  }\n}";
                } else {
                    String type = info.blockType != null ? info.blockType.toLowerCase() : "block";
                    if (type.equals("stair")) {
                        generatedJson = "{\n  \"parent\": \"block/stairs\",\n  \"textures\": {\n    \"bottom\": \"" + tBottom + "\",\n    \"top\": \"" + tTop + "\",\n    \"side\": \"" + tSide + "\"\n  }\n}";
                    } else if (type.equals("slab")) {
                        if (isDoubleSlabModel) {
                            if (hasTop || hasBottom || hasFront || hasSide) {
                                 generatedJson = "{\n  \"parent\": \"block/orientable\",\n  \"textures\": {\n" +
                                     "    \"top\": \"" + tTop + "\",\n" +
                                     "    \"bottom\": \"" + tBottom + "\",\n" +
                                     "    \"front\": \"" + tFront + "\",\n" +
                                     "    \"side\": \"" + tSide + "\"\n" +
                                     "  }\n}";
                            } else {
                                generatedJson = "{\n  \"parent\": \"block/cube_all\",\n  \"textures\": {\n    \"all\": \"" + texPath + "\"\n  }\n}";
                            }
                        } else {
                            generatedJson = "{\n  \"parent\": \"block/half_slab\",\n  \"textures\": {\n    \"bottom\": \"" + tBottom + "\",\n    \"top\": \"" + tTop + "\",\n    \"side\": \"" + tSide + "\"\n  }\n}";
                        }
                    } else {
                        if (hasTop || hasBottom || hasFront || hasSide) {
                             generatedJson = "{\n  \"parent\": \"block/orientable\",\n  \"textures\": {\n" +
                                 "    \"top\": \"" + tTop + "\",\n" +
                                 "    \"bottom\": \"" + tBottom + "\",\n" +
                                 "    \"front\": \"" + tFront + "\",\n" +
                                 "    \"side\": \"" + tSide + "\"\n" +
                                 "  }\n}";
                        } else {
                            generatedJson = "{\n  \"parent\": \"block/cube_all\",\n  \"textures\": {\n    \"all\": \"" + texPath + "\"\n  }\n}";
                        }
                    }
                }
                return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
            } else if (modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(blockId) != null) {
                 modularcontents.custom.pack.WorkbenchConfig config = modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(blockId);
                 String generatedJson = "";
                 String tex = config.texture != null && !config.texture.isEmpty() ? config.texture : blockId;

                 boolean hasTop = config.textureTop != null && !config.textureTop.isEmpty();
                 boolean hasBottom = config.textureBottom != null && !config.textureBottom.isEmpty();
                 boolean hasFront = config.textureFront != null && !config.textureFront.isEmpty();
                 boolean hasSide = config.textureSide != null && !config.textureSide.isEmpty();

                 if (hasTop || hasBottom || hasFront || hasSide) {
                     String tTop = hasTop ? config.textureTop : tex;
                     String tBottom = hasBottom ? config.textureBottom : tex;
                     String tFront = hasFront ? config.textureFront : (hasSide ? config.textureSide : tex);
                     String tSide = hasSide ? config.textureSide : tex;

                     generatedJson = "{\n  \"parent\": \"block/orientable\",\n  \"textures\": {\n" +
                             "    \"top\": \"modularcontents:blocks/" + tTop + "\",\n" +
                             "    \"bottom\": \"modularcontents:blocks/" + tBottom + "\",\n" +
                             "    \"front\": \"modularcontents:blocks/" + tFront + "\",\n" +
                             "    \"side\": \"modularcontents:blocks/" + tSide + "\"\n" +
                             "  }\n}";
                 } else {
                     generatedJson = "{\n  \"parent\": \"block/cube_all\",\n  \"textures\": {\n    \"all\": \"modularcontents:blocks/" + tex + "\"\n  }\n}";
                 }

                 return new ByteArrayInputStream(generatedJson.getBytes(StandardCharsets.UTF_8));
            }
        }

        throw new IOException("Resource not found: " + location);
    }

    @Override
    public boolean resourceExists(ResourceLocation location) {
        String path = location.getResourcePath();

        // 0. Serve any file directly from pack if it exists
        if (packResourceExists(path)) {
            return true;
        }

        // 0. Serve generated lang files
        if (path.startsWith("lang/") && path.endsWith(".lang")) {
            File langFile = new File(rootPacksDir, path);
            if (langFile.exists()) {
                return true;
            }
        }

        // Model intercept check
        if (path.startsWith("models/item/") && path.endsWith(".json")) {
            String itemId = path.substring("models/item/".length(), path.length() - 5);
            if (CustomContentManager.CUSTOM_ITEMS.containsKey(itemId) || CustomContentManager.CUSTOM_FOODS.containsKey(itemId) || CustomContentManager.CUSTOM_BLOCKS.containsKey(itemId) || itemId.equals("custom_workbench") || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(itemId) != null) {
                if (!itemId.equals("custom_workbench")) {
                    return true;
                }
            }
        }

        if (path.startsWith("blockstates/") && path.endsWith(".json")) {
            String blockId = path.substring("blockstates/".length(), path.length() - 5);
            boolean isDoubleSlab = blockId.endsWith("_double");
            String searchId = isDoubleSlab ? blockId.substring(0, blockId.length() - 7) : blockId;
            if (CustomContentManager.CUSTOM_BLOCKS.containsKey(searchId) || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(searchId) != null) return true;
        }

        if (path.startsWith("models/block/") && path.endsWith(".json")) {
            String blockId = path.substring("models/block/".length(), path.length() - 5);
            boolean isDoubleSlabModel = blockId.endsWith("_double");
            String baseId = blockId.replace("_top", "").replace("_inner", "").replace("_outer", "").replace("_double", "");
            if (CustomContentManager.CUSTOM_BLOCKS.containsKey(baseId) || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(baseId) != null) return true;
        }

        // Texture intercept check
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            return textureExists(path);
        }

        return false;
    }

    private InputStream findPackResource(String path) throws IOException {
        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File resFile = new File(packDir, path);
                if (resFile.exists() && resFile.isFile()) {
                    return new FileInputStream(resFile);
                }
            }
        }
        return PackZipUtils.findZipResource(rootPacksDir, path);
    }

    private boolean packResourceExists(String path) {
        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                if (new File(packDir, path).exists() && new File(packDir, path).isFile()) {
                    return true;
                }
            }
        }
        return PackZipUtils.zipResourceExists(rootPacksDir, path);
    }

    private InputStream findTexture(String path) throws IOException {
        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                File textureFile = new File(packDir, path);
                if (textureFile.exists()) {
                    return new FileInputStream(textureFile);
                }
            }
        }
        return PackZipUtils.findZipResource(rootPacksDir, path);
    }

    private boolean textureExists(String path) {
        File[] packDirs = rootPacksDir.listFiles(File::isDirectory);
        if (packDirs != null) {
            for (File packDir : packDirs) {
                if (new File(packDir, path).exists()) {
                    return true;
                }
            }
        }
        return PackZipUtils.zipResourceExists(rootPacksDir, path);
    }

    @Override
    public Set<String> getResourceDomains() {
        return DOMAINS;
    }

    @Nullable
    @Override
    public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "ModularContents Dynamic Resources";
    }
}
