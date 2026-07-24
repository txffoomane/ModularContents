with open("C:\Users\Ethan\.openclaude\projects\D--CONTRACT-modularcontents\memory\team\MEMORY.md", "r") as f:
    lines = f.readlines()

with open("C:\Users\Ethan\.openclaude\projects\D--CONTRACT-modularcontents\memory\team\MEMORY.md", "w") as f:
    for line in lines:
        if "Custom Pack Texture Paths" not in line:
            f.write(line)
