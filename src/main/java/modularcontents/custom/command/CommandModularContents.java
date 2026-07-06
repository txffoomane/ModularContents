package modularcontents.custom.command;

import modularcontents.ModularcontentsMod;
import modularcontents.custom.entity.EntityAirdrop;
import modularcontents.custom.network.PacketSyncContent;
import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import modularcontents.custom.loot.AirdropLootManager;
import modularcontents.custom.loot.EquipmentManager;
import net.minecraft.command.CommandBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class CommandModularContents extends CommandBase {

    @Override
    public String getName() {
        return "modularcontents";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/modularcontents <reload|airdrop>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("mc");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Requires OP (cheats enabled)
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Reloading ModularContents configs, recipes and loot tables..."));

            // Reload main config
            modularcontents.custom.config.ModularContentsConfig.load(server.getDataDirectory());
            // Reload recipes from JSON
            ListWorkbenchRecipeManager.loadRecipes(server.getDataDirectory());
            // Reload loot tables
            AirdropLootManager.loadLootTables(server.getDataDirectory());
            // Reload equipment presets
            EquipmentManager.loadEquipment(server.getDataDirectory());

            PacketSyncContent syncPacket = ModularcontentsMod.buildContentSyncPacket();
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                ModularcontentsMod.PACKET_HANDLER.sendTo(syncPacket, player);
            }

            int recipeCount = ListWorkbenchRecipeManager.getAllRecipes().size();
            int lootCount = AirdropLootManager.LOOT_TABLES.size();
            int equipmentCount = EquipmentManager.EQUIPMENT.size();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Successfully reloaded " + recipeCount + " recipes, " + lootCount + " loot tables and " + equipmentCount + " equipment presets!"));
        } else if (args.length > 0 && args[0].equalsIgnoreCase("airdrop")) {
            if (args.length >= 3) {
                try {
                    double x = parseCoordinate(sender.getPosition().getX(), args[1], true).getResult();
                    double z = parseCoordinate(sender.getPosition().getZ(), args[2], true).getResult();

                    String lootTable = "";
                    if (args.length >= 4) {
                        lootTable = args[3];
                    }

                    World world = sender.getEntityWorld();
                    EntityAirdrop airdrop = new EntityAirdrop(world, x, 250.0D, z);

                    if (!lootTable.isEmpty()) {
                        airdrop.setLootTable(lootTable);
                    }

                    world.spawnEntity(airdrop);

                    sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Airdrop spawned at X: " + (int)x + " Z: " + (int)z + (lootTable.isEmpty() ? "" : " [Loot: " + lootTable + "]")));
                } catch (Exception e) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Invalid coordinates"));
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: /modularcontents airdrop <x> <z> [loot_table]"));
            }
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "airdrop");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("airdrop")) {
            return getListOfStringsMatchingLastWord(args, AirdropLootManager.LOOT_TABLES.keySet());
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}