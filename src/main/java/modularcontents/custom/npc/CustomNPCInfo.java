package modularcontents.custom.npc;

import java.util.HashMap;
import java.util.Map;

public class CustomNPCInfo {
    public String id = "";
    public String name = "Bandit";
    public double maxHealth = 20.0;
    public double speed = 0.25;
    public double attackDamage = 2.0;

    // Visuals
    public String texture = "minecraft:textures/entity/steve.png"; // Default texture
    public boolean slimModel = false; // true for Alex arms, false for Steve arms

    // AI Settings
    public double followRange = 32.0;
    public double shootRange = 16.0; // Distance to start shooting
    public boolean isAggressive = true;

    // Loot
    public String lootTable = ""; // e.g., "modularcontents:bandits_loot"

    // Equipment: Map of slot ("mainhand", "offhand", "head", "chest", "legs", "feet") to item ID
    public Map<String, String> equipment = new HashMap<>();
    public Map<String, Float> dropChances = new HashMap<>();
}