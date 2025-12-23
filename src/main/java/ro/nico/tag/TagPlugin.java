package ro.nico.tag;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nico.tag.util.TicketType;
import ro.nico.tag.wrapper.ChunkPos;
import ro.nico.tag.wrapper.WorldId;

public class TagPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                WorldId worldId = WorldId.fromWorld(world);
                for (Player player : world.getPlayers()) {
                    ChunkPos chunkPos = ChunkPos.fromLocation(player.getLocation());
                    CraftTagRegister.getWorldRegister(worldId).setChunkTicket(chunkPos, TicketType.PLAYER);
                }
            }
        }, 0, 1);
    }
}
