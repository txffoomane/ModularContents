import re
with open("src/main/java/modularcontents/ModularcontentsMod.java", "r") as f:
    data = f.read()

# Replace everything from the second "// Register Weapons" to the end of the method with just the method end
second_index = data.find("// Register Weapons", data.find("// Register Weapons") + 10)

if second_index != -1:
    end_of_method = data.find("    }", second_index)
    data = data[:second_index] + data[end_of_method:]

with open("src/main/java/modularcontents/ModularcontentsMod.java", "w") as f:
    f.write(data)
