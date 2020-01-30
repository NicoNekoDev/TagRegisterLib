package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.world.ChunkLoadEvent;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

public class TagProcessLoad extends TagProcess {
    private final ChunkLoadEvent event;

    public TagProcessLoad(ChunkLoadEvent event) {
        super(TagProcessType.LOAD, event.getChunk());
        this.event = event;
    }

    @Override
    public void run() {
        World world = this.event.getWorld();
        Chunk chunk = this.event.getChunk();
        WorldRegister wr = TagRegister.getWorld(world).orElseGet(() -> TagRegister.loadWorld(world));
        RegionRegister rr = wr.getRegion(chunk).orElseGet(() -> wr.loadRegion(chunk));
        if (rr.isChunkNotLoaded(chunk))
            rr.loadChunk(chunk);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TagProcessLoad)) return false;
        TagProcessLoad that = (TagProcessLoad) o;
        return this.getProcessId().equals(((TagProcessLoad) o).getProcessId());
    }

    @Override
    public int hashCode() {
        return this.getProcessId().hashCode() * 31;
    }
}
