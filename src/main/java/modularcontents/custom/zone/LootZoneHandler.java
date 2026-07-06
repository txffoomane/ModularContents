package modularcontents.custom.zone;

import modularcontents.custom.loot.EquipmentManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootZoneHandler {

    public static LootZoneManager get(World world) {
        LootZoneManager manager = (LootZoneManager) world.getPerWorldStorage().getOrLoadData(LootZoneManager.class, "ModularContents_LootZones");
        if (manager == null) {
            manager = new LootZoneManager();
            world.getPerWorldStorage().setData("ModularContents_LootZones", manager);
        }
        return manager;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote) {
            return;
        }

        WorldServer world = (WorldServer) event.world;
        
        // We don't want to check every single tick for performance. Every 20 ticks (1 sec) is fine.
        if (world.getTotalWorldTime() % 20 != 0) {
            return;
        }

        LootZoneManager manager = get(world);
        if (manager.zones.isEmpty()) {
            return;
        }

        boolean dirty = false;
        long currentTime = world.getTotalWorldTime();
        Random random = world.rand;

        for (LootZone zone : manager.zones) {
            if (zone.respawnIntervalTicks <= 0 || zone.presets.isEmpty()) {
                continue;
            }

            // Find all authorized inventories in the zone
            for (BlockPos pos : zone.validContainers) {
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof IInventory)) {
                    continue;
                }
                IInventory inv = (IInventory) te;

                if (isEmpty(inv)) {
                    if (!zone.restockTimes.containsKey(pos)) {
                        // Container is empty and not tracked yet. Start cooldown.
                        zone.restockTimes.put(pos, currentTime + zone.respawnIntervalTicks);
                        dirty = true;
                    } else if (currentTime >= zone.restockTimes.get(pos)) {
                        // Cooldown finished, restock!
                        restockContainer(inv, zone, random);
                        zone.restockTimes.remove(pos); // remove from tracking until it gets empty again

                        inv.markDirty();
                        world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                        dirty = true;
                    }
                } else {
                    // If it's not empty, but it's in the tracking map, it means someone put items in it before it naturally restocked.
                    // Optional: we can remove it from tracking.
                    if (zone.restockTimes.containsKey(pos)) {
                        zone.restockTimes.remove(pos);
                        dirty = true;
                    }
                }
            }
        }

        if (dirty) {
            manager.markDirty();
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote || event.getPlayer() == null) {
            return;
        }

        // Only allow automatic container addition if the player is in Creative Mode
        if (!event.getPlayer().isCreative()) {
            return;
        }

        // Check if the placed block has a TileEntity (e.g., a chest or other inventory)
        if (event.getPlacedBlock().getBlock().hasTileEntity(event.getPlacedBlock())) {
            WorldServer world = (WorldServer) event.getWorld();
            LootZoneManager manager = get(world);
            BlockPos pos = event.getPos();
            boolean dirty = false;

            for (LootZone zone : manager.zones) {
                if (pos.getX() >= zone.minX && pos.getX() <= zone.maxX &&
                    pos.getZ() >= zone.minZ && pos.getZ() <= zone.maxZ) {

                    if (!zone.validContainers.contains(pos)) {
                        zone.validContainers.add(pos);
                        dirty = true;
                    }
                }
            }

            if (dirty) {
                manager.markDirty();
            }
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }

        WorldServer world = (WorldServer) event.getWorld();
        LootZoneManager manager = get(world);
        BlockPos pos = event.getPos();
        boolean dirty = false;

        for (LootZone zone : manager.zones) {
            if (zone.validContainers.contains(pos)) {
                zone.validContainers.remove(pos);
                zone.restockTimes.remove(pos); // Clean up restock timer if it exists
                dirty = true;
            }
        }

        if (dirty) {
            manager.markDirty();
        }
    }

    private boolean isEmpty(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void restockContainer(IInventory inventory, LootZone zone, Random random) {
        List<EquipmentManager.PresetSelection> selections = new ArrayList<>();
        for (Map.Entry<String, Float> entry : zone.presets.entrySet()) {
            selections.add(new EquipmentManager.PresetSelection(entry.getKey(), entry.getValue()));
        }

        List<ItemStack> loot = EquipmentManager.rollContainerLoot(selections, random);
        if (loot.isEmpty()) {
            return;
        }

        int size = inventory.getSizeInventory();
        for (ItemStack stack : loot) {
            int attempts = 0;
            boolean done = false;
            while (attempts < size * 2 && !done) {
                int slot = random.nextInt(size);
                if (inventory.getStackInSlot(slot).isEmpty() && inventory.isItemValidForSlot(slot, stack)) {
                    inventory.setInventorySlotContents(slot, stack);
                    done = true;
                }
                attempts++;
            }
        }
    }
}
