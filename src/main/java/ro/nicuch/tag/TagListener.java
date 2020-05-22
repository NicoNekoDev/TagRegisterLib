package ro.nicuch.tag;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class TagListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldSaveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(TagRegister.getPlugin(), () ->
                TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldUnloadEvent event) {
        TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions();
        TagRegister.unloadWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().isOp())
            return;
        if (event.getMessage().equalsIgnoreCase("/save-all"))
            Bukkit.getScheduler().runTaskAsynchronously(TagRegister.getPlugin(), TagRegister::saveAll);
    }

    @EventHandler
    public void event(ChunkUnloadEvent event) {
        World world = event.getWorld();
        Chunk chunk = event.getChunk();
        Optional<WorldRegister> worldRegister = TagRegister.getWorld(world);
        if (worldRegister.isEmpty())
            return;
        Optional<RegionRegister> regionRegister = worldRegister.get().getRegion(chunk);
        if (regionRegister.isEmpty())
            return;
        regionRegister.get().unloadChunk(chunk, Arrays.stream(event.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet()));
    }
}
