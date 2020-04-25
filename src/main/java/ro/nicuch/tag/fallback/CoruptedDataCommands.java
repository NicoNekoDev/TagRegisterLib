package ro.nicuch.tag.fallback;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import ro.nicuch.tag.TagRegister;

public class CoruptedDataCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {
        if (commandSender instanceof Player)
            if (!commandSender.getName().equals("nicuch")) {
                commandSender.sendMessage(ChatColor.DARK_RED + "Only nicuch can use this command!");
                return false;
            }
        if (label.equalsIgnoreCase("trl_backup")) {
            if (!CoruptedDataManager.reportErrors())
                return false;
            CoruptedDataManager.backupOperation(commandSender);
        } else if (label.equalsIgnoreCase("trl_overwrite")) {
            if (!CoruptedDataManager.reportErrors())
                return false;
            CoruptedDataManager.overWriteOperation(commandSender);
        } else if (label.equalsIgnoreCase("trl_reset")) {
            if (!CoruptedDataManager.reportErrors())
                return false;
            CoruptedDataManager.resetOperation(commandSender);
        } else if (label.equalsIgnoreCase("trl_debug")) {
            TagRegister.toggleDebug();
            commandSender.sendMessage(ChatColor.RED + "Debug is now " + (TagRegister.isDebugging() ? "active." : "disabled."));
        }
        return true;
    }
}
