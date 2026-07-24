import re

with open("src/main/java/modularcontents/custom/client/ModularResourcePack.java", "r", encoding="utf-8") as f:
    content = f.read()

# Update item models (line 61)
content = content.replace(
    'generatedJson = "{\n  \\"parent\\": \\"modularcontents:block/" + itemId + "\\"\n}";',
    '''generatedJson = "{\n  \\"parent\\": \\"modularcontents:block/" + itemId + "\\"\n}";
                         if (CustomContentManager.CUSTOM_BLOCKS.containsKey(itemId)) {
                             modularcontents.custom.item.CustomBlockInfo bInfo = CustomContentManager.CUSTOM_BLOCKS.get(itemId);
                             if ("fence".equalsIgnoreCase(bInfo.blockType) || "wall".equalsIgnoreCase(bInfo.blockType)) {
                                 generatedJson = "{\n  \\"parent\\": \\"modularcontents:block/" + itemId + "_inventory\\"\n}";
                             }
                         }'''
)

# Update blockstates (line 82)
content = content.replace(
    '''                } else if (type.equals("slab")) {
                    generatedJson = "{\n  \\"variants\\": {\n    \\"half=bottom,variant=default\\": { \\"model\\": \\"modularcontents:" + blockId + "\\" },\n    \\"half=top,variant=default\\": { \\"model\\": \\"modularcontents:" + blockId + "_top\\" }\n  }\n}";
                } else {
                    if ("horizontal".equalsIgnoreCase(info.rotationType)) {''',
    '''                } else if (type.equals("slab")) {
                    generatedJson = "{\n  \\"variants\\": {\n    \\"half=bottom,variant=default\\": { \\"model\\": \\"modularcontents:" + blockId + "\\" },\n    \\"half=top,variant=default\\": { \\"model\\": \\"modularcontents:" + blockId + "_top\\" }\n  }\n}";
                } else if (type.equals("fence")) {
                    generatedJson = "{\n  \\"multipart\\": [\n" +
                        "    { \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_post\\" } },\n" +
                        "    { \\"when\\": { \\"north\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"east\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 90, \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"south\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 180, \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"west\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 270, \\"uvlock\\": true } }\n" +
                        "  ]\n}";
                } else if (type.equals("wall")) {
                    generatedJson = "{\n  \\"multipart\\": [\n" +
                        "    { \\"when\\": { \\"up\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_post\\" } },\n" +
                        "    { \\"when\\": { \\"north\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"east\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 90, \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"south\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 180, \\"uvlock\\": true } },\n" +
                        "    { \\"when\\": { \\"west\\": \\"true\\" }, \\"apply\\": { \\"model\\": \\"modularcontents:" + blockId + "_side\\", \\"y\\": 270, \\"uvlock\\": true } }\n" +
                        "  ]\n}";
                } else {
                    if ("horizontal".equalsIgnoreCase(info.rotationType)) {'''
)

# Update block models (line 140)
content = content.replace(
    '''                } else if (blockId.endsWith("_top")) {
                    generatedJson = "{\n  \\"parent\\": \\"block/upper_slab\\",\n  \\"textures\\": {\n    \\"bottom\\": \\"" + tBottom + "\\",\n    \\"top\\": \\"" + tTop + "\\",\n    \\"side\\": \\"" + tSide + "\\"\n  }\n}";
                } else {
                    String type = info.blockType != null ? info.blockType.toLowerCase() : "block";''',
    '''                } else if (blockId.endsWith("_top")) {
                    generatedJson = "{\n  \\"parent\\": \\"block/upper_slab\\",\n  \\"textures\\": {\n    \\"bottom\\": \\"" + tBottom + "\\",\n    \\"top\\": \\"" + tTop + "\\",\n    \\"side\\": \\"" + tSide + "\\"\n  }\n}";
                } else if (blockId.endsWith("_post")) {
                    String parent = "block/fence_post";
                    if ("wall".equalsIgnoreCase(info.blockType)) parent = "block/wall_post";
                    generatedJson = "{\n  \\"parent\\": \\"" + parent + "\\",\n  \\"textures\\": {\n    \\"texture\\": \\"" + texPath + "\\",\n    \\"wall\\": \\"" + texPath + "\\"\n  }\n}";
                } else if (blockId.endsWith("_side")) {
                    String parent = "block/fence_side";
                    if ("wall".equalsIgnoreCase(info.blockType)) parent = "block/wall_side";
                    generatedJson = "{\n  \\"parent\\": \\"" + parent + "\\",\n  \\"textures\\": {\n    \\"texture\\": \\"" + texPath + "\\",\n    \\"wall\\": \\"" + texPath + "\\"\n  }\n}";
                } else if (blockId.endsWith("_inventory")) {
                    String parent = "block/fence_inventory";
                    if ("wall".equalsIgnoreCase(info.blockType)) parent = "block/wall_inventory";
                    generatedJson = "{\n  \\"parent\\": \\"" + parent + "\\",\n  \\"textures\\": {\n    \\"texture\\": \\"" + texPath + "\\",\n    \\"wall\\": \\"" + texPath + "\\"\n  }\n}";
                } else {
                    String type = info.blockType != null ? info.blockType.toLowerCase() : "block";'''
)

# Update resourceExists (line 243)
content = content.replace(
    'String baseId = blockId.replace("_top", "").replace("_inner", "").replace("_outer", "").replace("_double", "");',
    'String baseId = blockId.replace("_top", "").replace("_inner", "").replace("_outer", "").replace("_double", "").replace("_post", "").replace("_side", "").replace("_inventory", "");'
)

with open("src/main/java/modularcontents/custom/client/ModularResourcePack.java", "w", encoding="utf-8") as f:
    f.write(content)
