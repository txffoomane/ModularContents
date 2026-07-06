package modularcontents.custom.zone;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LootZone {
    public UUID id;
    public String name = "Zone";
    public int minX, minZ, maxX, maxZ;
    public int respawnIntervalTicks;
    public int color = 0x88FFAA00; // Default orange/yellow semi-transparent
    public Map<String, Float> presets = new HashMap<>();
    
    // Tracks when each container should be restocked
    public Map<BlockPos, Long> restockTimes = new HashMap<>();

    // Tracks authorized containers so player-placed chests don't spawn free loot
    public List<BlockPos> validContainers = new ArrayList<>();

    public LootZone() {
        this.id = UUID.randomUUID();
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasUniqueId("ID")) {
            id = tag.getUniqueId("ID");
        }
        if (tag.hasKey("Name")) {
            name = tag.getString("Name");
        }
        minX = tag.getInteger("MinX");
        minZ = tag.getInteger("MinZ");
        maxX = tag.getInteger("MaxX");
        maxZ = tag.getInteger("MaxZ");
        respawnIntervalTicks = tag.getInteger("RespawnTicks");
        if (tag.hasKey("Color")) {
            color = tag.getInteger("Color");
        }

        presets.clear();
        NBTTagList presetsList = tag.getTagList("Presets", 10);
        for (int i = 0; i < presetsList.tagCount(); i++) {
            NBTTagCompound p = presetsList.getCompoundTagAt(i);
            presets.put(p.getString("Name"), p.getFloat("Chance"));
        }

        restockTimes.clear();
        NBTTagList timesList = tag.getTagList("RestockTimes", 10);
        for (int i = 0; i < timesList.tagCount(); i++) {
            NBTTagCompound t = timesList.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(t.getLong("Pos"));
            long time = t.getLong("Time");
            restockTimes.put(pos, time);
        }

        validContainers.clear();
        NBTTagList containersList = tag.getTagList("ValidContainers", 10);
        for (int i = 0; i < containersList.tagCount(); i++) {
            NBTTagCompound c = containersList.getCompoundTagAt(i);
            validContainers.add(BlockPos.fromLong(c.getLong("Pos")));
        }
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setUniqueId("ID", id);
        tag.setString("Name", name);
        tag.setInteger("MinX", minX);
        tag.setInteger("MinZ", minZ);
        tag.setInteger("MaxX", maxX);
        tag.setInteger("MaxZ", maxZ);
        tag.setInteger("RespawnTicks", respawnIntervalTicks);
        tag.setInteger("Color", color);

        NBTTagList presetsList = new NBTTagList();
        for (Map.Entry<String, Float> entry : presets.entrySet()) {
            NBTTagCompound p = new NBTTagCompound();
            p.setString("Name", entry.getKey());
            p.setFloat("Chance", entry.getValue());
            presetsList.appendTag(p);
        }
        tag.setTag("Presets", presetsList);

        NBTTagList timesList = new NBTTagList();
        for (Map.Entry<BlockPos, Long> entry : restockTimes.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setLong("Pos", entry.getKey().toLong());
            t.setLong("Time", entry.getValue());
            timesList.appendTag(t);
        }
        tag.setTag("RestockTimes", timesList);

        NBTTagList containersList = new NBTTagList();
        for (BlockPos pos : validContainers) {
            NBTTagCompound c = new NBTTagCompound();
            c.setLong("Pos", pos.toLong());
            containersList.appendTag(c);
        }
        tag.setTag("ValidContainers", containersList);
    }
}
