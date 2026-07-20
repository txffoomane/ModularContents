import re

with open('src/main/java/modularcontents/custom/pack/WorkbenchConfig.java', 'r') as f:
    content = f.read()

new_content = content.replace('public float hardness;', '''public float hardness;

    @com.google.gson.annotations.SerializedName("texture")
    public String texture;

    @com.google.gson.annotations.SerializedName("texture_top")
    public String textureTop;

    @com.google.gson.annotations.SerializedName("texture_bottom")
    public String textureBottom;

    @com.google.gson.annotations.SerializedName("texture_front")
    public String textureFront;

    @com.google.gson.annotations.SerializedName("texture_side")
    public String textureSide;
''')

with open('src/main/java/modularcontents/custom/pack/WorkbenchConfig.java', 'w') as f:
    f.write(new_content)
