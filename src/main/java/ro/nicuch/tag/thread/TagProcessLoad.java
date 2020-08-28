package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.event.world.ChunkLoadEvent;
import ro.nicuch.tag.TagRegister;

public class TagProcessLoad implements TagRunnable {
    private final ChunkLoadEvent event;

    public TagProcessLoad(ChunkLoadEvent event) {
        this.event = event;
    }

    @Override
    public void run() {
        Chunk chunk = this.event.getChunk();
        TagRegister.getOrLoadWorld(this.event.getWorld()).getOrLoadRegion(chunk).getOrLoadChunk(chunk);
    }
}
