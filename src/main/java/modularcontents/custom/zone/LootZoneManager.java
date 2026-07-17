package modularcontents.custom.zone;

import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;

public class LootZoneManager extends WorldSavedData {
    private static final String DATA_NAME = "ModularContents_LootZones";
    public final List<LootZone> zones = new ArrayList<>();
    public final Map<BlockPos, List<ItemStack>> hiddenContainerLoot = new HashMap<>();

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
        
        hiddenContainerLoot.clear();
        NBTTagList hiddenList = nbt.getTagList("HiddenLoot", 10);
        for (int i = 0; i < hiddenList.tagCount(); i++) {
            NBTTagCompound tag = hiddenList.getCompoundTagAt(i);
            BlockPos pos = BlockPos.fromLong(tag.getLong("Pos"));
            NBTTagList itemsTag = tag.getTagList("Items", 10);
            List<ItemStack> items = new ArrayList<>();
            for (int j = 0; j < itemsTag.tagCount(); j++) {
                items.add(new ItemStack(itemsTag.getCompoundTagAt(j)));
            }
            hiddenContainerLoot.put(pos, items);
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
        
        NBTTagList hiddenList = new NBTTagList();
        for (Map.Entry<BlockPos, List<ItemStack>> entry : hiddenContainerLoot.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("Pos", entry.getKey().toLong());
            NBTTagList itemsTag = new NBTTagList();
            for (ItemStack stack : entry.getValue()) {
                itemsTag.appendTag(stack.writeToNBT(new NBTTagCompound()));
            }
            tag.setTag("Items", itemsTag);
            hiddenList.appendTag(tag);
        }
        compound.setTag("HiddenLoot", hiddenList);
        
        return compound;
    }
}
