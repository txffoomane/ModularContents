package modularcontents.custom.network;

import modularcontents.custom.entity.EntityAirdrop;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketLaptopAirdropHandler implements IMessageHandler<PacketLaptopAirdrop, IMessage> {
    private static final int MIN_RANGE = 25;
    private static final int MAX_RANGE = 200;

    @Override
    public IMessage onMessage(PacketLaptopAirdrop message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        WorldServer world = player.getServerWorld();

        world.addScheduledTask(() -> {
            // Distance check to ensure player is actually near the laptop
            if (player.getDistanceSq(message.lx, message.ly, message.lz) > 64.0D) {
                return;
            }

            double dx = message.targetX - message.lx;
            double dz = message.targetZ - message.lz;
            double distSq = dx * dx + dz * dz;
            if (distSq < MIN_RANGE * MIN_RANGE || distSq > MAX_RANGE * MAX_RANGE) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Target out of range (" + MIN_RANGE + "-" + MAX_RANGE + " blocks)."));
                return;
            }

            int tx = (int) message.targetX;
            int tz = (int) message.targetZ;
            int ty = world.getHeight(tx, tz);
            BlockPos surface = new BlockPos(tx, ty > 0 ? ty - 1 : 0, tz);
            if (world.getBlockState(surface).getMaterial() == Material.WATER) {
                player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot drop on water."));
                return;
            }

            int delayTicks = 200 + world.rand.nextInt(800); // 10-50 seconds
            EntityAirdrop airdrop = new EntityAirdrop(world, message.targetX, 250.0D, message.targetZ);
            airdrop.setDelayAndCaller(delayTicks, player.getName(), false, message.targetX, message.targetZ);
            world.spawnEntity(airdrop);

            int seconds = delayTicks / 20;
            player.sendMessage(new TextComponentString(TextFormatting.GREEN + "Laptop Airdrop confirmed! Target coordinates: X: " + (int)message.targetX + ", Z: " + (int)message.targetZ + ". ETA: " + seconds + " seconds."));
        });

        return null;
    }
}