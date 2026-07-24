import os

for root, _, files in os.walk("src/main/java/modularcontents/custom/item"):
    for file in files:
        if file.endswith(".java"):
            path = os.path.join(root, file)
            with open(path, "r") as f:
                content = f.read()
            if "this.setRegistryName(info.id);" in content:
                content = content.replace('this.setRegistryName(info.id);', 'this.setRegistryName(new ResourceLocation("modularcontents", info.id));')
                
                # Check if ResourceLocation is imported
                if "import net.minecraft.util.ResourceLocation;" not in content:
                    content = content.replace("import net.minecraft.item.ItemStack;", "import net.minecraft.item.ItemStack;\nimport net.minecraft.util.ResourceLocation;")
                
                with open(path, "w") as f:
                    f.write(content)
