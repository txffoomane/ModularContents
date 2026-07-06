package modularcontents.custom.zone;

import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LootZoneManager extends WorldSavedData {
    private static final String DATA_NAME = "ModularContents_LootZones";
    public final List<LootZone> zones = new ArrayList<>();

    public LootZoneManager() {
        super(DATA_NAME);
    }

    public LootZoneManager(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        zones.clear();
        NBTTagList list = nbt.getTagList("Zones", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            LootZone zone = new LootZone();
            zone.readFromNBT(tag);
            zones.add(zone);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (LootZone zone : zones) {
            NBTTagCompound tag = new NBTTagCompound();
            zone.writeToNBT(tag);
            list.appendTag(tag);
        }
        compound.setTag("Zones", list);
        return compound;
    }
}
