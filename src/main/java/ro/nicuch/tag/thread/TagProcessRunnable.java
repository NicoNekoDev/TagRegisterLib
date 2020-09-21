package ro.nicuch.tag.thread;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import ro.nicuch.tag.TagRegister;
import ro.nicuch.tag.register.RegionRegister;
import ro.nicuch.tag.register.WorldRegister;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class TagProcessRunnable implements Runnable {
    private static final ConcurrentLinkedQueue<TagRunnable> processes = new ConcurrentLinkedQueue<>();

    // ASYNC CALL
    @Override
    public void run() {
        while (!processes.isEmpty()) {
            processes.poll().run();
        }
    }

    // SYNC CALLS
    public static void addToLoad(ChunkLoadEvent event) {
        processes.offer(() -> {
            Chunk chunk = event.getChunk();
            TagRegister.getOrLoadWorld(event.getWorld()).getOrLoadRegion(chunk).getOrLoadChunk(chunk);
        });
    }

    public static void addToUnload(ChunkUnloadEvent event) {
        processes.offer(() -> {
            Set<UUID> entitiesSync = Arrays.stream(event.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet());
            World world = event.getWorld();
            Chunk chunk = event.getChunk();
            if (!TagRegister.isWorldLoaded(world))
                return;
            WorldRegister worldRegister = TagRegister.getWorldUnsafe(world);
            if (!worldRegister.isRegionLoaded(chunk))
                return;
            RegionRegister regionRegister = worldRegister.getRegionUnsafe(chunk);
            if (!regionRegister.isChunkLoaded(chunk))
                return;
            regionRegister.unloadChunk(chunk, entitiesSync);
            entitiesSync.clear();
        });
    }
}