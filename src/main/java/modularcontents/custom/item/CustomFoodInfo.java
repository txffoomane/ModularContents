package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomFoodInfo {
    public String id;

    @SerializedName("description")
    public String[] description = null;

    @SerializedName("display_name")
    public String displayName = "Custom Food";

    @SerializedName("max_stack_size")
    public int maxStackSize = 64;

    @SerializedName("creative_tab")
    public String creativeTab = "food";

    @SerializedName("heal_amount")
    public int healAmount = 4; // 1 = half a drumstick, 4 = 2 drumsticks

    @SerializedName("saturation")
    public float saturation = 0.3f; // modifier for how long you stay full

    @SerializedName("always_edible")
    public boolean alwaysEdible = false; // true if you can eat it when hunger is full

    @SerializedName("is_meat")
    public boolean isMeat = false; // affects wolves feeding

    @SerializedName("potion_effect")
    public String potionEffect = ""; // e.g., "minecraft:regeneration"

    @SerializedName("potion_duration")
    public int potionDuration = 100; // in ticks (20 = 1 sec)

    @SerializedName("potion_amplifier")
    public int potionAmplifier = 0; // 0 = level 1, 1 = level 2

    @SerializedName("potion_probability")
    public float potionProbability = 1.0f; // 0.0 to 1.0
}