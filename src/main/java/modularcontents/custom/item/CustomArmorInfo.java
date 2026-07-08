package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomArmorInfo {
    public String id;

    @SerializedName("description")
    public String[] description = null;

    @SerializedName("display_name")
    public String displayName = "Custom Armor";

    @SerializedName("max_stack_size")
    public int maxStackSize = 1;

    @SerializedName("creative_tab")
    public String creativeTab = "combat";

    @SerializedName("max_damage")
    public int maxDamage = 200;

    @SerializedName("equipment_slot")
    public String equipmentSlot = "head"; // "head", "chest", "legs", "feet"

    @SerializedName("damage_reduction")
    public int damageReduction = 2; // Helmet=3, Chestplate=8, Leggings=6, Boots=3 (Diamond)

    @SerializedName("toughness")
    public float toughness = 0.0f; // 2.0f for Diamond armor

    @SerializedName("enchantability")
    public int enchantability = 10;

    @SerializedName("armor_texture")
    public String armorTexture = ""; // The resource path for the armor model texture. Empty defaults to generated.
}