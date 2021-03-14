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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TagProcessRunnable {
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();

    public TagProcessRunnable() {
        this.executorService.scheduleAtFixedRate(() -> {
            while (!this.tasks.isEmpty())
                this.tasks.poll().run();
        }, 20, 20, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        this.executorService.shutdown();
    }

    // SYNC CALLS
    public void addToLoad(ChunkLoadEvent event) {
        if (this.executorService.isTerminated())
            return;
        this.tasks.offer(() -> {
            Chunk chunk = event.getChunk();
            TagRegister.getOrLoadWorld(event.getWorld()).getOrLoadRegion(chunk).getOrLoadChunk(chunk);
        });
    }

    public void addToUnload(ChunkUnloadEvent event) {
        if (this.executorService.isTerminated())
            return;
        this.tasks.offer(() -> {
            Set<UUID> entitiesSync = Arrays.stream(event.getChunk().getEntities()).map(Entity::getUniqueId).collect(Collectors.toSet());
            World world = event.getWorld();
            Chunk chunk = event.getChunk();
            Optional<WorldRegister> optionalWorldRegister = TagRegister.getWorld(world);
            if (!optionalWorldRegister.isPresent())
                return;
            WorldRegister worldRegister = optionalWorldRegister.get();

            Optional<RegionRegister> optionalRegionRegister = worldRegister.getRegion(chunk);
            if (!optionalRegionRegister.isPresent())
                return;
            RegionRegister regionRegister = optionalRegionRegister.get();
            if (!regionRegister.isChunkLoaded(chunk))
                return;
            regionRegister.unloadChunk(chunk, entitiesSync);
            entitiesSync.clear();
        });
    }
}