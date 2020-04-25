package ro.nicuch.tag.fallback;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CoruptedDataListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!CoruptedDataManager.reportErrors())
            return;
        Player player = event.getPlayer();
        if (!player.isOp())
            return;
        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Corupted data present! Fallback data was activated!");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> If you don't know what this is, please contact the server owner immediately!");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> This can cause bugs and data loss!");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Type [ /trl_backup ] to backup corupted data!");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Type [ /trl_debug ] to start debugging!");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Type [ /trl_overwrite ] to overwrite corupted data! [DANGEROUS]");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Type [ /trl_reset ] to remove corupted data! [DANGEROUS]");
        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "> Be aware that this commands may not work if the server don't have enough space on disk!");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "!!!============================================!!!");
    }
}
