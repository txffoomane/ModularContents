import re

with open("src/main/java/modularcontents/ModularcontentsMod.java", "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(
    """            } else if ("fence".equalsIgnoreCase(info.blockType)) {
                block = new modularcontents.custom.block.BlockCustomFence(info);
            } else {""",
    """            } else if ("fence".equalsIgnoreCase(info.blockType)) {
                block = new modularcontents.custom.block.BlockCustomFence(info);
            } else if ("wall".equalsIgnoreCase(info.blockType)) {
                block = new modularcontents.custom.block.BlockCustomWall(info);
            } else {"""
)

with open("src/main/java/modularcontents/ModularcontentsMod.java", "w", encoding="utf-8") as f:
    f.write(content)
