package modularcontents.custom.command;

import modularcontents.custom.recipe.ListWorkbenchRecipeManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandModularContents extends CommandBase {

    @Override
    public String getName() {
        return "modularcontents";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/modularcontents reload";
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
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Reloading ModularContents recipes..."));

            // Reload recipes from JSON
            ListWorkbenchRecipeManager.loadRecipes(server.getDataDirectory());

            int count = ListWorkbenchRecipeManager.getAllRecipes().size();
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Successfully reloaded " + count + " recipes!"));
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
