package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkUnloadEvent;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TagProcessUnload extends TagProcess {
    private final ChunkUnloadEvent event;
    private final Set<UUID> entitiesSync = new HashSet<>();

    public TagProcessUnload(ChunkUnloadEvent event) {
        super(TagProcessType.UNLOAD);
        this.event = event;
        for (Entity entity : event.getChunk().getEntities())
            entitiesSync.add(entity.getUniqueId());
    }

    @Override
    public void run() {
        World world = this.event.getWorld();
        Chunk chunk = this.event.getChunk();
        WorldRegister wr = TagRegister.getWorld(world).orElseGet(() -> TagRegister.loadWorld(world));
        RegionRegister rr = wr.getRegion(chunk).orElseGet(() -> wr.loadRegion(chunk));
        rr.unloadChunk(chunk, this.entitiesSync);
    }
}
