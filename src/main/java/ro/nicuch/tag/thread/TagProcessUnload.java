package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkUnloadEvent;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TagProcessUnload implements TagRunnable {
    private final ChunkUnloadEvent event;
    private final Set<UUID> entitiesSync;

    public TagProcessUnload(ChunkUnloadEvent event) {
        this.event = event;
        this.entitiesSync = Arrays.stream(event.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet());
    }

    @Override
    public void run() {
        World world = this.event.getWorld();
        Chunk chunk = this.event.getChunk();
        if (!TagRegister.isWorldLoaded(world))
            return;
        WorldRegister worldRegister = TagRegister.getWorldUnsafe(world);
        if (!worldRegister.isRegionLoaded(chunk))
            return;
        RegionRegister regionRegister = worldRegister.getRegionUnsafe(chunk);
        if (!regionRegister.isChunkLoaded(chunk))
            return;
        regionRegister.unloadChunk(chunk, this.entitiesSync);
        this.entitiesSync.clear();
    }
}
