import re

with open("src/main/java/modularcontents/custom/client/ModularResourcePack.java", "r") as f:
    content = f.read()

old_cond = 'if (CustomContentManager.CUSTOM_ITEMS.containsKey(itemId) || CustomContentManager.CUSTOM_FOODS.containsKey(itemId) || CustomContentManager.CUSTOM_BLOCKS.containsKey(itemId) || itemId.equals("custom_workbench") || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(itemId) != null) {'
new_cond = 'if (CustomContentManager.CUSTOM_ITEMS.containsKey(itemId) || CustomContentManager.CUSTOM_FOODS.containsKey(itemId) || CustomContentManager.CUSTOM_WEAPONS.containsKey(itemId) || CustomContentManager.CUSTOM_TOOLS.containsKey(itemId) || CustomContentManager.CUSTOM_ARMORS.containsKey(itemId) || CustomContentManager.CUSTOM_BLOCKS.containsKey(itemId) || itemId.equals("custom_workbench") || modularcontents.custom.pack.CustomWorkbenchManager.getWorkbench(itemId) != null) {'

content = content.replace(old_cond, new_cond)

with open("src/main/java/modularcontents/custom/client/ModularResourcePack.java", "w") as f:
    f.write(content)
