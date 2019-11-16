package ro.nicuch.tag.fallback;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CoruptedDataCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] strings) {
        if (!CoruptedDataManager.reportErrors())
            return false;
        if (label.equalsIgnoreCase("trl_backup")) {
            CoruptedDataManager.backupOperation(commandSender);
        } else if (label.equalsIgnoreCase("trl_overwrite")) {
            CoruptedDataManager.overWriteOperation(commandSender);
        } else if (label.equalsIgnoreCase("trl_reset")) {
            CoruptedDataManager.resetOperation(commandSender);
        }
        return true;
    }
}
