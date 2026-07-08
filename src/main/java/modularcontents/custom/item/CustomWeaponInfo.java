package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomWeaponInfo {
    public String id;

    @SerializedName("description")
    public String[] description = null;

    @SerializedName("display_name")
    public String displayName = "Custom Weapon";

    @SerializedName("max_stack_size")
    public int maxStackSize = 1;

    @SerializedName("creative_tab")
    public String creativeTab = "combat";

    @SerializedName("max_damage")
    public int maxDamage = 250;

    @SerializedName("attack_damage")
    public float attackDamage = 3.0f; // Added to base damage (which is 1 for bare hands, so 3.0f = 4 total damage = 2 hearts)

    @SerializedName("attack_speed")
    public float attackSpeed = -2.4f; // Modifier for attack speed (default sword is -2.4, default axe is -3.0 or -3.2)
}