package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomToolInfo {
    public String id;

    @SerializedName("description")
    public String[] description = null;

    @SerializedName("display_name")
    public String displayName = "Custom Tool";

    @SerializedName("max_stack_size")
    public int maxStackSize = 1;

    @SerializedName("creative_tab")
    public String creativeTab = "tools";

    @SerializedName("max_damage")
    public int maxDamage = 250;

    @SerializedName("tool_type")
    public String toolType = "pickaxe"; // "pickaxe", "axe", "shovel", "hoe"

    @SerializedName("harvest_level")
    public int harvestLevel = 2; // 0 = Wood/Gold, 1 = Stone, 2 = Iron, 3 = Diamond

    @SerializedName("efficiency")
    public float efficiency = 6.0f; // 2.0 (Wood), 4.0 (Stone), 6.0 (Iron), 8.0 (Diamond), 12.0 (Gold)

    @SerializedName("attack_damage")
    public float attackDamage = 1.0f;

    @SerializedName("attack_speed")
    public float attackSpeed = -2.8f;
}