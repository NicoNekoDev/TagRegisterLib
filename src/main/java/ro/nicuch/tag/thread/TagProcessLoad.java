package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.world.ChunkLoadEvent;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.register.ChunkRegister;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

public class TagProcessLoad implements TagRunnable {
    private final ChunkLoadEvent event;

    public TagProcessLoad(ChunkLoadEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        World world = this.event.getWorld();
        Chunk chunk = this.event.getChunk();
        WorldRegister wr = TagRegister.getWorld(world).orElseGet(() -> TagRegister.loadWorld(world));
        RegionRegister rr = wr.getRegion(chunk).orElseGet(() -> wr.loadRegion(chunk));
        if (rr.isChunkNotLoaded(chunk)) {
            ChunkRegister chunkRegister = rr.loadChunk(chunk);
            if (TagRegister.isDebugging())
                System.out.println(chunkRegister.toString() + " loaded!");
        }
    }
}
