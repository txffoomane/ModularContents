package modularcontents.custom.item;

import com.google.gson.annotations.SerializedName;

public class CustomItemInfo {
    public String id;

    @SerializedName("description")
    public String[] description = null;

    @SerializedName("display_name")
    public String displayName = "Custom Item";

    @SerializedName("max_stack_size")
    public int maxStackSize = 64;

    @SerializedName("creative_tab")
    public String creativeTab = "misc"; // currently defaults to misc or custom tab

    @SerializedName("max_damage")
    public int maxDamage = 0; // if > 0, it acts as a tool with durability

    @SerializedName("airdrop_caller")
    public AirdropCallerInfo airdropCaller = null; // If present, item acts as an airdrop caller

    public static class AirdropCallerInfo {
        @SerializedName("is_flare")
        public boolean isFlare = false; // True = spawns thrown flare, False = instant request (like radio)

        @SerializedName("delay_min")
        public int delayMin = 200; // Ticks for fuse or delay

        @SerializedName("delay_max")
        public int delayMax = 1000;

        @SerializedName("distance_min")
        public double distanceMin = 50.0;

        @SerializedName("distance_max")
        public double distanceMax = 200.0;

        @SerializedName("loot_table")
        public String lootTable = ""; // Empty string means random

        @SerializedName("consume_on_use")
        public boolean consumeOnUse = true;
    }
}