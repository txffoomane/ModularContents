import os

for filename in ["ItemCustomTool.java", "ItemCustomWeapon.java", "ItemCustom.java", "ItemCustomFood.java", "ItemCustomArmor.java"]:
    path = os.path.join("src/main/java/modularcontents/custom/item", filename)
    if os.path.exists(path):
        with open(path, "r") as f:
            content = f.read()
        
        # Replace this.setRegistryName(info.id) with this.setRegistryName("modularcontents", info.id)
        content = content.replace("this.setRegistryName(info.id);", 'this.setRegistryName("modularcontents", info.id);')
        
        with open(path, "w") as f:
            f.write(content)

