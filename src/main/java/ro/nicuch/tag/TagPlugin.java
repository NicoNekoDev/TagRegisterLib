package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import ro.nicuch.tag.register.CraftWorldRegister;
import ro.nicuch.tag.wrapper.ChunkUUID;
import ro.nicuch.tag.wrapper.WorldUUID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagPlugin extends JavaPlugin implements Listener {
    private final ConcurrentMap<WorldUUID, CraftWorldRegister> worlds = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                WorldUUID worldUUID = WorldUUID.fromWorld(player.getWorld());
                this.worlds.computeIfAbsent(worldUUID, (k) -> new CraftWorldRegister(player.getWorld()))
                        .updatePlayerChunk(player, ChunkUUID.fromLocation(player.getLocation().getBlock().getRelative(BlockFace.DOWN)));
            }
            for (CraftWorldRegister world : this.worlds.values())
                world.propTick();
        }, 1, 1);
    }
}
