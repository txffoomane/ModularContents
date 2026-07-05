package modularcontents.custom.network;

import modularcontents.custom.loot.EquipmentManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PacketZoneLootHandler implements IMessageHandler<PacketZoneLoot, IMessage> {
    private static final int MAX_ZONE_SIZE = 128;

    @Override
    public IMessage onMessage(PacketZoneLoot message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        WorldServer world = player.getServerWorld();

        world.addScheduledTask(() -> {
            if (!player.isCreative()) {
                return;
            }

            int minX = Math.min(message.minX, message.maxX);
            int maxX = Math.max(message.minX, message.maxX);
            int minZ = Math.min(message.minZ, message.maxZ);
            int maxZ = Math.max(message.minZ, message.maxZ);

            if (maxX - minX > MAX_ZONE_SIZE || maxZ - minZ > MAX_ZONE_SIZE) {
                player.sendMessage(new TextComponentString(TextFormatting.RED
                        + "Zone too large (max " + MAX_ZONE_SIZE + " x " + MAX_ZONE_SIZE + " blocks)."));
                return;
            }

            List<IInventory> containers = new ArrayList<>();
            for (TileEntity te : new ArrayList<>(world.loadedTileEntityList)) {
                if (!(te instanceof IInventory)) {
                    continue;
                }
                BlockPos pos = te.getPos();
                if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
                    continue;
                }
                containers.add((IInventory) te);
            }

            if (containers.isEmpty()) {
                player.sendMessage(new TextComponentString(TextFormatting.YELLOW
                        + "No containers found in the selected zone."));
                return;
            }

            if (message.clear) {
                int cleared = 0;
                for (IInventory inventory : containers) {
                    inventory.clear();
                    markContainerDirty(world, inventory);
                    cleared++;
                }
                player.sendMessage(new TextComponentString(TextFormatting.GREEN
                        + "Cleared loot from " + cleared + " container(s)."));
                return;
            }

            List<EquipmentManager.PresetSelection> selections = new ArrayList<>();
            for (PacketZoneLoot.PresetEntry entry : message.presets) {
                if (entry.name != null && !entry.name.isEmpty() && entry.chance > 0.0f) {
                    selections.add(new EquipmentManager.PresetSelection(entry.name, entry.chance));
                }
            }

            if (selections.isEmpty()) {
                player.sendMessage(new TextComponentString(TextFormatting.RED
                        + "No presets selected."));
                return;
            }

            Random random = world.rand;
            int filledContainers = 0;
            int placedItems = 0;
            for (IInventory inventory : containers) {
                List<ItemStack> loot = EquipmentManager.rollContainerLoot(selections, random);
                if (loot.isEmpty()) {
                    continue;
                }
                int placed = scatterLoot(inventory, loot, random);
                if (placed > 0) {
                    markContainerDirty(world, inventory);
                    filledContainers++;
                    placedItems += placed;
                }
            }

            player.sendMessage(new TextComponentString(TextFormatting.GREEN
                    + "Filled " + filledContainers + " of " + containers.size()
                    + " container(s) with " + placedItems + " item stack(s)."));
        });

        return null;
    }

    private static int scatterLoot(IInventory inventory, List<ItemStack> loot, Random random) {
        int size = inventory.getSizeInventory();
        int placed = 0;
        for (ItemStack stack : loot) {
            int attempts = 0;
            boolean done = false;
            while (attempts < size * 2 && !done) {
                int slot = random.nextInt(size);
                if (inventory.getStackInSlot(slot).isEmpty() && inventory.isItemValidForSlot(slot, stack)) {
                    inventory.setInventorySlotContents(slot, stack);
                    placed++;
                    done = true;
                }
                attempts++;
            }
        }
        return placed;
    }

    private static void markContainerDirty(WorldServer world, IInventory inventory) {
        inventory.markDirty();
        if (inventory instanceof TileEntity) {
            TileEntity te = (TileEntity) inventory;
            BlockPos pos = te.getPos();
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }
}
