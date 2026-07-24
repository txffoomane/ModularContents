import re

with open("src/main/java/modularcontents/custom/tab/CustomTabInfo.java", "r") as f:
    data = f.read()

data = data.replace('public String icon; // format: "minecraft:diamond_sword" or "modularcontents:my_custom_item"', 
                    'public String icon; // format: "minecraft:diamond_sword" or "modularcontents:my_custom_item"\n\n    public java.util.List<String> items; // Items to add to this tab')

with open("src/main/java/modularcontents/custom/tab/CustomTabInfo.java", "w") as f:
    f.write(data)


with open("src/main/java/modularcontents/custom/tab/CustomTabManager.java", "r") as f:
    data = f.read()

data = data.replace('import net.minecraft.util.ResourceLocation;', 'import net.minecraft.util.ResourceLocation;\nimport net.minecraft.util.NonNullList;\nimport net.minecraftforge.fml.relauncher.Side;\nimport net.minecraftforge.fml.relauncher.SideOnly;')

method_impl = """
            @Override
            @SideOnly(Side.CLIENT)
            public void displayAllReleventItems(NonNullList<ItemStack> list) {
                super.displayAllReleventItems(list);
                if (info.items != null) {
                    for (String itemId : info.items) {
                        Item item = Item.REGISTRY.getObject(new ResourceLocation(itemId));
                        if (item != null && item != Blocks.AIR.getItemDropped(Blocks.AIR.getDefaultState(), null, 0)) { // sanity check
                            item.getSubItems(this, list);
                        }
                    }
                }
            }"""

data = data.replace('            public String getTranslatedTabLabel() {\n                return info.displayName != null ? info.displayName : info.id;\n            }', '            public String getTranslatedTabLabel() {\n                return info.displayName != null ? info.displayName : info.id;\n            }\n' + method_impl)


with open("src/main/java/modularcontents/custom/tab/CustomTabManager.java", "w") as f:
    f.write(data)
