package modularcontents.custom.tab;

import com.google.gson.annotations.SerializedName;

public class CustomTabInfo {
    public String id;

    @SerializedName("display_name")
    public String displayName;

    public String icon; // format: "minecraft:diamond_sword" or "modularcontents:my_custom_item"
    public java.util.List<String> items;
}