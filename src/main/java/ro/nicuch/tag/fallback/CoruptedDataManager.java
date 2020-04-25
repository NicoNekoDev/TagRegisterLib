package ro.nicuch.tag.fallback;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CoruptedDataManager {
    private final static ConcurrentMap<String, CoruptedDataBackup> coruptedData = new ConcurrentHashMap<>();
    private static CoruptedDataListener coruptedListener;

    public static boolean reportErrors() {
        return coruptedData.size() != 0;
    }

    public static void fallbackOperation(CoruptedDataFallback fallback) {
        CoruptedDataBackup backup = new CoruptedDataBackup(fallback.getCoruptedDataId(), fallback.getCoruptedDataFile(), fallback.getCoruptedDataCompoundTag(), fallback.getWorldName());
        coruptedData.put(fallback.getCoruptedDataId(), backup);
        if (coruptedListener == null) {
            Bukkit.getPluginManager().registerEvents(new CoruptedDataListener(), Bukkit.getPluginManager().getPlugin("TagRegisterLib"));
        }
    }

    public static void backupOperation(CommandSender sender) {
        File directory = new File(Bukkit.getPluginManager().getPlugin("TagRegisterLib").getDataFolder() + File.separator + "backup");
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Backup operation started!");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Total files: " + coruptedData.size());
        sender.sendMessage("");

        for (CoruptedDataBackup backup : coruptedData.values()) {
            String failed = backup.tryToWriteToBackup(directory);
            if (failed != null)
                sender.sendMessage(failed);
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
    }

    public static void overWriteOperation(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Overwrite operation started!");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Total files: " + coruptedData.size());
        sender.sendMessage("");

        for (CoruptedDataBackup backup : coruptedData.values()) {
            String failed = backup.tryToOverWrite();
            if (failed != null)
                sender.sendMessage(failed);
        }

        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
    }

    public static void resetOperation(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
        sender.sendMessage("");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Reset operation started!");
        sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Total files: " + coruptedData.size());
        coruptedData.clear();
        sender.sendMessage("");
        sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
    }
}
