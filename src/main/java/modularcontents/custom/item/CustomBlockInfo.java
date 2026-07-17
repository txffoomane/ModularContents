package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomBlockInfo {
    public String id;

    @SerializedName("display_name")
    public String displayName = "Custom Block";

    @SerializedName("creative_tab")
    public String creativeTab = "buildingBlocks";

    @SerializedName("material")
    public String material = "rock"; // rock, wood, earth, iron, glass

    @SerializedName("hardness")
    public float hardness = 1.5f; // time to mine (stone is 1.5, dirt 0.5, obsidian 50)

    @SerializedName("resistance")
    public float resistance = 10.0f; // explosion resistance

    @SerializedName("light_level")
    public float lightLevel = 0.0f; // 0.0 to 1.0 (1.0 is max brightness like glowstone)

    @SerializedName("tool_class")
    public String toolClass = "pickaxe"; // pickaxe, axe, shovel

    @SerializedName("harvest_level")
    public int harvestLevel = 0; // 0=wood/gold, 1=stone, 2=iron, 3=diamond

    @SerializedName("block_type")
    public String blockType = "block"; // block, slab, stair

    @SerializedName("texture")
    public String texture = "";

    @SerializedName("has_slab")
    public boolean hasSlab = false;

    @SerializedName("has_stairs")
    public boolean hasStairs = false;

    @SerializedName("has_fence")
    public boolean hasFence = false;

    @SerializedName("has_wall")
    public boolean hasWall = false;
}