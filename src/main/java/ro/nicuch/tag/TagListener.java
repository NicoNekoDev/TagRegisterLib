package ro.nicuch.tag;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldSaveEvent;

import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

public class TagListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(ChunkLoadEvent event) {
        World world = event.getWorld();
        Chunk chunk = event.getChunk();
        WorldRegister wr = TagRegister.getWorld(world).orElseGet(() -> TagRegister.loadWorld(world));
        RegionRegister rr = wr.getRegion(chunk).orElseGet(() -> wr.loadRegion(chunk));
        if (rr.isChunkNotLoaded(chunk))
            rr.loadChunk(chunk);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(ChunkUnloadEvent event) {
        World world = event.getWorld();
        Chunk chunk = event.getChunk();
        WorldRegister wr = TagRegister.getWorld(world).orElseGet(() -> TagRegister.loadWorld(world));
        RegionRegister rr = wr.getRegion(chunk).orElseGet(() -> wr.loadRegion(chunk));
        rr.unloadChunk(chunk);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void event(WorldSaveEvent event) {
        TagRegister.getWorld(event.getWorld()).orElseGet(() -> TagRegister.loadWorld(event.getWorld())).saveRegions();
    }
}
